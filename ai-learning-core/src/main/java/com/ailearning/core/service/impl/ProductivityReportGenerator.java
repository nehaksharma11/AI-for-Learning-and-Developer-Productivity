package com.ailearning.core.service.impl;

import com.ailearning.core.model.ProductivityMetrics;
import com.ailearning.core.model.DevelopmentSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating productivity reports and visualizations.
 * Creates comprehensive reports from productivity metrics and session data.
 */
@Service
public class ProductivityReportGenerator {

    private final ProductivityTracker productivityTracker;

    @Autowired
    public ProductivityReportGenerator(ProductivityTracker productivityTracker) {
        this.productivityTracker = Objects.requireNonNull(productivityTracker, "ProductivityTracker cannot be null");
    }

    /**
     * Generates a comprehensive productivity report for a developer.
     */
    public ProductivityReport generateReport(String developerId, ReportPeriod period) {
        LocalDateTime[] dateRange = calculateDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        ProductivityMetrics metrics = productivityTracker.calculateMetrics(developerId, startDate, endDate);
        List<DevelopmentSession> sessions = productivityTracker.getSessionHistory(developerId, 50)
                .stream()
                .filter(session -> isSessionInPeriod(session, startDate, endDate))
                .collect(Collectors.toList());

        return ProductivityReport.builder()
                .developerId(developerId)
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .metrics(metrics)
                .sessions(sessions)
                .summary(generateSummary(metrics, sessions))
                .trends(calculateTrends(developerId, period))
                .recommendations(generateRecommendations(metrics, sessions))
                .charts(generateChartData(metrics, sessions))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Generates a quick daily summary report.
     */
    public DailySummary generateDailySummary(String developerId) {
        ProductivityMetrics dailyMetrics = productivityTracker.getDailyMetrics(developerId);
        Optional<DevelopmentSession> activeSession = productivityTracker.getActiveSession(developerId);

        return DailySummary.builder()
                .developerId(developerId)
                .date(LocalDateTime.now().toLocalDate())
                .metrics(dailyMetrics)
                .activeSession(activeSession.orElse(null))
                .totalActiveTime(dailyMetrics.getTotalProductiveTime())
                .productivityScore(dailyMetrics.getOverallProductivityScore())
                .keyAchievements(identifyKeyAchievements(dailyMetrics))
                .areasForImprovement(identifyImprovementAreas(dailyMetrics))
                .build();
    }

    /**
     * Generates trend analysis comparing current period with previous periods.
     */
    public TrendAnalysis generateTrendAnalysis(String developerId, ReportPeriod period, int periodsToCompare) {
        List<ProductivityMetrics> periodMetrics = new ArrayList<>();
        LocalDateTime[] currentRange = calculateDateRange(period);
        
        for (int i = 0; i < periodsToCompare; i++) {
            LocalDateTime[] range = calculatePreviousPeriodRange(period, i);
            ProductivityMetrics metrics = productivityTracker.calculateMetrics(developerId, range[0], range[1]);
            periodMetrics.add(metrics);
        }

        return TrendAnalysis.builder()
                .developerId(developerId)
                .period(period)
                .periodsAnalyzed(periodsToCompare)
                .metrics(periodMetrics)
                .trends(calculateMetricTrends(periodMetrics))
                .insights(generateTrendInsights(periodMetrics))
                .build();
    }

    /**
     * Exports productivity data in various formats.
     */
    public String exportReport(ProductivityReport report, ExportFormat format) {
        switch (format) {
            case JSON:
                return exportAsJson(report);
            case CSV:
                return exportAsCsv(report);
            case MARKDOWN:
                return exportAsMarkdown(report);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }

    private ProductivitySummary generateSummary(ProductivityMetrics metrics, List<DevelopmentSession> sessions) {
        return ProductivitySummary.builder()
                .totalSessions(sessions.size())
                .totalActiveTime(metrics.getTotalProductiveTime())
                .averageSessionLength(calculateAverageSessionLength(sessions))
                .mostProductiveHour(findMostProductiveHour(sessions))
                .totalLinesOfCode(metrics.getNetLinesOfCode())
                .averageFocusScore(metrics.getFocusScore())
                .productivityScore(metrics.getOverallProductivityScore())
                .build();
    }

    private List<ProductivityTrend> calculateTrends(String developerId, ReportPeriod period) {
        List<ProductivityTrend> trends = new ArrayList<>();
        
        // Get previous period for comparison
        LocalDateTime[] previousRange = calculatePreviousPeriodRange(period, 1);
        ProductivityMetrics previousMetrics = productivityTracker.calculateMetrics(
                developerId, previousRange[0], previousRange[1]);
        
        LocalDateTime[] currentRange = calculateDateRange(period);
        ProductivityMetrics currentMetrics = productivityTracker.calculateMetrics(
                developerId, currentRange[0], currentRange[1]);

        // Calculate trends for key metrics
        trends.add(calculateTrend("Overall Productivity", 
                previousMetrics.getOverallProductivityScore(), 
                currentMetrics.getOverallProductivityScore()));
        
        trends.add(calculateTrend("Lines of Code", 
                previousMetrics.getNetLinesOfCode(), 
                currentMetrics.getNetLinesOfCode()));
        
        trends.add(calculateTrend("Focus Score", 
                previousMetrics.getFocusScore(), 
                currentMetrics.getFocusScore()));
        
        trends.add(calculateTrend("Code Velocity", 
                previousMetrics.getCodeVelocity(), 
                currentMetrics.getCodeVelocity()));

        return trends;
    }

    private List<String> generateRecommendations(ProductivityMetrics metrics, List<DevelopmentSession> sessions) {
        List<String> recommendations = new ArrayList<>();

        // Focus-based recommendations
        if (metrics.getFocusScore() < 0.7) {
            recommendations.add("Consider reducing context switches to improve focus. " +
                    "Try time-blocking techniques or the Pomodoro method.");
        }

        // Productivity-based recommendations
        if (metrics.getOverallProductivityScore() < 0.6) {
            recommendations.add("Your productivity score suggests room for improvement. " +
                    "Focus on completing more meaningful tasks and reducing interruptions.");
        }

        // Code quality recommendations
        if (metrics.getCodeQualityScore() < 0.7) {
            recommendations.add("Consider spending more time on code review and refactoring " +
                    "to improve overall code quality.");
        }

        // Session length recommendations
        double avgSessionLength = calculateAverageSessionLength(sessions);
        if (avgSessionLength < 30) {
            recommendations.add("Your sessions are quite short. Consider longer focused work periods " +
                    "for better deep work and productivity.");
        } else if (avgSessionLength > 180) {
            recommendations.add("Your sessions are quite long. Consider taking regular breaks " +
                    "to maintain focus and prevent burnout.");
        }

        return recommendations;
    }

    private Map<String, Object> generateChartData(ProductivityMetrics metrics, List<DevelopmentSession> sessions) {
        Map<String, Object> chartData = new HashMap<>();

        // Productivity over time
        chartData.put("productivityOverTime", generateProductivityTimeChart(sessions));
        
        // Activity distribution
        chartData.put("activityDistribution", generateActivityDistributionChart(sessions));
        
        // Focus score trend
        chartData.put("focusTrend", generateFocusTrendChart(sessions));
        
        // Code metrics breakdown
        chartData.put("codeMetrics", generateCodeMetricsChart(metrics));

        return chartData;
    }

    private List<String> identifyKeyAchievements(ProductivityMetrics metrics) {
        List<String> achievements = new ArrayList<>();

        if (metrics.getOverallProductivityScore() > 0.8) {
            achievements.add("Excellent productivity score of " + 
                    String.format("%.1f%%", metrics.getOverallProductivityScore() * 100));
        }

        if (metrics.getNetLinesOfCode() > 200) {
            achievements.add("High code output: " + metrics.getNetLinesOfCode() + " net lines of code");
        }

        if (metrics.getFocusScore() > 0.8) {
            achievements.add("Great focus with minimal interruptions");
        }

        if (metrics.getCodeQualityScore() > 0.8) {
            achievements.add("High code quality maintained");
        }

        return achievements;
    }

    private List<String> identifyImprovementAreas(ProductivityMetrics metrics) {
        List<String> improvements = new ArrayList<>();

        if (metrics.getFocusScore() < 0.6) {
            improvements.add("Focus: Too many interruptions and context switches");
        }

        if (metrics.getCodeQualityScore() < 0.6) {
            improvements.add("Code Quality: Consider more thorough testing and review");
        }

        if (metrics.getTotalProductiveTime() < 240) { // Less than 4 hours
            improvements.add("Active Time: Increase focused coding time");
        }

        return improvements;
    }

    // Helper methods for calculations and data processing

    private LocalDateTime[] calculateDateRange(ReportPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case DAILY:
                return new LocalDateTime[]{now.toLocalDate().atStartOfDay(), now};
            case WEEKLY:
                LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1)
                        .toLocalDate().atStartOfDay();
                return new LocalDateTime[]{startOfWeek, now};
            case MONTHLY:
                LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
                return new LocalDateTime[]{startOfMonth, now};
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }
    }

    private LocalDateTime[] calculatePreviousPeriodRange(ReportPeriod period, int periodsBack) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case DAILY:
                LocalDateTime dayStart = now.minusDays(periodsBack).toLocalDate().atStartOfDay();
                return new LocalDateTime[]{dayStart, dayStart.plusDays(1)};
            case WEEKLY:
                LocalDateTime weekStart = now.minusWeeks(periodsBack)
                        .minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
                return new LocalDateTime[]{weekStart, weekStart.plusWeeks(1)};
            case MONTHLY:
                LocalDateTime monthStart = now.minusMonths(periodsBack).withDayOfMonth(1).toLocalDate().atStartOfDay();
                return new LocalDateTime[]{monthStart, monthStart.plusMonths(1)};
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }
    }

    private boolean isSessionInPeriod(DevelopmentSession session, LocalDateTime start, LocalDateTime end) {
        LocalDateTime sessionEnd = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();
        return !session.getStartTime().isAfter(end) && !sessionEnd.isBefore(start);
    }

    private double calculateAverageSessionLength(List<DevelopmentSession> sessions) {
        if (sessions.isEmpty()) return 0.0;
        return sessions.stream()
                .mapToLong(DevelopmentSession::getDurationMinutes)
                .average()
                .orElse(0.0);
    }

    private int findMostProductiveHour(List<DevelopmentSession> sessions) {
        Map<Integer, Long> hourlyProductivity = new HashMap<>();
        
        for (DevelopmentSession session : sessions) {
            int hour = session.getStartTime().getHour();
            hourlyProductivity.merge(hour, session.getDurationMinutes(), Long::sum);
        }
        
        return hourlyProductivity.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(9); // Default to 9 AM
    }

    private ProductivityTrend calculateTrend(String metric, double previous, double current) {
        double change = current - previous;
        double percentChange = previous != 0 ? (change / previous) * 100 : 0;
        
        TrendDirection direction = change > 0 ? TrendDirection.UP : 
                                 change < 0 ? TrendDirection.DOWN : TrendDirection.STABLE;
        
        return ProductivityTrend.builder()
                .metric(metric)
                .previousValue(previous)
                .currentValue(current)
                .change(change)
                .percentChange(percentChange)
                .direction(direction)
                .build();
    }

    private Map<String, List<Double>> calculateMetricTrends(List<ProductivityMetrics> metrics) {
        Map<String, List<Double>> trends = new HashMap<>();
        
        trends.put("productivity", metrics.stream()
                .map(ProductivityMetrics::getOverallProductivityScore)
                .collect(Collectors.toList()));
        
        trends.put("focus", metrics.stream()
                .map(ProductivityMetrics::getFocusScore)
                .collect(Collectors.toList()));
        
        trends.put("velocity", metrics.stream()
                .map(ProductivityMetrics::getCodeVelocity)
                .collect(Collectors.toList()));
        
        return trends;
    }

    private List<String> generateTrendInsights(List<ProductivityMetrics> metrics) {
        List<String> insights = new ArrayList<>();
        
        if (metrics.size() >= 2) {
            ProductivityMetrics latest = metrics.get(0);
            ProductivityMetrics previous = metrics.get(1);
            
            double productivityChange = latest.getOverallProductivityScore() - previous.getOverallProductivityScore();
            if (Math.abs(productivityChange) > 0.1) {
                String direction = productivityChange > 0 ? "improved" : "declined";
                insights.add("Productivity has " + direction + " by " + 
                        String.format("%.1f%%", Math.abs(productivityChange) * 100));
            }
        }
        
        return insights;
    }

    // Chart generation methods (simplified - would integrate with actual charting library)
    
    private List<Map<String, Object>> generateProductivityTimeChart(List<DevelopmentSession> sessions) {
        return sessions.stream()
                .map(session -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("time", session.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    point.put("productivity", session.getProductivityScore());
                    return point;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Long> generateActivityDistributionChart(List<DevelopmentSession> sessions) {
        Map<String, Long> distribution = new HashMap<>();
        
        for (DevelopmentSession session : sessions) {
            for (DevelopmentSession.SessionActivity activity : session.getActivities()) {
                distribution.merge(activity.getType().name(), activity.getDurationMinutes(), Long::sum);
            }
        }
        
        return distribution;
    }

    private List<Map<String, Object>> generateFocusTrendChart(List<DevelopmentSession> sessions) {
        return sessions.stream()
                .map(session -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("time", session.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    point.put("focus", session.getFocusScore());
                    return point;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> generateCodeMetricsChart(ProductivityMetrics metrics) {
        Map<String, Object> chart = new HashMap<>();
        chart.put("linesWritten", metrics.getLinesOfCodeWritten());
        chart.put("linesDeleted", metrics.getLinesOfCodeDeleted());
        chart.put("netLines", metrics.getNetLinesOfCode());
        chart.put("commits", metrics.getCommitsCount());
        chart.put("tests", metrics.getTestsWritten());
        return chart;
    }

    // Export methods
    
    private String exportAsJson(ProductivityReport report) {
        // Would use Jackson ObjectMapper in real implementation
        return "{ \"report\": \"json_export_placeholder\" }";
    }

    private String exportAsCsv(ProductivityReport report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value\n");
        csv.append("Developer ID,").append(report.getDeveloperId()).append("\n");
        csv.append("Period,").append(report.getPeriod()).append("\n");
        csv.append("Productivity Score,").append(report.getMetrics().getOverallProductivityScore()).append("\n");
        csv.append("Lines of Code,").append(report.getMetrics().getNetLinesOfCode()).append("\n");
        csv.append("Focus Score,").append(report.getMetrics().getFocusScore()).append("\n");
        return csv.toString();
    }

    private String exportAsMarkdown(ProductivityReport report) {
        StringBuilder md = new StringBuilder();
        md.append("# Productivity Report\n\n");
        md.append("**Developer:** ").append(report.getDeveloperId()).append("\n");
        md.append("**Period:** ").append(report.getPeriod()).append("\n");
        md.append("**Generated:** ").append(report.getGeneratedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");
        
        md.append("## Summary\n");
        md.append("- **Productivity Score:** ").append(String.format("%.1f%%", report.getMetrics().getOverallProductivityScore() * 100)).append("\n");
        md.append("- **Lines of Code:** ").append(report.getMetrics().getNetLinesOfCode()).append("\n");
        md.append("- **Focus Score:** ").append(String.format("%.1f%%", report.getMetrics().getFocusScore() * 100)).append("\n");
        
        return md.toString();
    }

    // Enums and data classes
    
    public enum ReportPeriod {
        DAILY, WEEKLY, MONTHLY
    }

    public enum ExportFormat {
        JSON, CSV, MARKDOWN
    }

    public enum TrendDirection {
        UP, DOWN, STABLE
    }

    // Data classes would be implemented as separate classes in a real application
    // Simplified here for brevity
    
    public static class ProductivityReport {
        private String developerId;
        private ReportPeriod period;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private ProductivityMetrics metrics;
        private List<DevelopmentSession> sessions;
        private ProductivitySummary summary;
        private List<ProductivityTrend> trends;
        private List<String> recommendations;
        private Map<String, Object> charts;
        private LocalDateTime generatedAt;

        public static Builder builder() { return new Builder(); }
        
        // Getters
        public String getDeveloperId() { return developerId; }
        public ReportPeriod getPeriod() { return period; }
        public LocalDateTime getStartDate() { return startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public ProductivityMetrics getMetrics() { return metrics; }
        public List<DevelopmentSession> getSessions() { return sessions; }
        public ProductivitySummary getSummary() { return summary; }
        public List<ProductivityTrend> getTrends() { return trends; }
        public List<String> getRecommendations() { return recommendations; }
        public Map<String, Object> getCharts() { return charts; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }

        public static class Builder {
            private String developerId;
            private ReportPeriod period;
            private LocalDateTime startDate;
            private LocalDateTime endDate;
            private ProductivityMetrics metrics;
            private List<DevelopmentSession> sessions;
            private ProductivitySummary summary;
            private List<ProductivityTrend> trends;
            private List<String> recommendations;
            private Map<String, Object> charts;
            private LocalDateTime generatedAt;

            public Builder developerId(String developerId) { this.developerId = developerId; return this; }
            public Builder period(ReportPeriod period) { this.period = period; return this; }
            public Builder startDate(LocalDateTime startDate) { this.startDate = startDate; return this; }
            public Builder endDate(LocalDateTime endDate) { this.endDate = endDate; return this; }
            public Builder metrics(ProductivityMetrics metrics) { this.metrics = metrics; return this; }
            public Builder sessions(List<DevelopmentSession> sessions) { this.sessions = sessions; return this; }
            public Builder summary(ProductivitySummary summary) { this.summary = summary; return this; }
            public Builder trends(List<ProductivityTrend> trends) { this.trends = trends; return this; }
            public Builder recommendations(List<String> recommendations) { this.recommendations = recommendations; return this; }
            public Builder charts(Map<String, Object> charts) { this.charts = charts; return this; }
            public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }

            public ProductivityReport build() {
                ProductivityReport report = new ProductivityReport();
                report.developerId = this.developerId;
                report.period = this.period;
                report.startDate = this.startDate;
                report.endDate = this.endDate;
                report.metrics = this.metrics;
                report.sessions = this.sessions;
                report.summary = this.summary;
                report.trends = this.trends;
                report.recommendations = this.recommendations;
                report.charts = this.charts;
                report.generatedAt = this.generatedAt;
                return report;
            }
        }
    }

    public static class DailySummary {
        private String developerId;
        private java.time.LocalDate date;
        private ProductivityMetrics metrics;
        private DevelopmentSession activeSession;
        private int totalActiveTime;
        private double productivityScore;
        private List<String> keyAchievements;
        private List<String> areasForImprovement;

        public static Builder builder() { return new Builder(); }

        // Getters
        public String getDeveloperId() { return developerId; }
        public java.time.LocalDate getDate() { return date; }
        public ProductivityMetrics getMetrics() { return metrics; }
        public DevelopmentSession getActiveSession() { return activeSession; }
        public int getTotalActiveTime() { return totalActiveTime; }
        public double getProductivityScore() { return productivityScore; }
        public List<String> getKeyAchievements() { return keyAchievements; }
        public List<String> getAreasForImprovement() { return areasForImprovement; }

        public static class Builder {
            private String developerId;
            private java.time.LocalDate date;
            private ProductivityMetrics metrics;
            private DevelopmentSession activeSession;
            private int totalActiveTime;
            private double productivityScore;
            private List<String> keyAchievements;
            private List<String> areasForImprovement;

            public Builder developerId(String developerId) { this.developerId = developerId; return this; }
            public Builder date(java.time.LocalDate date) { this.date = date; return this; }
            public Builder metrics(ProductivityMetrics metrics) { this.metrics = metrics; return this; }
            public Builder activeSession(DevelopmentSession activeSession) { this.activeSession = activeSession; return this; }
            public Builder totalActiveTime(int totalActiveTime) { this.totalActiveTime = totalActiveTime; return this; }
            public Builder productivityScore(double productivityScore) { this.productivityScore = productivityScore; return this; }
            public Builder keyAchievements(List<String> keyAchievements) { this.keyAchievements = keyAchievements; return this; }
            public Builder areasForImprovement(List<String> areasForImprovement) { this.areasForImprovement = areasForImprovement; return this; }

            public DailySummary build() {
                DailySummary summary = new DailySummary();
                summary.developerId = this.developerId;
                summary.date = this.date;
                summary.metrics = this.metrics;
                summary.activeSession = this.activeSession;
                summary.totalActiveTime = this.totalActiveTime;
                summary.productivityScore = this.productivityScore;
                summary.keyAchievements = this.keyAchievements;
                summary.areasForImprovement = this.areasForImprovement;
                return summary;
            }
        }
    }

    public static class TrendAnalysis {
        private String developerId;
        private ReportPeriod period;
        private int periodsAnalyzed;
        private List<ProductivityMetrics> metrics;
        private Map<String, List<Double>> trends;
        private List<String> insights;

        public static Builder builder() { return new Builder(); }

        // Getters
        public String getDeveloperId() { return developerId; }
        public ReportPeriod getPeriod() { return period; }
        public int getPeriodsAnalyzed() { return periodsAnalyzed; }
        public List<ProductivityMetrics> getMetrics() { return metrics; }
        public Map<String, List<Double>> getTrends() { return trends; }
        public List<String> getInsights() { return insights; }

        public static class Builder {
            private String developerId;
            private ReportPeriod period;
            private int periodsAnalyzed;
            private List<ProductivityMetrics> metrics;
            private Map<String, List<Double>> trends;
            private List<String> insights;

            public Builder developerId(String developerId) { this.developerId = developerId; return this; }
            public Builder period(ReportPeriod period) { this.period = period; return this; }
            public Builder periodsAnalyzed(int periodsAnalyzed) { this.periodsAnalyzed = periodsAnalyzed; return this; }
            public Builder metrics(List<ProductivityMetrics> metrics) { this.metrics = metrics; return this; }
            public Builder trends(Map<String, List<Double>> trends) { this.trends = trends; return this; }
            public Builder insights(List<String> insights) { this.insights = insights; return this; }

            public TrendAnalysis build() {
                TrendAnalysis analysis = new TrendAnalysis();
                analysis.developerId = this.developerId;
                analysis.period = this.period;
                analysis.periodsAnalyzed = this.periodsAnalyzed;
                analysis.metrics = this.metrics;
                analysis.trends = this.trends;
                analysis.insights = this.insights;
                return analysis;
            }
        }
    }

    public static class ProductivitySummary {
        private int totalSessions;
        private int totalActiveTime;
        private double averageSessionLength;
        private int mostProductiveHour;
        private int totalLinesOfCode;
        private double averageFocusScore;
        private double productivityScore;

        public static Builder builder() { return new Builder(); }

        // Getters
        public int getTotalSessions() { return totalSessions; }
        public int getTotalActiveTime() { return totalActiveTime; }
        public double getAverageSessionLength() { return averageSessionLength; }
        public int getMostProductiveHour() { return mostProductiveHour; }
        public int getTotalLinesOfCode() { return totalLinesOfCode; }
        public double getAverageFocusScore() { return averageFocusScore; }
        public double getProductivityScore() { return productivityScore; }

        public static class Builder {
            private int totalSessions;
            private int totalActiveTime;
            private double averageSessionLength;
            private int mostProductiveHour;
            private int totalLinesOfCode;
            private double averageFocusScore;
            private double productivityScore;

            public Builder totalSessions(int totalSessions) { this.totalSessions = totalSessions; return this; }
            public Builder totalActiveTime(int totalActiveTime) { this.totalActiveTime = totalActiveTime; return this; }
            public Builder averageSessionLength(double averageSessionLength) { this.averageSessionLength = averageSessionLength; return this; }
            public Builder mostProductiveHour(int mostProductiveHour) { this.mostProductiveHour = mostProductiveHour; return this; }
            public Builder totalLinesOfCode(int totalLinesOfCode) { this.totalLinesOfCode = totalLinesOfCode; return this; }
            public Builder averageFocusScore(double averageFocusScore) { this.averageFocusScore = averageFocusScore; return this; }
            public Builder productivityScore(double productivityScore) { this.productivityScore = productivityScore; return this; }

            public ProductivitySummary build() {
                ProductivitySummary summary = new ProductivitySummary();
                summary.totalSessions = this.totalSessions;
                summary.totalActiveTime = this.totalActiveTime;
                summary.averageSessionLength = this.averageSessionLength;
                summary.mostProductiveHour = this.mostProductiveHour;
                summary.totalLinesOfCode = this.totalLinesOfCode;
                summary.averageFocusScore = this.averageFocusScore;
                summary.productivityScore = this.productivityScore;
                return summary;
            }
        }
    }

    public static class ProductivityTrend {
        private String metric;
        private double previousValue;
        private double currentValue;
        private double change;
        private double percentChange;
        private TrendDirection direction;

        public static Builder builder() { return new Builder(); }

        // Getters
        public String getMetric() { return metric; }
        public double getPreviousValue() { return previousValue; }
        public double getCurrentValue() { return currentValue; }
        public double getChange() { return change; }
        public double getPercentChange() { return percentChange; }
        public TrendDirection getDirection() { return direction; }

        public static class Builder {
            private String metric;
            private double previousValue;
            private double currentValue;
            private double change;
            private double percentChange;
            private TrendDirection direction;

            public Builder metric(String metric) { this.metric = metric; return this; }
            public Builder previousValue(double previousValue) { this.previousValue = previousValue; return this; }
            public Builder currentValue(double currentValue) { this.currentValue = currentValue; return this; }
            public Builder change(double change) { this.change = change; return this; }
            public Builder percentChange(double percentChange) { this.percentChange = percentChange; return this; }
            public Builder direction(TrendDirection direction) { this.direction = direction; return this; }

            public ProductivityTrend build() {
                ProductivityTrend trend = new ProductivityTrend();
                trend.metric = this.metric;
                trend.previousValue = this.previousValue;
                trend.currentValue = this.currentValue;
                trend.change = this.change;
                trend.percentChange = this.percentChange;
                trend.direction = this.direction;
                return trend;
            }
        }
    }
}