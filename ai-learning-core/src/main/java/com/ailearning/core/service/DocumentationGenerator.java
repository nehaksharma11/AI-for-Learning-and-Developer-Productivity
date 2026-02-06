package com.ailearning.core.service;

import com.ailearning.core.model.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service interface for generating and managing code documentation.
 * 
 * Provides capabilities for:
 * - Template-based documentation generation
 * - Multi-language comment generation (Javadoc, JSDoc, Python docstrings)
 * - Markdown documentation creation
 * - Documentation synchronization and updates
 * - Validation and quality assurance
 */
public interface DocumentationGenerator {
    
    /**
     * Generates inline comments for the given code.
     * 
     * @param code the source code to document
     * @param language the programming language
     * @param filePath the file path for context
     * @return future containing the documented code with inline comments
     */
    CompletableFuture<String> generateInlineComments(String code, String language, String filePath);
    
    /**
     * Creates API documentation for a code module.
     * 
     * @param moduleContext the module context containing code structure
     * @param format the desired output format
     * @return future containing the generated API documentation
     */
    CompletableFuture<Documentation> createAPIDocumentation(CodeContext moduleContext, 
                                                           Documentation.Format format);
    
    /**
     * Updates documentation based on code changes.
     * 
     * @param changes the list of code changes
     * @param existingDocs the existing documentation to update
     * @return future containing the list of documentation updates
     */
    CompletableFuture<List<DocumentationUpdate>> updateDocumentation(List<CodeChange> changes, 
                                                                    List<Documentation> existingDocs);
    
    /**
     * Validates documentation against the corresponding code.
     * 
     * @param documentation the documentation to validate
     * @param code the corresponding source code
     * @param language the programming language
     * @return future containing the validation result
     */
    CompletableFuture<ValidationResult> validateDocumentation(Documentation documentation, 
                                                             String code, String language);
    
    /**
     * Generates documentation for a specific code element (class, method, function).
     * 
     * @param elementName the name of the code element
     * @param elementType the type of the code element (class, method, function, etc.)
     * @param code the source code containing the element
     * @param language the programming language
     * @param filePath the file path for context
     * @return future containing the generated documentation
     */
    CompletableFuture<Documentation> generateElementDocumentation(String elementName, 
                                                                 String elementType,
                                                                 String code, 
                                                                 String language, 
                                                                 String filePath);
    
    /**
     * Creates markdown documentation for a project or module.
     * 
     * @param projectContext the project context
     * @param template the markdown template to use
     * @return future containing the generated markdown documentation
     */
    CompletableFuture<Documentation> createMarkdownDocumentation(ProjectContext projectContext, 
                                                                String template);
    
    /**
     * Synchronizes documentation with code changes to maintain consistency.
     * 
     * @param filePath the file path that changed
     * @param oldCode the previous version of the code
     * @param newCode the new version of the code
     * @param language the programming language
     * @return future containing the list of synchronization updates
     */
    CompletableFuture<List<DocumentationUpdate>> synchronizeDocumentation(String filePath, 
                                                                         String oldCode, 
                                                                         String newCode, 
                                                                         String language);
    
    /**
     * Gets available documentation templates for a specific language.
     * 
     * @param language the programming language
     * @param elementType the type of code element (optional)
     * @return list of available templates
     */
    List<String> getAvailableTemplates(String language, String elementType);
    
    /**
     * Checks if documentation exists for a specific code element.
     * 
     * @param filePath the file path
     * @param elementName the element name
     * @param elementType the element type
     * @return true if documentation exists, false otherwise
     */
    boolean hasDocumentation(String filePath, String elementName, String elementType);
    
    /**
     * Gets documentation statistics for a project or file.
     * 
     * @param projectContext the project context (optional, can be null for file-level stats)
     * @param filePath the specific file path (optional, can be null for project-level stats)
     * @return documentation statistics
     */
    DocumentationStats getDocumentationStats(ProjectContext projectContext, String filePath);
    
    /**
     * Represents documentation statistics.
     */
    final class DocumentationStats {
        private final int totalElements;
        private final int documentedElements;
        private final int outdatedDocuments;
        private final double coveragePercentage;
        private final double averageAccuracy;
        
        public DocumentationStats(int totalElements, int documentedElements, int outdatedDocuments, 
                                double coveragePercentage, double averageAccuracy) {
            this.totalElements = totalElements;
            this.documentedElements = documentedElements;
            this.outdatedDocuments = outdatedDocuments;
            this.coveragePercentage = coveragePercentage;
            this.averageAccuracy = averageAccuracy;
        }
        
        public int getTotalElements() { return totalElements; }
        public int getDocumentedElements() { return documentedElements; }
        public int getOutdatedDocuments() { return outdatedDocuments; }
        public double getCoveragePercentage() { return coveragePercentage; }
        public double getAverageAccuracy() { return averageAccuracy; }
        
        public boolean hasGoodCoverage() { return coveragePercentage >= 80.0; }
        public boolean hasHighAccuracy() { return averageAccuracy >= 0.8; }
        
        @Override
        public String toString() {
            return String.format("DocumentationStats{coverage=%.1f%%, accuracy=%.2f, documented=%d/%d}", 
                    coveragePercentage, averageAccuracy, documentedElements, totalElements);
        }
    }
}