package com.ailearning.core.service.ai;

import com.ailearning.core.model.CodeContext;
import com.ailearning.core.model.ProjectContext;
import com.ailearning.core.model.ai.AIBreakdown;
import com.ailearning.core.model.ai.AIExample;
import com.ailearning.core.model.ai.AIExplanation;
import com.ailearning.core.service.ai.impl.FallbackAIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("AIServiceManager Tests")
class AIServiceManagerTest {

    @Mock
    private AIService mockHighPriorityService;
    
    @Mock
    private AIService mockLowPriorityService;
    
    @Mock
    private CodeContext mockCodeContext;
    
    @Mock
    private ProjectContext mockProjectContext;

    private AIServiceManager serviceManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configure mock services
        when(mockHighPriorityService.getPriority()).thenReturn(100);
        when(mockHighPriorityService.getServiceName()).thenReturn("HighPriorityService");
        
        when(mockLowPriorityService.getPriority()).thenReturn(50);
        when(mockLowPriorityService.getServiceName()).thenReturn("LowPriorityService");
        
        List<AIService> services = Arrays.asList(mockLowPriorityService, mockHighPriorityService);
        serviceManager = new AIServiceManager(services, 10, true);
    }

    @Nested
    @DisplayName("Service Management Tests")
    class ServiceManagementTests {

        @Test
        @DisplayName("Should prioritize services correctly")
        void shouldPrioritizeServicesCorrectly() {
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            when(mockLowPriorityService.isAvailable()).thenReturn(true);
            
            AIService preferred = serviceManager.getPreferredService();
            
            assertEquals("HighPriorityService", preferred.getServiceName());
        }

        @Test
        @DisplayName("Should return available services only")
        void shouldReturnAvailableServicesOnly() {
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            when(mockLowPriorityService.isAvailable()).thenReturn(false);
            
            List<AIService> available = serviceManager.getAvailableServices();
            
            assertEquals(1, available.size());
            assertEquals("HighPriorityService", available.get(0).getServiceName());
        }

        @Test
        @DisplayName("Should add new service correctly")
        void shouldAddNewServiceCorrectly() {
            AIService newService = mock(AIService.class);
            when(newService.getPriority()).thenReturn(75);
            when(newService.getServiceName()).thenReturn("NewService");
            
            serviceManager.addService(newService);
            
            // Should be inserted in correct priority order
            when(newService.isAvailable()).thenReturn(true);
            when(mockHighPriorityService.isAvailable()).thenReturn(false);
            when(mockLowPriorityService.isAvailable()).thenReturn(false);
            
            AIService preferred = serviceManager.getPreferredService();
            assertEquals("NewService", preferred.getServiceName());
        }

        @Test
        @DisplayName("Should remove service correctly")
        void shouldRemoveServiceCorrectly() {
            serviceManager.removeService(mockHighPriorityService);
            
            when(mockLowPriorityService.isAvailable()).thenReturn(true);
            
            AIService preferred = serviceManager.getPreferredService();
            assertEquals("LowPriorityService", preferred.getServiceName());
        }

        @Test
        @DisplayName("Should include fallback service when enabled")
        void shouldIncludeFallbackServiceWhenEnabled() {
            List<AIService> services = Arrays.asList(mockHighPriorityService);
            AIServiceManager managerWithFallback = new AIServiceManager(services, 10, true);
            
            List<AIService> available = managerWithFallback.getAvailableServices();
            
            assertTrue(available.stream().anyMatch(s -> s instanceof FallbackAIService));
        }
    }

    @Nested
    @DisplayName("Fallback Mechanism Tests")
    class FallbackMechanismTests {

        @Test
        @DisplayName("Should use high priority service when available")
        void shouldUseHighPriorityServiceWhenAvailable() throws ExecutionException, InterruptedException {
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            when(mockLowPriorityService.isAvailable()).thenReturn(true);
            
            AIExplanation mockExplanation = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("High priority explanation")
                    .serviceProvider("HighPriorityService")
                    .build();
            
            when(mockHighPriorityService.explainCode(anyString(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(mockExplanation));
            
            CompletableFuture<AIExplanation> result = serviceManager.explainCode("test code", mockCodeContext, mockProjectContext);
            AIExplanation explanation = result.get();
            
            assertEquals("High priority explanation", explanation.getExplanation());
            assertEquals("HighPriorityService", explanation.getServiceProvider());
            
            verify(mockHighPriorityService).explainCode(anyString(), any(), any());
            verify(mockLowPriorityService, never()).explainCode(anyString(), any(), any());
        }

        @Test
        @DisplayName("Should fallback to lower priority service when high priority fails")
        void shouldFallbackToLowerPriorityServiceWhenHighPriorityFails() throws ExecutionException, InterruptedException {
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            when(mockLowPriorityService.isAvailable()).thenReturn(true);
            
            when(mockHighPriorityService.explainCode(anyString(), any(), any()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Service failed")));
            
            AIExplanation mockExplanation = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("Low priority explanation")
                    .serviceProvider("LowPriorityService")
                    .build();
            
            when(mockLowPriorityService.explainCode(anyString(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(mockExplanation));
            
            CompletableFuture<AIExplanation> result = serviceManager.explainCode("test code", mockCodeContext, mockProjectContext);
            AIExplanation explanation = result.get();
            
            assertEquals("Low priority explanation", explanation.getExplanation());
            assertEquals("LowPriorityService", explanation.getServiceProvider());
        }

        @Test
        @DisplayName("Should skip unavailable services")
        void shouldSkipUnavailableServices() throws ExecutionException, InterruptedException {
            when(mockHighPriorityService.isAvailable()).thenReturn(false);
            when(mockLowPriorityService.isAvailable()).thenReturn(true);
            
            AIExplanation mockExplanation = AIExplanation.builder()
                    .codeSnippet("test")
                    .explanation("Low priority explanation")
                    .serviceProvider("LowPriorityService")
                    .build();
            
            when(mockLowPriorityService.explainCode(anyString(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(mockExplanation));
            
            CompletableFuture<AIExplanation> result = serviceManager.explainCode("test code", mockCodeContext, mockProjectContext);
            AIExplanation explanation = result.get();
            
            assertEquals("LowPriorityService", explanation.getServiceProvider());
            
            verify(mockHighPriorityService, never()).explainCode(anyString(), any(), any());
            verify(mockLowPriorityService).explainCode(anyString(), any(), any());
        }
    }

    @Nested
    @DisplayName("Operation Tests")
    class OperationTests {

        @Test
        @DisplayName("Should handle explain code operation")
        void shouldHandleExplainCodeOperation() throws ExecutionException, InterruptedException {
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            
            AIExplanation mockExplanation = AIExplanation.builder()
                    .codeSnippet("public void test() {}")
                    .explanation("This is a test method")
                    .serviceProvider("HighPriorityService")
                    .build();
            
            when(mockHighPriorityService.explainCode(anyString(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(mockExplanation));
            
            CompletableFuture<AIExplanation> result = serviceManager.explainCode("public void test() {}", mockCodeContext, mockProjectContext);
            AIExplanation explanation = result.get();
            
            assertNotNull(explanation);
            assertEquals("This is a test method", explanation.getExplanation());
        }

        @Test
        @DisplayName("Should handle generate examples operation")
        void shouldHandleGenerateExamplesOperation() throws ExecutionException, InterruptedException {
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            
            List<AIExample> mockExamples = Arrays.asList(
                    AIExample.builder()
                            .title("Example 1")
                            .description("First example")
                            .codeExample("code1")
                            .serviceProvider("HighPriorityService")
                            .build(),
                    AIExample.builder()
                            .title("Example 2")
                            .description("Second example")
                            .codeExample("code2")
                            .serviceProvider("HighPriorityService")
                            .build()
            );
            
            when(mockHighPriorityService.generateExamples(anyString(), any()))
                    .thenReturn(CompletableFuture.completedFuture(mockExamples));
            
            CompletableFuture<List<AIExample>> result = serviceManager.generateExamples("pattern", mockProjectContext);
            List<AIExample> examples = result.get();
            
            assertNotNull(examples);
            assertEquals(2, examples.size());
            assertEquals("Example 1", examples.get(0).getTitle());
        }

        @Test
        @DisplayName("Should handle create breakdown operation")
        void shouldHandleCreateBreakdownOperation() throws ExecutionException, InterruptedException {
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            
            AIBreakdown mockBreakdown = AIBreakdown.builder()
                    .originalCode("complex code")
                    .overview("Complex code breakdown")
                    .serviceProvider("HighPriorityService")
                    .build();
            
            when(mockHighPriorityService.createBreakdown(anyString(), any()))
                    .thenReturn(CompletableFuture.completedFuture(mockBreakdown));
            
            CompletableFuture<AIBreakdown> result = serviceManager.createBreakdown("complex code", mockCodeContext);
            AIBreakdown breakdown = result.get();
            
            assertNotNull(breakdown);
            assertEquals("Complex code breakdown", breakdown.getOverview());
        }
    }

    @Nested
    @DisplayName("Health Check Tests")
    class HealthCheckTests {

        @Test
        @DisplayName("Should generate health report")
        void shouldGenerateHealthReport() throws ExecutionException, InterruptedException {
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            when(mockLowPriorityService.isAvailable()).thenReturn(false);
            
            CompletableFuture<AIServiceHealthReport> result = serviceManager.checkHealth();
            AIServiceHealthReport report = result.get();
            
            assertNotNull(report);
            assertTrue(report.getTotalServices() >= 2); // At least our two mock services
            assertEquals(1, report.getAvailableServices()); // Only high priority is available
            assertTrue(report.hasAvailableServices());
        }

        @Test
        @DisplayName("Should report service availability correctly")
        void shouldReportServiceAvailabilityCorrectly() {
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            when(mockLowPriorityService.isAvailable()).thenReturn(false);
            
            assertTrue(serviceManager.isAvailable());
            
            when(mockHighPriorityService.isAvailable()).thenReturn(false);
            when(mockLowPriorityService.isAvailable()).thenReturn(false);
            
            // Should still be available due to fallback service
            assertTrue(serviceManager.isAvailable());
        }
    }

    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("Should create default manager")
        void shouldCreateDefaultManager() {
            AIServiceManager defaultManager = AIServiceManager.createDefault();
            
            assertNotNull(defaultManager);
            assertEquals("AIServiceManager", defaultManager.getServiceName());
            assertEquals(1000, defaultManager.getPriority());
            assertTrue(defaultManager.isAvailable()); // Should have fallback service
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle all services failing")
        void shouldHandleAllServicesFailing() {
            // Create manager without fallback
            List<AIService> services = Arrays.asList(mockHighPriorityService, mockLowPriorityService);
            AIServiceManager managerWithoutFallback = new AIServiceManager(services, 10, false);
            
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            when(mockLowPriorityService.isAvailable()).thenReturn(true);
            
            when(mockHighPriorityService.explainCode(anyString(), any(), any()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("High priority failed")));
            when(mockLowPriorityService.explainCode(anyString(), any(), any()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Low priority failed")));
            
            CompletableFuture<AIExplanation> result = managerWithoutFallback.explainCode("test", mockCodeContext, mockProjectContext);
            
            assertThrows(ExecutionException.class, result::get);
        }

        @Test
        @DisplayName("Should handle null services gracefully")
        void shouldHandleNullServicesGracefully() {
            serviceManager.addService(null);
            
            // Should not affect functionality
            when(mockHighPriorityService.isAvailable()).thenReturn(true);
            assertTrue(serviceManager.isAvailable());
        }
    }
}