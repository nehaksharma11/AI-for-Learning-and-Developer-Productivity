# AI Learning Companion

An intelligent system that integrates with IDEs to provide context-aware learning assistance and productivity enhancement.

**Product Link**: [View Live Demo](https://your-deployment-url.com) *(Update with your deployment URL)*

**GitHub Repository**: [https://github.com/nehaksharma11/AI-for-Learning-and-Developer-Productivity](https://github.com/nehaksharma11/AI-for-Learning-and-Developer-Productivity)

## Project Structure

This is a multi-module Maven project with the following structure:

```
ai-learning-companion/
├── pom.xml                     # Root POM with dependency management
├── ai-learning-core/           # Core domain models and interfaces
├── ai-learning-lsp/            # LSP integration layer (planned)
├── ai-learning-analyzer/       # Code analysis components (planned)
├── ai-learning-learning/       # Learning path generation (planned)
├── ai-learning-docs/           # Documentation generation (planned)
├── ai-learning-ai/             # AI/ML integration (planned)
└── ai-learning-app/            # Main application (planned)
```

## Current Status

✅ **Task 1 Completed**: Set up project structure and core interfaces

### What's Been Implemented

1. **Maven Multi-Module Structure**
   - Root POM with dependency management
   - Core module with proper dependencies
   - LSP module with Eclipse LSP4J integration
   - Testing framework configuration (JUnit 5, Mockito, jqwik)

2. **Core Domain Models**
   - `DeveloperProfile` - Tracks developer skills, preferences, and history
   - `SkillLevel` - Represents proficiency in specific domains
   - `LearningPreferences` - Configures personalized learning experience
   - `WorkSession` - Tracks productivity metrics
   - `Achievement` - Represents learning milestones
   - `LearningGoal` - Manages learning objectives

3. **Project Context and Code Analysis Models**
   - `ProjectContext` - Maintains comprehensive codebase understanding
   - `ProjectStructure` - Represents file hierarchy and module organization
   - `ModuleDefinition` - Defines logical code groupings with dependencies
   - `Relationship` - Models code relationships (inheritance, dependencies, etc.)
   - `ComplexityMetrics` - Tracks cyclomatic, cognitive, and structural complexity
   - `AnalysisResult` - Contains findings from static code analysis
   - `CodeIssue` - Represents bugs, security issues, and code smells
   - `Suggestion` - Provides improvement recommendations
   - `CodePattern` - Detects design patterns and anti-patterns
   - `CodingConvention` - Enforces project coding standards
   - `Dependency` - Manages external and internal dependencies

4. **LSP Integration Layer**
   - `AILearningLanguageServer` - Main LSP server implementation with full protocol support
   - `AILearningTextDocumentService` - Handles document lifecycle, hover, completion, code actions
   - `AILearningWorkspaceService` - Manages workspace operations, configuration, file watching
   - `LSPServerLauncher` - Server launcher with stdio and socket communication support
   - JSON-RPC communication infrastructure with Eclipse LSP4J
   - Standard LSP methods (initialize, hover, completion, diagnostics, code actions)
   - **Custom AI Learning Extensions**:
     - **Contextual Code Explanations**: Intelligent hover provider with language-specific explanations
     - **Learning-Focused Completions**: Smart code completion with educational context
     - **Interactive Code Actions**: Right-click actions for learning and improvement
     - **Custom Commands**: 10+ specialized commands for code analysis and learning

5. **Core Service Interfaces**
   - `ContextEngine` - Real-time codebase analysis and understanding
   - `CodeAnalyzer` - Static analysis and intelligent suggestions

6. **Configuration**
   - Application properties with performance settings
   - LSP-specific configuration with capabilities and feature toggles
   - Logging configuration with structured output
   - Test configuration for different environments

7. **Comprehensive Testing**
   - Unit tests for all domain models and LSP services
   - Property-based tests for adaptive learning (jqwik)
   - JSON serialization tests for caching support
   - Integration tests for complete LSP workflows
   - Test coverage for complex scenarios and edge cases

8. **AST Parsing and Analysis Components**
   - `MultiLanguageASTParser` - Unified interface for parsing multiple programming languages
   - `JavaASTParser` - Java-specific AST parser using JavaParser library
   - `JavaScriptASTParser` - JavaScript/TypeScript parser using Rhino
   - `PythonASTParser` - Python parser with basic regex-based parsing
   - `DependencyGraphBuilder` - Constructs dependency graphs from AST nodes
   - **AST Model Classes**: `ASTNode`, `ClassNode`, `MethodNode`, `VariableNode`, `ExpressionNode`, `StatementNode`
   - **Parse Results**: `ParseResult`, `ParseError`, `ParseWarning`, `ParseMetrics`, `SourceLocation`

### Key Features Implemented

- **Immutable Domain Models**: All core models are immutable with builder patterns
- **Comprehensive Code Analysis**: Full static analysis with complexity metrics, issue detection, and suggestions
- **Project Understanding**: Deep codebase context with relationships, patterns, and conventions
- **Advanced LSP Integration**: Custom learning extensions with intelligent features
- **Contextual Learning**: Real-time code explanations and educational hover information
- **Smart Completions**: Learning-focused code completion with best practices
- **Interactive Code Actions**: Right-click menu with 10+ learning and improvement actions
- **Multi-Language AST Parsing**: Java, JavaScript/TypeScript, Python with extensible architecture
- **Dependency Graph Analysis**: Automatic detection of code relationships and dependencies
- **Incremental Parsing**: Real-time AST updates for performance (foundation implemented)
- **Comprehensive Parse Results**: Detailed error reporting, warnings, and metrics
- **Source Location Tracking**: Precise code position information for all AST nodes
- **Security Analysis**: Built-in security vulnerability detection and recommendations
- **Performance Insights**: Code performance analysis and optimization suggestions
- **Documentation Generation**: Automated documentation with learning context
- **Refactoring Assistance**: Extract method, simplify code, and other refactoring suggestions
- **Validation**: Input validation using Jakarta Bean Validation
- **JSON Serialization**: Jackson annotations for caching and persistence support
- **Performance Monitoring**: Built-in metrics and logging
- **Type Safety**: Strong typing with proper null checks
- **Property-Based Testing**: Validates correctness across all possible inputs

## Prerequisites

To build and run this project, you need:

- Java 17 or higher
- Maven 3.6+ or Gradle 7+
- IDE with Java support (IntelliJ IDEA, Eclipse, VS Code)

## Building the Project

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package
```

## Next Steps

✅ **Task 6.2 Completed**: Add intelligent suggestion system

The intelligent suggestion system is now complete! Building upon the static analysis foundation, the system now provides:

### 🧠 **Intelligent Suggestion Engine**
- **Context-Aware Recommendations**: Suggestions based on project type, file type, and codebase context
- **Pattern-Based Suggestions**: Recommendations derived from detected code patterns and anti-patterns
- **Framework-Specific Guidance**: Tailored suggestions for Spring Boot, React, Angular, Django, Express.js
- **Learning-Oriented Recommendations**: Personalized suggestions based on developer skill levels
- **Adaptive Prioritization**: Smart filtering and prioritization to avoid overwhelming users

### 🎯 **Context-Aware Recommendations**
- **Project Type Awareness**: Different suggestions for web, API, library, and microservice projects
- **File Type Intelligence**: Specialized suggestions for test files, configuration files, and utility classes
- **Architectural Guidance**: Repository pattern, Service Layer, MVC architecture recommendations
- **Security Context**: Project-specific security recommendations (CSRF for web, rate limiting for APIs)

### 🏗️ **Pattern-Based Intelligence**
- **Anti-Pattern Detection**: God Class, Callback Hell, Magic Numbers with refactoring suggestions
- **Design Pattern Optimization**: Singleton thread safety, Builder pattern improvements
- **Performance Pattern Recognition**: N+1 queries, inefficient loops, memory leak detection
- **Architectural Pattern Suggestions**: Missing patterns detection and implementation guidance

### 🚀 **Framework-Specific Expertise**
- **Spring Boot**: Constructor injection, validation, transaction management, security best practices
- **React**: Functional component conversion, useCallback optimization, performance improvements
- **Angular**: Lifecycle hooks, OnDestroy implementation, component best practices
- **Django**: Model string representation, security practices, ORM optimization
- **Express.js**: Error handling, middleware patterns, security implementations

### 📚 **Learning-Oriented Recommendations**
- **Skill Gap Analysis**: Identifies learning opportunities based on developer profile
- **Progressive Learning**: Suggests appropriate learning resources based on current skill level
- **Code Complexity Learning**: Refactoring techniques for complex code
- **Security Awareness**: Secure coding practices based on detected vulnerabilities
- **Best Practice Education**: Links to authoritative resources and documentation

### ⚡ **Advanced Features**
- **Refactoring Intelligence**: Extract method, reduce nesting, split large classes
- **Architectural Suggestions**: Cross-file pattern analysis and system-wide recommendations
- **Confidence Scoring**: Each suggestion includes confidence level for reliability
- **Actionable Examples**: Concrete code examples and implementation guidance
- **Smart Filtering**: Limits suggestions to prevent information overload

### 🎛️ **Adaptive Behavior**
- **Developer Profile Integration**: Adjusts suggestions based on skill levels and preferences
- **Priority-Based Sorting**: Critical security issues first, then high-impact improvements
- **Contextual Relevance**: Considers current work context and project conventions
- **Learning Preference Adaptation**: Detailed vs. concise suggestions based on user preferences

The intelligent suggestion system transforms static analysis results into actionable, educational, and contextually relevant recommendations that help developers improve their code quality, learn new skills, and follow best practices.

The next task to implement is **Task 6.3: Write property test for context-aware suggestions** to validate the correctness of the intelligent suggestion system.

## Custom AI Learning Features

The LSP server now includes comprehensive learning extensions:

### 🧠 Contextual Explanations
- **Smart Hover**: Hover over any code element for intelligent explanations
- **Language-Aware**: Tailored explanations for Java, JavaScript/TypeScript, Python
- **Learning Tips**: Each explanation includes personalized learning recommendations

### 💡 Intelligent Completions  
- **Best Practice Suggestions**: Code completions that teach good practices
- **Pattern-Based**: Common design patterns and code templates
- **Documentation Templates**: Auto-generate proper documentation formats
- **Learning Context**: Every suggestion includes educational value

### ⚡ Interactive Code Actions
Right-click on any code to access:
- **🧠 Explain this code**: Detailed explanations with learning context
- **📚 Show similar examples**: Find related code patterns and examples  
- **🎯 Create learning path**: Generate personalized learning roadmap
- **⚡ Suggest improvements**: Performance and readability enhancements
- **🚀 Analyze performance**: Time/space complexity analysis
- **🔒 Security analysis**: Vulnerability detection and recommendations
- **📝 Generate documentation**: Comprehensive documentation generation
- **💬 Add explanatory comments**: Inline code explanations
- **🔧 Extract method**: Refactoring suggestions
- **🎯 Simplify expression**: Code simplification recommendations

### 🎓 Learning-First Design
- **Educational Focus**: Every feature designed to enhance learning
- **Progressive Complexity**: Adapts to developer skill level
- **Best Practices**: Promotes industry standards and conventions
- **Multi-Language**: Consistent experience across programming languages

## Architecture Highlights

- **Layered Architecture**: Clear separation between domain, service, and integration layers
- **Event-Driven**: Asynchronous processing for real-time requirements
- **Extensible**: Plugin architecture for supporting multiple IDEs and languages
- **Performance-First**: Sub-500ms response times for context updates
- **Privacy-Aware**: Local processing with optional cloud integration

## Requirements Addressed

This implementation addresses the foundational requirements:
- Context-aware IDE integration (interfaces defined)
- Real-time performance requirements (timeout configurations)
- Multi-language support (extensible architecture)
- Developer productivity tracking (WorkSession model)
- Personalized learning (DeveloperProfile and LearningPreferences)

For detailed requirements and design specifications, see:
- [Requirements Document](.kiro/specs/ai-learning-companion/requirements.md)
- [Design Document](.kiro/specs/ai-learning-companion/design.md)
- [Implementation Tasks](.kiro/specs/ai-learning-companion/tasks.md)