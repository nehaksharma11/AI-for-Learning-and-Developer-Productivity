package com.ailearning.core.model;

/**
 * Placeholder for CodeChange - to be implemented in later tasks.
 */
public class CodeChange {
    private final String filePath;
    private final String changeType;
    
    public CodeChange(String filePath, String changeType) {
        this.filePath = filePath;
        this.changeType = changeType;
    }
    
    public String getFilePath() { return filePath; }
    public String getChangeType() { return changeType; }
}