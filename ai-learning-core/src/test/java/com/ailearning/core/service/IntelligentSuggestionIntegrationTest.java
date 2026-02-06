package com.ailearning.core.service;

import com.ailearning.core.model.*;
import com.ailearning.core.service.impl.DefaultCodeAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the complete intelligent suggestion system.
 * Tests end-to-end functionality of context-aware, pattern-based, and framework-specific suggestions.
 */
class IntelligentSuggestionIntegrationTest {
    
    private CodeAnalyzer codeAnalyzer;
    
    @BeforeEach
    void setUp() {
        codeAnalyzer = new DefaultCodeAnalyzer();
    }
    
    @Test
    @DisplayName("Intelligent Suggestion System - Complete Integration Test")
    void testCompleteIntelligentSuggestionSystem() throws Exception {
        // Test Spring Boot application with multiple improvement opportunities
        String springBootCode = """
            package com.example.userservice;
            
            import org.springframework.beans.factory.annotation.Autowired;
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            @RequestMapping("/api/users")
            public class UserController {
                
                @Autowired
                private UserService userService;
                
                private static String dbPassword = "admin123";
                
                @PostMapping
                public User createUser(@RequestBody User user) {
                    String sql = "INSERT INTO users (name) VALUES ('" + user.getName() + "')";
                    
                    // Complex nested logic
                    if (user != null) {
                        if (user.getName() != null) {
                            if (user.getName().length() > 0) {
                                if (user.getEmail() != null) {
                                    if (user.getEmail().contains("@")) {
                                        return userService.save(user, sql);
                                    }
                                }
                            }
                        }
                    }
                    
                    return null;
                }
                
                @GetMapping
                public List<User> getAllUsers() {
                    ArrayList<User> users = new ArrayList<>();
                    String result = "";
                    
                    for (int i = 0; i < 1000; i++) {
                        result += "User " + i;
                        users.add(new User());
                    }
                    
                    System.out.println(result);
                    return users;
                }
            }
            """;
        
        // Create context for API project
        CodeContext context = CodeContext.builder()
                .fileName("UserController.java")
                .currentFile(springBootCode)
                .projectType("api")
                .projectRoot("/example/userservice")
                .build();
        
        // Create developer profile with skill gaps
        DeveloperProfile developerProfile = createDeveloperProfile();
        
        // Perform comprehensive analysis and suggestion generation
        DefaultCodeAnalyzer analyzer = (DefaultCodeAnalyzer) codeAnalyzer;
        CompletableFuture<List<Suggestion>> suggestionsResult = 
                analyzer.suggestImprovements(context, developerProfile);
        List<Suggestion> suggestions = suggestionsResult.get();
        
        // Validate comprehensive suggestion coverage
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.size() >= 5, "Should provide comprehensive suggestions");
        
        // Validate security suggestions
        boolean hasSecuritySuggestions = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.SECURITY);
        assertTrue(hasSecuritySuggestions, "Should detect security issues and provide suggestions");
        
        // Validate performance suggestions
        boolean hasPerformanceSuggestions = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.PERFORMANCE);
        assertTrue(hasPerformanceSuggestions, "Should provide performance improvement suggestions");
        
        // Validate refactoring suggestions
        boolean hasRefactoringSuggestions = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.REFACTORING);
        assertTrue(hasRefactoringSuggestions, "Should suggest refactoring for complex code");
        
        // Validate framework-specific suggestions
        boolean hasSpringBootSuggestions = suggestions.stream()
                .anyMatch(s -> s.getCategory() != null && s.getCategory().contains("Spring"));
        assertTrue(hasSpringBootSuggestions, "Should provide Spring Boot specific suggestions");
        
        // Validate API project suggestions
        boolean hasApiSuggestions = suggestions.stream()
                .anyMatch(s -> s.getCategory() != null && s.getCategory().contains("API"));
        assertTrue(hasApiSuggestions, "Should provide API project specific suggestions");
        
        // Validate learning suggestions
        boolean hasLearningSuggestions = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.LEARNING);
        assertTrue(hasLearningSuggestions, "Should provide learning suggestions based on profile");
        
        // Validate suggestion quality
        validateSuggestionQuality(suggestions);
        
        System.out.println("✅ Intelligent Suggestion System Integration Test Passed!");
        System.out.println("📊 Suggestion Analysis:");
        printSuggestionSummary(suggestions);
    }
    
    @Test
    @DisplayName("React Application - Framework-Specific Suggestions")
    void testReactFrameworkSuggestions() throws Exception {
        String reactCode = """
            import React, { useState, useEffect } from 'react';
            
            class UserComponent extends React.Component {
                constructor(props) {
                    super(props);
                    this.state = { users: [] };
                }
                
                componentDidMount() {
                    fetch('/api/users')
                        .then(response => response.json())
                        .then(users => this.setState({ users }));
                }
                
                render() {
                    return (
                        <div>
                            {this.state.users.map(user => 
                                <div key={user.id} dangerouslySetInnerHTML={{__html: user.bio}} />
                            )}
                        </div>
                    );
                }
            }
            
            function AnotherComponent() {
                const [data, setData] = useState([]);
                
                useEffect(() => {
                    fetchData().then(setData);
                }, []);
                
                return <div>{data.map(item => <span>{item.name}</span>)}</div>;
            }
            """;
        
        CodeContext context = CodeContext.builder()
                .fileName("UserComponent.jsx")
                .currentFile(reactCode)
                .projectType("web")
                .build();
        
        CompletableFuture<List<Suggestion>> suggestionsResult = codeAnalyzer.suggestImprovements(context);
        List<Suggestion> suggestions = suggestionsResult.get();
        
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // Should suggest converting class component to functional component
        boolean hasClassToFunctionSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Functional Component"));
        assertTrue(hasClassToFunctionSuggestion, "Should suggest converting to functional components");
        
        // Should detect XSS vulnerability
        boolean hasXssSuggestion = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.SECURITY && 
                              s.getDescription().toLowerCase().contains("xss"));
        assertTrue(hasXssSuggestion, "Should detect XSS vulnerability in dangerouslySetInnerHTML");
        
        System.out.println("✅ React Framework Suggestions Test Passed!");
        System.out.println("   - Suggestions: " + suggestions.size());
    }
    
    @Test
    @DisplayName("Python Django - Framework-Specific Suggestions")
    void testDjangoFrameworkSuggestions() throws Exception {
        String djangoCode = """
            from django.db import models
            from django.http import HttpResponse
            
            class User(models.Model):
                name = models.CharField(max_length=100)
                email = models.EmailField()
                
            def user_view(request):
                user_id = request.GET.get('id')
                query = f"SELECT * FROM users WHERE id = {user_id}"
                
                exec(request.POST.get('code', ''))
                
                return HttpResponse("User data")
            """;
        
        CodeContext context = CodeContext.builder()
                .fileName("views.py")
                .currentFile(djangoCode)
                .projectType("web")
                .build();
        
        CompletableFuture<List<Suggestion>> suggestionsResult = codeAnalyzer.suggestImprovements(context);
        List<Suggestion> suggestions = suggestionsResult.get();
        
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // Should suggest adding __str__ method to model
        boolean hasStrMethodSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("String Representation"));
        assertTrue(hasStrMethodSuggestion, "Should suggest adding __str__ method to Django model");
        
        // Should detect security issues
        boolean hasSecuritySuggestions = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.SECURITY);
        assertTrue(hasSecuritySuggestions, "Should detect SQL injection and exec() security issues");
        
        System.out.println("✅ Django Framework Suggestions Test Passed!");
        System.out.println("   - Suggestions: " + suggestions.size());
    }
    
    @Test
    @DisplayName("Architectural Suggestions for Large Project")
    void testArchitecturalSuggestions() throws Exception {
        // Create a project context representing a large application
        ProjectContext projectContext = createLargeProjectContext();
        
        DefaultCodeAnalyzer analyzer = (DefaultCodeAnalyzer) codeAnalyzer;
        CompletableFuture<List<Suggestion>> suggestionsResult = 
                analyzer.generateArchitecturalSuggestions(projectContext);
        List<Suggestion> suggestions = suggestionsResult.get();
        
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // Should suggest architectural patterns
        boolean hasArchitecturalSuggestions = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.ARCHITECTURE);
        assertTrue(hasArchitecturalSuggestions, "Should provide architectural suggestions");
        
        // Should suggest Repository pattern for data access
        boolean hasRepositorySuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Repository"));
        assertTrue(hasRepositorySuggestion, "Should suggest Repository pattern");
        
        // Should suggest Service Layer pattern
        boolean hasServiceLayerSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Service Layer"));
        assertTrue(hasServiceLayerSuggestion, "Should suggest Service Layer pattern");
        
        System.out.println("✅ Architectural Suggestions Test Passed!");
        System.out.println("   - Architectural suggestions: " + suggestions.size());
    }
    
    @Test
    @DisplayName("Learning-Oriented Suggestions Based on Developer Profile")
    void testLearningOrientedSuggestions() throws Exception {
        String complexCode = """
            public class ComplexBusinessLogic {
                public void processOrder(Order order) {
                    // High complexity method with many responsibilities
                    if (order.getItems().size() > 0) {
                        for (OrderItem item : order.getItems()) {
                            if (item.getQuantity() > 0) {
                                if (item.getPrice() > 0) {
                                    if (inventory.isAvailable(item.getProductId())) {
                                        if (payment.process(item.getPrice() * item.getQuantity())) {
                                            shipping.schedule(item);
                                            audit.log("Item processed: " + item.getId());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            """;
        
        CodeContext context = CodeContext.builder()
                .fileName("ComplexBusinessLogic.java")
                .currentFile(complexCode)
                .projectType("library")
                .build();
        
        // Create developer profile with skill gaps
        DeveloperProfile profile = createDeveloperProfileWithSkillGaps();
        
        DefaultCodeAnalyzer analyzer = (DefaultCodeAnalyzer) codeAnalyzer;
        CompletableFuture<List<Suggestion>> suggestionsResult = 
                analyzer.suggestImprovements(context, profile);
        List<Suggestion> suggestions = suggestionsResult.get();
        
        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        
        // Should provide learning suggestions
        boolean hasLearningSuggestions = suggestions.stream()
                .anyMatch(s -> s.getType() == Suggestion.Type.LEARNING);
        assertTrue(hasLearningSuggestions, "Should provide learning suggestions");
        
        // Should suggest learning refactoring techniques
        boolean hasRefactoringLearningSuggestion = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Refactoring") && 
                              s.getType() == Suggestion.Type.LEARNING);
        assertTrue(hasRefactoringLearningSuggestion, 
                "Should suggest learning refactoring for complex code");
        
        // Should suggest design patterns learning
        boolean hasDesignPatternsLearning = suggestions.stream()
                .anyMatch(s -> s.getTitle().contains("Design Patterns"));
        assertTrue(hasDesignPatternsLearning, 
                "Should suggest learning design patterns for low skill level");
        
        System.out.println("✅ Learning-Oriented Suggestions Test Passed!");
        System.out.println("   - Learning suggestions provided based on developer profile");
    }
    
    // Helper methods
    
    private void validateSuggestionQuality(List<Suggestion> suggestions) {
        for (Suggestion suggestion : suggestions) {
            // Validate required fields
            assertNotNull(suggestion.getId(), "Suggestion should have ID");
            assertNotNull(suggestion.getTitle(), "Suggestion should have title");
            assertNotNull(suggestion.getDescription(), "Suggestion should have description");
            assertNotNull(suggestion.getType(), "Suggestion should have type");
            assertNotNull(suggestion.getPriority(), "Suggestion should have priority");
            
            // Validate confidence score
            assertTrue(suggestion.getConfidenceScore() >= 0.0 && suggestion.getConfidenceScore() <= 1.0,
                    "Confidence score should be between 0 and 1");
            
            // Validate actionable suggestions have examples or code snippets
            if (suggestion.isActionable()) {
                assertTrue(suggestion.getExample() != null || suggestion.getCodeExample() != null,
                        "Actionable suggestions should have examples");
            }
            
            // Validate learning suggestions have learn more URLs
            if (suggestion.getType() == Suggestion.Type.LEARNING) {
                assertNotNull(suggestion.getLearnMoreUrl(), 
                        "Learning suggestions should have learn more URLs");
            }
        }
    }
    
    private void printSuggestionSummary(List<Suggestion> suggestions) {
        Map<Suggestion.Type, Long> typeCount = new HashMap<>();
        Map<Suggestion.Priority, Long> priorityCount = new HashMap<>();
        
        for (Suggestion suggestion : suggestions) {
            typeCount.merge(suggestion.getType(), 1L, Long::sum);
            priorityCount.merge(suggestion.getPriority(), 1L, Long::sum);
        }
        
        System.out.println("   - Total suggestions: " + suggestions.size());
        System.out.println("   - By type: " + typeCount);
        System.out.println("   - By priority: " + priorityCount);
        
        long actionableSuggestions = suggestions.stream()
                .mapToLong(s -> s.isActionable() ? 1 : 0)
                .sum();
        System.out.println("   - Actionable suggestions: " + actionableSuggestions);
        
        long learningSuggestions = suggestions.stream()
                .mapToLong(s -> s.isLearningOpportunity() ? 1 : 0)
                .sum();
        System.out.println("   - Learning opportunities: " + learningSuggestions);
    }
    
    private DeveloperProfile createDeveloperProfile() {
        Map<String, SkillLevel> skills = new HashMap<>();
        skills.put("spring-boot", SkillLevel.builder()
                .domain("spring-boot")
                .proficiency(0.7)
                .confidence(0.6)
                .build());
        skills.put("security", SkillLevel.builder()
                .domain("security")
                .proficiency(0.4)
                .confidence(0.3)
                .build());
        
        return DeveloperProfile.builder()
                .id("test-developer")
                .skillLevels(skills)
                .learningPreferences(LearningPreferences.builder()
                        .detailLevel("detailed")
                        .build())
                .build();
    }
    
    private DeveloperProfile createDeveloperProfileWithSkillGaps() {
        Map<String, SkillLevel> skills = new HashMap<>();
        skills.put("design-patterns", SkillLevel.builder()
                .domain("design-patterns")
                .proficiency(0.3) // Low proficiency
                .confidence(0.2)
                .build());
        skills.put("refactoring", SkillLevel.builder()
                .domain("refactoring")
                .proficiency(0.4) // Low proficiency
                .confidence(0.3)
                .build());
        skills.put("testing", SkillLevel.builder()
                .domain("testing")
                .proficiency(0.5) // Medium proficiency
                .confidence(0.4)
                .build());
        
        return DeveloperProfile.builder()
                .id("junior-developer")
                .skillLevels(skills)
                .learningPreferences(LearningPreferences.builder()
                        .detailLevel("detailed")
                        .build())
                .build();
    }
    
    private ProjectContext createLargeProjectContext() {
        List<FileNode> files = List.of(
                FileNode.builder().name("UserController.java").path("src/main/java/controller/UserController.java").build(),
                FileNode.builder().name("OrderController.java").path("src/main/java/controller/OrderController.java").build(),
                FileNode.builder().name("UserService.java").path("src/main/java/service/UserService.java").build(),
                FileNode.builder().name("OrderService.java").path("src/main/java/service/OrderService.java").build(),
                FileNode.builder().name("UserData.java").path("src/main/java/data/UserData.java").build(),
                FileNode.builder().name("OrderData.java").path("src/main/java/data/OrderData.java").build(),
                FileNode.builder().name("DatabaseConfig.java").path("src/main/java/config/DatabaseConfig.java").build(),
                FileNode.builder().name("SecurityConfig.java").path("src/main/java/config/SecurityConfig.java").build(),
                FileNode.builder().name("User.java").path("src/main/java/model/User.java").build(),
                FileNode.builder().name("Order.java").path("src/main/java/model/Order.java").build(),
                FileNode.builder().name("UserRepository.java").path("src/main/java/repository/UserRepository.java").build()
        );
        
        ProjectStructure structure = ProjectStructure.builder()
                .files(files)
                .build();
        
        return ProjectContext.builder()
                .id("large-project")
                .structure(structure)
                .build();
    }
}