package com.ailearning.core.service.impl;

import com.ailearning.core.model.DevelopmentSession;
import com.ailearning.core.model.ProductivityMetrics;
import com.ailearning.core.model.CodeChange;
import com.ailearning.core.model.AnalysisResult;
import com.ailearning.core.service.CodeAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for tracking developer productivity metrics in real-time.
 * Collects data from development sessions and calculates comprehensive productivity metrics.
 */
@Service
public class ProductivityTracker {

    private final CodeAnalyzer codeAnalyzer;
    private final Map<String, DevelopmentSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, List<DevelopmentSession>> sessionHistory = new ConcurrentHashMap<>();
    private final Map<String, ProductivityMetrics> cachedMetrics = new ConcurrentHashMap<>();

    @Autowired
    public ProductivityTracker(CodeAnalyzer codeAnalyzer) {
        this.codeAnalyzer = Objects.requireNonNull(codeAnalyzer, "CodeAnalyzer cannot be null");
    }

    /**
     * Starts a new development session for tracking.
     */
    public DevelopmentSession startSession(String developerId, String projectId) {
        String sessionId = generateSessionId(developerId);
        
        DevelopmentSession session = DevelopmentSession.builder()
                .id(sessionId)
                .developerId(developerId)
                .projectId(projectId)
                .startTime(LocalDateTime.now())
                .state(DevelopmentSession.SessionState.ACTIVE)
                .build();
        
        activeSessions.put(sessionId, session);
        return session;
    }

    /**
     * Updates an active session with new activity data.
     */
    public DevelopmentSession updateSession(String sessionId, SessionUpdateData updateData) {
        DevelopmentSession currentSession = activeSessions.get(sessionId);
        if (currentSession == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        DevelopmentSession updatedSession = DevelopmentSession.builder()
                .id(currentSession.getId())
                .developerId(currentSession.getDeveloperId())
                .projectId(currentSession.getProjectId())
                .startTime(currentSession.getStartTime())
                .endTime(currentSession.getEndTime())
                .state(currentSession.getState())
                .activities(mergeActivities(currentSession.getActivities(), updateData.newActivities))
                .sessionData(mergeSessionData(currentSession.getSessionData(), updateData.sessionData))
                .totalKeystrokes(currentSession.getTotalKeystrokes() + updateData.keystrokesDelta)
                .totalMouseClicks(currentSession.getTotalMouseClicks() + updateData.mouseClicksDelta)
                .filesModified(Math.max(currentSession.getFilesModified(), updateData.filesModified))
                .linesAdded(currentSession.getLinesAdded() + updateData.linesAdded)
                .linesDeleted(currentSession.getLinesDeleted() + updateData.linesDeleted)
                .contextSwitches(currentSession.getContextSwitches() + updateData.contextSwitches)
                .focusScore(calculateFocusScore(currentSession, updateData))
                .interruptionCount(currentSession.getInterruptionCount() + updateData.interruptions)
                .idleTimeMinutes(currentSession.getIdleTimeMinutes() + updateData.idleTimeMinutes)
                .lastUpdated(LocalDateTime.now())
                .build();

        activeSessions.put(sessionId, updatedSession);
        return updatedSession;
    }

    /**
     * Ends a development session and moves it to history.
     */
    public DevelopmentSession endSession(String sessionId) {
        DevelopmentSession session = activeSessions.remove(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        DevelopmentSession completedSession = DevelopmentSession.builder()
                .id(session.getId())
                .developerId(session.getDeveloperId())
                .projectId(session.getProjectId())
                .startTime(session.getStartTime())
                .endTime(LocalDateTime.now())
                .state(DevelopmentSession.SessionState.COMPLETED)
                .activities(session.getActivities())
                .sessionData(session.getSessionData())
                .totalKeystrokes(session.getTotalKeystrokes())
                .totalMouseClicks(session.getTotalMouseClicks())
                .filesModified(session.getFilesModified())
                .linesAdded(session.getLinesAdded())
                .linesDeleted(session.getLinesDeleted())
                .contextSwitches(session.getContextSwitches())
                .focusScore(session.getFocusScore())
                .interruptionCount(session.getInterruptionCount())
                .idleTimeMinutes(session.getIdleTimeMinutes())
                .lastUpdated(LocalDateTime.now())
                .build();

        // Add to history
        sessionHistory.computeIfAbsent(session.getDeveloperId(), k -> new ArrayList<>())
                .add(completedSession);

        // Clear cached metrics to force recalculation
        cachedMetrics.remove(session.getDeveloperId());

        return completedSession;
    }

    /**
     * Calculates productivity metrics for a developer over a specified period.
     */
    public ProductivityMetrics calculateMetrics(String developerId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        String cacheKey = developerId + "_" + periodStart + "_" + periodEnd;
        
        // Check cache first
        ProductivityMetrics cached = cachedMetrics.get(cacheKey);
        if (cached != null && cached.getCalculatedAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
            return cached;
        }

        List<DevelopmentSession> sessions = getSessionsInPeriod(developerId, periodStart, periodEnd);
        ProductivityMetrics metrics = calculateMetricsFromSessions(developerId, sessions, periodStart, periodEnd);
        
        cachedMetrics.put(cacheKey, metrics);
        return metrics;
    }

    /**
     * Gets productivity metrics for the current day.
     */
    public ProductivityMetrics getDailyMetrics(String developerId) {
        LocalDateTime startOfDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return calculateMetrics(developerId, startOfDay, endOfDay);
    }

    /**
     * Gets productivity metrics for the current week.
     */
    public ProductivityMetrics getWeeklyMetrics(String developerId) {
        LocalDateTime startOfWeek = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
                .minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1);
        LocalDateTime endOfWeek = startOfWeek.plusWeeks(1);
        return calculateMetrics(developerId, startOfWeek, endOfWeek);
    }

    /**
     * Records a code change event for productivity tracking.
     */
    public void recordCodeChange(String sessionId, CodeChange change) {
        DevelopmentSession session = activeSessions.get(sessionId);
        if (session == null) return;

        SessionUpdateData updateData = new SessionUpdateData();
        updateData.linesAdded = change.getLinesAdded();
        updateData.linesDeleted = change.getLinesDeleted();
        updateData.filesModified = 1;
        
        // Analyze code quality if possible
        try {
            AnalysisResult analysis = codeAnalyzer.analyzeCode(change.getNewContent(), change.getLanguage());
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("lastCodeQuality", analysis.getQualityScore());
            sessionData.put("lastComplexity", analysis.getComplexityMetrics().getCyclomaticComplexity());
            updateData.sessionData = sessionData;
        } catch (Exception e) {
            // Continue without analysis if it fails
        }

        updateSession(sessionId, updateData);
    }

    /**
     * Gets the current active session for a developer.
     */
    public Optional<DevelopmentSession> getActiveSession(String developerId) {
        return activeSessions.values().stream()
                .filter(session -> session.getDeveloperId().equals(developerId))
                .findFirst();
    }

    /**
     * Gets session history for a developer.
     */
    public List<DevelopmentSession> getSessionHistory(String developerId, int limit) {
        List<DevelopmentSession> sessions = sessionHistory.getOrDefault(developerId, new ArrayList<>());
        return sessions.stream()
                .sorted((s1, s2) -> s2.getStartTime().compareTo(s1.getStartTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<DevelopmentSession> getSessionsInPeriod(String developerId, LocalDateTime start, LocalDateTime end) {
        List<DevelopmentSession> allSessions = new ArrayList<>();
        
        // Add completed sessions from history
        List<DevelopmentSession> history = sessionHistory.getOrDefault(developerId, new ArrayList<>());
        allSessions.addAll(history.stream()
                .filter(session -> isSessionInPeriod(session, start, end))
                .collect(Collectors.toList()));
        
        // Add active sessions if they overlap with the period
        activeSessions.values().stream()
                .filter(session -> session.getDeveloperId().equals(developerId))
                .filter(session -> isSessionInPeriod(session, start, end))
                .forEach(allSessions::add);
        
        return allSessions;
    }

    private boolean isSessionInPeriod(DevelopmentSession session, LocalDateTime start, LocalDateTime end) {
        LocalDateTime sessionEnd = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();
        return !session.getStartTime().isAfter(end) && !sessionEnd.isBefore(start);
    }

    private ProductivityMetrics calculateMetricsFromSessions(String developerId, List<DevelopmentSession> sessions, 
                                                           LocalDateTime periodStart, LocalDateTime periodEnd) {
        if (sessions.isEmpty()) {
            return createEmptyMetrics(developerId, periodStart, periodEnd);
        }

        // Aggregate session data
        int totalLinesAdded = sessions.stream().mapToInt(DevelopmentSession::getLinesAdded).sum();
        int totalLinesDeleted = sessions.stream().mapToInt(DevelopmentSession::getLinesDeleted).sum();
        int totalFilesModified = sessions.stream().mapToInt(DevelopmentSession::getFilesModified).sum();
        int totalContextSwitches = sessions.stream().mapToInt(DevelopmentSession::getContextSwitches).sum();
        long totalActiveTime = sessions.stream().mapToLong(DevelopmentSession::getActiveCodingMinutes).sum();
        
        // Calculate averages and derived metrics
        double avgFocusScore = sessions.stream().mapToDouble(DevelopmentSession::getFocusScore).average().orElse(0.0);
        double avgProductivityScore = sessions.stream().mapToDouble(DevelopmentSession::getProductivityScore).average().orElse(0.0);
        
        // Estimate other metrics based on available data
        int estimatedCommits = Math.max(1, totalFilesModified / 3); // Rough estimate
        int estimatedTests = Math.max(0, totalLinesAdded / 20); // Rough estimate
        
        return ProductivityMetrics.builder()
                .id(generateMetricsId(developerId, periodStart))
                .developerId(developerId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .linesOfCodeWritten(totalLinesAdded)
                .linesOfCodeDeleted(totalLinesDeleted)
                .commitsCount(estimatedCommits)
                .testsWritten(estimatedTests)
                .activeCodeTime((int) totalActiveTime)
                .contextSwitches(totalContextSwitches)
                .focusScore(avgFocusScore)
                .velocityScore(avgProductivityScore * 100)
                .codeQualityScore(getAverageCodeQuality(sessions))
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    private ProductivityMetrics createEmptyMetrics(String developerId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        return ProductivityMetrics.builder()
                .id(generateMetricsId(developerId, periodStart))
                .developerId(developerId)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    private double getAverageCodeQuality(List<DevelopmentSession> sessions) {
        return sessions.stream()
                .flatMap(session -> session.getSessionData().values().stream())
                .filter(value -> value instanceof Double)
                .mapToDouble(value -> (Double) value)
                .average()
                .orElse(0.5); // Default quality score
    }

    private double calculateFocusScore(DevelopmentSession currentSession, SessionUpdateData updateData) {
        // Simple focus calculation based on interruptions and context switches
        long totalTime = currentSession.getDurationMinutes();
        if (totalTime == 0) return 1.0;
        
        int totalInterruptions = currentSession.getInterruptionCount() + updateData.interruptions;
        int totalContextSwitches = currentSession.getContextSwitches() + updateData.contextSwitches;
        
        double interruptionPenalty = Math.min(0.5, totalInterruptions * 0.1);
        double contextSwitchPenalty = Math.min(0.3, totalContextSwitches * 0.05);
        
        return Math.max(0.0, 1.0 - interruptionPenalty - contextSwitchPenalty);
    }

    private List<DevelopmentSession.SessionActivity> mergeActivities(
            List<DevelopmentSession.SessionActivity> existing, 
            List<DevelopmentSession.SessionActivity> newActivities) {
        List<DevelopmentSession.SessionActivity> merged = new ArrayList<>(existing);
        if (newActivities != null) {
            merged.addAll(newActivities);
        }
        return merged;
    }

    private Map<String, Object> mergeSessionData(Map<String, Object> existing, Map<String, Object> newData) {
        Map<String, Object> merged = new HashMap<>(existing);
        if (newData != null) {
            merged.putAll(newData);
        }
        return merged;
    }

    private String generateSessionId(String developerId) {
        return developerId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateMetricsId(String developerId, LocalDateTime periodStart) {
        return developerId + "_metrics_" + periodStart.toString().replace(":", "-");
    }

    /**
     * Data class for session updates.
     */
    public static class SessionUpdateData {
        public List<DevelopmentSession.SessionActivity> newActivities = new ArrayList<>();
        public Map<String, Object> sessionData = new HashMap<>();
        public int keystrokesDelta = 0;
        public int mouseClicksDelta = 0;
        public int filesModified = 0;
        public int linesAdded = 0;
        public int linesDeleted = 0;
        public int contextSwitches = 0;
        public int interruptions = 0;
        public long idleTimeMinutes = 0;
    }
}