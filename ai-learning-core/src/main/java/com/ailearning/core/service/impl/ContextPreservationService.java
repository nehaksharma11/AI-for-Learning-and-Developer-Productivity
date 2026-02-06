package com.ailearning.core.service.impl;

import com.ailearning.core.model.*;
import com.ailearning.core.service.ContextEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for preserving and restoring developer work context.
 * Handles work state serialization, context switching support, and session continuity.
 */
@Service
public class ContextPreservationService {

    private final ContextEngine contextEngine;
    private final ProductivityTracker productivityTracker;
    
    // In-memory storage (would be replaced with persistent storage in production)
    private final Map<String, WorkState> workStates = new ConcurrentHashMap<>();
    private final Map<String, List<ContextSwitchEvent>> contextSwitchHistory = new ConcurrentHashMap<>();
    private final Map<String, WorkState> currentWorkStates = new ConcurrentHashMap<>();
    
    // Configuration
    private final int maxWorkStatesPerDeveloper = 50;
    private final int maxContextSwitchHistoryPerDeveloper = 100;
    private final long workStateExpirationDays = 7;

    @Autowired
    public ContextPreservationService(ContextEngine contextEngine, ProductivityTracker productivityTracker) {
        this.contextEngine = Objects.requireNonNull(contextEngine, "ContextEngine cannot be null");
        this.productivityTracker = Objects.requireNonNull(productivityTracker, "ProductivityTracker cannot be null");
    }

    /**
     * Captures the current work state for a developer.
     */
    public WorkState captureWorkState(String developerId, String projectId, CaptureRequest request) {
        String workStateId = generateWorkStateId(developerId);
        
        WorkState workState = WorkState.builder()
                .id(workStateId)
                .developerId(developerId)
                .projectId(projectId)
                .currentActivity(request.getCurrentActivity())
                .capturedAt(LocalDateTime.now())
                .openFiles(request.getOpenFiles())
                .activeFile(request.getActiveFile())
                .cursorPosition(request.getCursorPosition())
                .selectedText(request.getSelectedText())
                .ideState(request.getIdeState())
                .currentTask(request.getCurrentTask())
                .currentGoal(request.getCurrentGoal())
                .recentActions(request.getRecentActions())
                .relevantCodeReferences(request.getRelevantCodeReferences())
                .recentSearchQueries(request.getRecentSearchQueries())
                .activeLearningSession(request.getActiveLearningSession())
                .recentLearningTopics(request.getRecentLearningTopics())
                .activeProductivitySession(request.getActiveProductivitySession())
                .sessionMetrics(request.getSessionMetrics())
                .mentalModel(request.getMentalModel())
                .developerNotes(request.getDeveloperNotes())
                .restorationHints(generateRestorationHints(request))
                .expiresAt(LocalDateTime.now().plusDays(workStateExpirationDays))
                .build();

        // Store the work state
        workStates.put(workStateId, workState);
        currentWorkStates.put(developerId, workState);
        
        // Clean up old work states
        cleanupOldWorkStates(developerId);
        
        return workState;
    }

    /**
     * Restores a work state for a developer.
     */
    public RestorationResult restoreWorkState(String workStateId, String developerId) {
        WorkState workState = workStates.get(workStateId);
        if (workState == null) {
            return RestorationResult.failure("Work state not found: " + workStateId);
        }

        if (!workState.getDeveloperId().equals(developerId)) {
            return RestorationResult.failure("Work state does not belong to developer: " + developerId);
        }

        if (!workState.isValid()) {
            return RestorationResult.failure("Work state has expired");
        }

        try {
            // Generate restoration instructions
            List<RestorationInstruction> instructions = generateRestorationInstructions(workState);
            
            // Update current work state
            currentWorkStates.put(developerId, workState);
            
            // Create restoration result
            return RestorationResult.success(workState, instructions);
            
        } catch (Exception e) {
            return RestorationResult.failure("Failed to restore work state: " + e.getMessage());
        }
    }

    /**
     * Records a context switch event.
     */
    public ContextSwitchEvent recordContextSwitch(String developerId, ContextSwitchRequest request) {
        String eventId = generateContextSwitchEventId(developerId);
        
        // Get current work state for context
        WorkState currentState = currentWorkStates.get(developerId);
        
        ContextSwitchEvent event = ContextSwitchEvent.builder()
                .id(eventId)
                .developerId(developerId)
                .timestamp(LocalDateTime.now())
                .switchType(request.getSwitchType())
                .switchReason(request.getSwitchReason())
                .previousProjectId(currentState != null ? currentState.getProjectId() : null)
                .previousTask(currentState != null ? currentState.getCurrentTask() : null)
                .previousActivity(currentState != null ? currentState.getCurrentActivity().name() : null)
                .previousFile(currentState != null ? currentState.getActiveFile() : null)
                .previousWorkStateId(currentState != null ? currentState.getId() : null)
                .newProjectId(request.getNewProjectId())
                .newTask(request.getNewTask())
                .newActivity(request.getNewActivity())
                .newFile(request.getNewFile())
                .newWorkStateId(request.getNewWorkStateId())
                .previousContextDurationMinutes(calculatePreviousContextDuration(currentState))
                .interruptionCount(request.getInterruptionCount())
                .interruptionSource(request.getInterruptionSource())
                .metadata(request.getMetadata())
                .estimatedProductivityImpact(estimateProductivityImpact(request))
                .estimatedRecoveryTimeMinutes(estimateRecoveryTime(request))
                .build();

        // Store the event
        contextSwitchHistory.computeIfAbsent(developerId, k -> new ArrayList<>()).add(event);
        
        // Clean up old events
        cleanupOldContextSwitchEvents(developerId);
        
        // Update productivity tracker with context switch
        updateProductivityTrackerWithContextSwitch(developerId, event);
        
        return event;
    }

    /**
     * Gets available work states for a developer.
     */
    public List<WorkState.WorkStateSummary> getAvailableWorkStates(String developerId) {
        return workStates.values().stream()
                .filter(ws -> ws.getDeveloperId().equals(developerId))
                .filter(WorkState::isValid)
                .sorted((ws1, ws2) -> Double.compare(ws2.getPriorityScore(), ws1.getPriorityScore()))
                .map(WorkState::createSummary)
                .limit(20) // Limit to top 20 most relevant states
                .collect(Collectors.toList());
    }

    /**
     * Gets context switch history for a developer.
     */
    public List<ContextSwitchEvent> getContextSwitchHistory(String developerId, int limit) {
        List<ContextSwitchEvent> history = contextSwitchHistory.getOrDefault(developerId, new ArrayList<>());
        return history.stream()
                .sorted((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Analyzes context switching patterns for a developer.
     */
    public ContextSwitchAnalysis analyzeContextSwitchingPatterns(String developerId, LocalDateTime since) {
        List<ContextSwitchEvent> events = contextSwitchHistory.getOrDefault(developerId, new ArrayList<>())
                .stream()
                .filter(event -> event.getTimestamp().isAfter(since))
                .collect(Collectors.toList());

        if (events.isEmpty()) {
            return ContextSwitchAnalysis.empty(developerId);
        }

        return ContextSwitchAnalysis.builder()
                .developerId(developerId)
                .analysisStart(since)
                .analysisEnd(LocalDateTime.now())
                .totalSwitches(events.size())
                .averageSwitchesPerHour(calculateAverageSwitchesPerHour(events, since))
                .totalProductivityLoss(calculateTotalProductivityLoss(events))
                .mostCommonSwitchType(findMostCommonSwitchType(events))
                .mostCommonSwitchReason(findMostCommonSwitchReason(events))
                .averageRecoveryTime(calculateAverageRecoveryTime(events))
                .significantDisruptions(countSignificantDisruptions(events))
                .recommendations(generateContextSwitchRecommendations(events))
                .build();
    }

    /**
     * Suggests the best work state to restore based on current context.
     */
    public Optional<WorkState.WorkStateSummary> suggestWorkStateToRestore(String developerId, String currentProjectId, String currentTask) {
        return workStates.values().stream()
                .filter(ws -> ws.getDeveloperId().equals(developerId))
                .filter(WorkState::isValid)
                .filter(ws -> isRelevantWorkState(ws, currentProjectId, currentTask))
                .max(Comparator.comparing(ws -> calculateRelevanceScore(ws, currentProjectId, currentTask)))
                .map(WorkState::createSummary);
    }

    /**
     * Creates a context restoration plan for resuming work after an interruption.
     */
    public ContextRestorationPlan createRestorationPlan(String developerId, String workStateId) {
        WorkState workState = workStates.get(workStateId);
        if (workState == null || !workState.getDeveloperId().equals(developerId)) {
            return ContextRestorationPlan.empty();
        }

        List<RestorationStep> steps = new ArrayList<>();
        
        // Step 1: Restore IDE state
        steps.add(RestorationStep.builder()
                .stepNumber(1)
                .title("Restore IDE State")
                .description("Reopen files and restore IDE configuration")
                .estimatedTimeMinutes(2)
                .instructions(generateIdeRestorationInstructions(workState))
                .build());

        // Step 2: Review context
        steps.add(RestorationStep.builder()
                .stepNumber(2)
                .title("Review Work Context")
                .description("Review your previous task and mental model")
                .estimatedTimeMinutes(3)
                .instructions(generateContextReviewInstructions(workState))
                .build());

        // Step 3: Resume work
        steps.add(RestorationStep.builder()
                .stepNumber(3)
                .title("Resume Work")
                .description("Continue with your previous task")
                .estimatedTimeMinutes(1)
                .instructions(generateWorkResumptionInstructions(workState))
                .build());

        return ContextRestorationPlan.builder()
                .developerId(developerId)
                .workStateId(workStateId)
                .workState(workState)
                .steps(steps)
                .totalEstimatedTimeMinutes(steps.stream().mapToInt(RestorationStep::getEstimatedTimeMinutes).sum())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Helper methods

    private String generateWorkStateId(String developerId) {
        return developerId + "_ws_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateContextSwitchEventId(String developerId) {
        return developerId + "_cs_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Map<String, Object> generateRestorationHints(CaptureRequest request) {
        Map<String, Object> hints = new HashMap<>();
        
        // Add hints based on current context
        if (request.getActiveFile() != null) {
            hints.put("primaryFile", request.getActiveFile());
        }
        
        if (request.getCursorPosition() > 0) {
            hints.put("cursorPosition", request.getCursorPosition());
        }
        
        if (request.getCurrentTask() != null) {
            hints.put("taskDescription", request.getCurrentTask());
        }
        
        // Add contextual hints
        hints.put("captureReason", "automatic");
        hints.put("contextComplexity", calculateContextComplexity(request));
        
        return hints;
    }

    private int calculateContextComplexity(CaptureRequest request) {
        int complexity = 0;
        complexity += request.getOpenFiles().size();
        complexity += request.getRecentActions().size() / 2;
        complexity += request.getRelevantCodeReferences().size();
        if (request.getCurrentTask() != null) complexity += 2;
        if (request.getMentalModel() != null) complexity += 3;
        return Math.min(10, complexity); // Cap at 10
    }

    private List<RestorationInstruction> generateRestorationInstructions(WorkState workState) {
        List<RestorationInstruction> instructions = new ArrayList<>();
        
        // File restoration
        if (!workState.getOpenFiles().isEmpty()) {
            instructions.add(RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.OPEN_FILES)
                    .description("Reopen " + workState.getOpenFiles().size() + " files")
                    .data(Map.of("files", workState.getOpenFiles()))
                    .priority(1)
                    .build());
        }
        
        // Active file and cursor position
        if (workState.getActiveFile() != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("file", workState.getActiveFile());
            data.put("cursorPosition", workState.getCursorPosition());
            if (workState.getSelectedText() != null) {
                data.put("selectedText", workState.getSelectedText());
            }
            
            instructions.add(RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.SET_ACTIVE_FILE)
                    .description("Set active file and cursor position")
                    .data(data)
                    .priority(2)
                    .build());
        }
        
        // Task context
        if (workState.getCurrentTask() != null) {
            instructions.add(RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.SHOW_CONTEXT)
                    .description("Display task context: " + workState.getCurrentTask())
                    .data(Map.of("task", workState.getCurrentTask()))
                    .priority(3)
                    .build());
        }
        
        // Mental model
        if (workState.getMentalModel() != null) {
            instructions.add(RestorationInstruction.builder()
                    .type(RestorationInstruction.InstructionType.SHOW_CONTEXT)
                    .description("Display mental model")
                    .data(Map.of("mentalModel", workState.getMentalModel()))
                    .priority(4)
                    .build());
        }
        
        return instructions.stream()
                .sorted(Comparator.comparing(RestorationInstruction::getPriority))
                .collect(Collectors.toList());
    }

    private long calculatePreviousContextDuration(WorkState currentState) {
        if (currentState == null) return 0;
        return java.time.Duration.between(currentState.getCapturedAt(), LocalDateTime.now()).toMinutes();
    }

    private double estimateProductivityImpact(ContextSwitchRequest request) {
        double impact = 0.0;
        
        // Base impact based on switch type
        switch (request.getSwitchType()) {
            case PROJECT_CHANGE: impact = -0.7; break;
            case TASK_CHANGE: impact = -0.5; break;
            case ACTIVITY_CHANGE: impact = -0.3; break;
            case FILE_CHANGE: impact = -0.1; break;
            case INTERRUPTION: impact = -0.6; break;
            case BREAK: impact = 0.2; break; // Breaks can be positive
            case RETURN_FROM_BREAK: impact = -0.2; break;
        }
        
        // Adjust based on reason
        switch (request.getSwitchReason()) {
            case INTERRUPTION: impact -= 0.2; break;
            case DISTRACTION: impact -= 0.1; break;
            case COMPLETION: impact += 0.3; break; // Completing tasks is positive
            case PLANNED: impact += 0.1; break;
        }
        
        return Math.max(-1.0, Math.min(1.0, impact));
    }

    private long estimateRecoveryTime(ContextSwitchRequest request) {
        long baseTime = 0;
        
        switch (request.getSwitchType()) {
            case PROJECT_CHANGE: baseTime = 15; break;
            case TASK_CHANGE: baseTime = 10; break;
            case ACTIVITY_CHANGE: baseTime = 5; break;
            case FILE_CHANGE: baseTime = 2; break;
            case INTERRUPTION: baseTime = 8; break;
            case BREAK: baseTime = 3; break;
            case RETURN_FROM_BREAK: baseTime = 5; break;
        }
        
        // Adjust based on interruption count
        baseTime += request.getInterruptionCount() * 2;
        
        return baseTime;
    }

    private void cleanupOldWorkStates(String developerId) {
        List<WorkState> developerStates = workStates.values().stream()
                .filter(ws -> ws.getDeveloperId().equals(developerId))
                .sorted((ws1, ws2) -> ws2.getCapturedAt().compareTo(ws1.getCapturedAt()))
                .collect(Collectors.toList());

        // Remove excess states
        if (developerStates.size() > maxWorkStatesPerDeveloper) {
            developerStates.subList(maxWorkStatesPerDeveloper, developerStates.size())
                    .forEach(ws -> workStates.remove(ws.getId()));
        }

        // Remove expired states
        workStates.entrySet().removeIf(entry -> 
                entry.getValue().getDeveloperId().equals(developerId) && !entry.getValue().isValid());
    }

    private void cleanupOldContextSwitchEvents(String developerId) {
        List<ContextSwitchEvent> events = contextSwitchHistory.get(developerId);
        if (events != null && events.size() > maxContextSwitchHistoryPerDeveloper) {
            events.sort((e1, e2) -> e2.getTimestamp().compareTo(e1.getTimestamp()));
            contextSwitchHistory.put(developerId, 
                    events.subList(0, maxContextSwitchHistoryPerDeveloper));
        }
    }

    private void updateProductivityTrackerWithContextSwitch(String developerId, ContextSwitchEvent event) {
        try {
            Optional<DevelopmentSession> activeSession = productivityTracker.getActiveSession(developerId);
            if (activeSession.isPresent()) {
                ProductivityTracker.SessionUpdateData updateData = new ProductivityTracker.SessionUpdateData();
                updateData.contextSwitches = 1;
                updateData.interruptions = event.isSignificantDisruption() ? 1 : 0;
                productivityTracker.updateSession(activeSession.get().getId(), updateData);
            }
        } catch (Exception e) {
            // Log error but don't fail the context switch recording
        }
    }

    // Analysis helper methods

    private double calculateAverageSwitchesPerHour(List<ContextSwitchEvent> events, LocalDateTime since) {
        if (events.isEmpty()) return 0.0;
        long hours = java.time.Duration.between(since, LocalDateTime.now()).toHours();
        return hours > 0 ? (double) events.size() / hours : events.size();
    }

    private double calculateTotalProductivityLoss(List<ContextSwitchEvent> events) {
        return events.stream()
                .mapToDouble(ContextSwitchEvent::getContextSwitchingCost)
                .sum();
    }

    private ContextSwitchEvent.SwitchType findMostCommonSwitchType(List<ContextSwitchEvent> events) {
        return events.stream()
                .collect(Collectors.groupingBy(ContextSwitchEvent::getSwitchType, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ContextSwitchEvent.SwitchType.TASK_CHANGE);
    }

    private ContextSwitchEvent.SwitchReason findMostCommonSwitchReason(List<ContextSwitchEvent> events) {
        return events.stream()
                .collect(Collectors.groupingBy(ContextSwitchEvent::getSwitchReason, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ContextSwitchEvent.SwitchReason.UNKNOWN);
    }

    private double calculateAverageRecoveryTime(List<ContextSwitchEvent> events) {
        return events.stream()
                .mapToLong(ContextSwitchEvent::getEstimatedRecoveryTimeMinutes)
                .average()
                .orElse(0.0);
    }

    private int countSignificantDisruptions(List<ContextSwitchEvent> events) {
        return (int) events.stream()
                .filter(ContextSwitchEvent::isSignificantDisruption)
                .count();
    }

    private List<String> generateContextSwitchRecommendations(List<ContextSwitchEvent> events) {
        List<String> recommendations = new ArrayList<>();
        
        long interruptionCount = events.stream()
                .filter(e -> e.getSwitchReason() == ContextSwitchEvent.SwitchReason.INTERRUPTION)
                .count();
        
        if (interruptionCount > events.size() * 0.3) {
            recommendations.add("Consider using focus time blocks to reduce interruptions");
        }
        
        double avgRecoveryTime = calculateAverageRecoveryTime(events);
        if (avgRecoveryTime > 10) {
            recommendations.add("High context switching cost detected. Try to batch similar tasks together");
        }
        
        if (events.size() > 20) {
            recommendations.add("High number of context switches. Consider task prioritization and time blocking");
        }
        
        return recommendations;
    }

    private boolean isRelevantWorkState(WorkState workState, String currentProjectId, String currentTask) {
        // Check project relevance
        if (currentProjectId != null && !currentProjectId.equals(workState.getProjectId())) {
            return false;
        }
        
        // Check task relevance
        if (currentTask != null && workState.getCurrentTask() != null) {
            return workState.getCurrentTask().toLowerCase().contains(currentTask.toLowerCase()) ||
                   currentTask.toLowerCase().contains(workState.getCurrentTask().toLowerCase());
        }
        
        return true;
    }

    private double calculateRelevanceScore(WorkState workState, String currentProjectId, String currentTask) {
        double score = workState.getPriorityScore();
        
        // Boost score for same project
        if (currentProjectId != null && currentProjectId.equals(workState.getProjectId())) {
            score += 0.3;
        }
        
        // Boost score for similar task
        if (currentTask != null && workState.getCurrentTask() != null) {
            if (workState.getCurrentTask().toLowerCase().contains(currentTask.toLowerCase())) {
                score += 0.2;
            }
        }
        
        return score;
    }

    private List<String> generateIdeRestorationInstructions(WorkState workState) {
        List<String> instructions = new ArrayList<>();
        
        if (!workState.getOpenFiles().isEmpty()) {
            instructions.add("Open files: " + String.join(", ", workState.getOpenFiles()));
        }
        
        if (workState.getActiveFile() != null) {
            instructions.add("Set active file: " + workState.getActiveFile());
            if (workState.getCursorPosition() > 0) {
                instructions.add("Position cursor at line/column: " + workState.getCursorPosition());
            }
        }
        
        return instructions;
    }

    private List<String> generateContextReviewInstructions(WorkState workState) {
        List<String> instructions = new ArrayList<>();
        
        if (workState.getCurrentTask() != null) {
            instructions.add("Review task: " + workState.getCurrentTask());
        }
        
        if (workState.getCurrentGoal() != null) {
            instructions.add("Review goal: " + workState.getCurrentGoal());
        }
        
        if (workState.getMentalModel() != null) {
            instructions.add("Review mental model: " + workState.getMentalModel());
        }
        
        if (!workState.getRecentActions().isEmpty()) {
            instructions.add("Review recent actions: " + String.join(", ", workState.getRecentActions()));
        }
        
        return instructions;
    }

    private List<String> generateWorkResumptionInstructions(WorkState workState) {
        List<String> instructions = new ArrayList<>();
        
        instructions.add("Take a moment to reorient yourself with the code");
        
        if (workState.getSelectedText() != null) {
            instructions.add("Review selected text: " + workState.getSelectedText());
        }
        
        instructions.add("Continue with your previous work");
        
        return instructions;
    }

    // Data classes for requests and responses

    public static class CaptureRequest {
        private WorkState.StateType currentActivity = WorkState.StateType.CODING;
        private List<String> openFiles = new ArrayList<>();
        private String activeFile;
        private int cursorPosition = 0;
        private String selectedText;
        private Map<String, Object> ideState = new HashMap<>();
        private String currentTask;
        private String currentGoal;
        private List<String> recentActions = new ArrayList<>();
        private List<CodeReference> relevantCodeReferences = new ArrayList<>();
        private List<String> recentSearchQueries = new ArrayList<>();
        private String activeLearningSession;
        private List<String> recentLearningTopics = new ArrayList<>();
        private String activeProductivitySession;
        private Map<String, Object> sessionMetrics = new HashMap<>();
        private String mentalModel;
        private List<String> developerNotes = new ArrayList<>();

        // Getters and setters
        public WorkState.StateType getCurrentActivity() { return currentActivity; }
        public void setCurrentActivity(WorkState.StateType currentActivity) { this.currentActivity = currentActivity; }
        public List<String> getOpenFiles() { return openFiles; }
        public void setOpenFiles(List<String> openFiles) { this.openFiles = openFiles; }
        public String getActiveFile() { return activeFile; }
        public void setActiveFile(String activeFile) { this.activeFile = activeFile; }
        public int getCursorPosition() { return cursorPosition; }
        public void setCursorPosition(int cursorPosition) { this.cursorPosition = cursorPosition; }
        public String getSelectedText() { return selectedText; }
        public void setSelectedText(String selectedText) { this.selectedText = selectedText; }
        public Map<String, Object> getIdeState() { return ideState; }
        public void setIdeState(Map<String, Object> ideState) { this.ideState = ideState; }
        public String getCurrentTask() { return currentTask; }
        public void setCurrentTask(String currentTask) { this.currentTask = currentTask; }
        public String getCurrentGoal() { return currentGoal; }
        public void setCurrentGoal(String currentGoal) { this.currentGoal = currentGoal; }
        public List<String> getRecentActions() { return recentActions; }
        public void setRecentActions(List<String> recentActions) { this.recentActions = recentActions; }
        public List<CodeReference> getRelevantCodeReferences() { return relevantCodeReferences; }
        public void setRelevantCodeReferences(List<CodeReference> relevantCodeReferences) { this.relevantCodeReferences = relevantCodeReferences; }
        public List<String> getRecentSearchQueries() { return recentSearchQueries; }
        public void setRecentSearchQueries(List<String> recentSearchQueries) { this.recentSearchQueries = recentSearchQueries; }
        public String getActiveLearningSession() { return activeLearningSession; }
        public void setActiveLearningSession(String activeLearningSession) { this.activeLearningSession = activeLearningSession; }
        public List<String> getRecentLearningTopics() { return recentLearningTopics; }
        public void setRecentLearningTopics(List<String> recentLearningTopics) { this.recentLearningTopics = recentLearningTopics; }
        public String getActiveProductivitySession() { return activeProductivitySession; }
        public void setActiveProductivitySession(String activeProductivitySession) { this.activeProductivitySession = activeProductivitySession; }
        public Map<String, Object> getSessionMetrics() { return sessionMetrics; }
        public void setSessionMetrics(Map<String, Object> sessionMetrics) { this.sessionMetrics = sessionMetrics; }
        public String getMentalModel() { return mentalModel; }
        public void setMentalModel(String mentalModel) { this.mentalModel = mentalModel; }
        public List<String> getDeveloperNotes() { return developerNotes; }
        public void setDeveloperNotes(List<String> developerNotes) { this.developerNotes = developerNotes; }
    }

    public static class ContextSwitchRequest {
        private ContextSwitchEvent.SwitchType switchType;
        private ContextSwitchEvent.SwitchReason switchReason = ContextSwitchEvent.SwitchReason.UNKNOWN;
        private String newProjectId;
        private String newTask;
        private String newActivity;
        private String newFile;
        private String newWorkStateId;
        private int interruptionCount = 0;
        private String interruptionSource;
        private Map<String, Object> metadata = new HashMap<>();

        // Getters and setters
        public ContextSwitchEvent.SwitchType getSwitchType() { return switchType; }
        public void setSwitchType(ContextSwitchEvent.SwitchType switchType) { this.switchType = switchType; }
        public ContextSwitchEvent.SwitchReason getSwitchReason() { return switchReason; }
        public void setSwitchReason(ContextSwitchEvent.SwitchReason switchReason) { this.switchReason = switchReason; }
        public String getNewProjectId() { return newProjectId; }
        public void setNewProjectId(String newProjectId) { this.newProjectId = newProjectId; }
        public String getNewTask() { return newTask; }
        public void setNewTask(String newTask) { this.newTask = newTask; }
        public String getNewActivity() { return newActivity; }
        public void setNewActivity(String newActivity) { this.newActivity = newActivity; }
        public String getNewFile() { return newFile; }
        public void setNewFile(String newFile) { this.newFile = newFile; }
        public String getNewWorkStateId() { return newWorkStateId; }
        public void setNewWorkStateId(String newWorkStateId) { this.newWorkStateId = newWorkStateId; }
        public int getInterruptionCount() { return interruptionCount; }
        public void setInterruptionCount(int interruptionCount) { this.interruptionCount = interruptionCount; }
        public String getInterruptionSource() { return interruptionSource; }
        public void setInterruptionSource(String interruptionSource) { this.interruptionSource = interruptionSource; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}