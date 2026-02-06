package com.ailearning.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Represents a relationship between code elements in the project.
 * Used to model dependencies, inheritance, composition, and other code relationships.
 */
public class Relationship {
    
    public enum RelationshipType {
        DEPENDS_ON, INHERITS_FROM, IMPLEMENTS, USES, CALLS, IMPORTS, CONTAINS, REFERENCES
    }

    @NotBlank
    private final String from;
    
    @NotBlank
    private final String to;
    
    @NotNull
    private final RelationshipType type;
    
    private final String description;
    private final double strength; // 0.0 to 1.0, indicating relationship strength

    @JsonCreator
    public Relationship(
            @JsonProperty("from") String from,
            @JsonProperty("to") String to,
            @JsonProperty("type") RelationshipType type,
            @JsonProperty("description") String description,
            @JsonProperty("strength") double strength) {
        this.from = Objects.requireNonNull(from, "From element cannot be null");
        this.to = Objects.requireNonNull(to, "To element cannot be null");
        this.type = Objects.requireNonNull(type, "Relationship type cannot be null");
        this.description = description;
        this.strength = Math.max(0.0, Math.min(1.0, strength));
    }

    public Relationship(String from, String to, RelationshipType type) {
        this(from, to, type, null, 1.0);
    }

    public static Relationship dependsOn(String from, String to) {
        return new Relationship(from, to, RelationshipType.DEPENDS_ON, "Dependency relationship", 0.8);
    }

    public static Relationship inheritsFrom(String from, String to) {
        return new Relationship(from, to, RelationshipType.INHERITS_FROM, "Inheritance relationship", 1.0);
    }

    public static Relationship uses(String from, String to) {
        return new Relationship(from, to, RelationshipType.USES, "Usage relationship", 0.6);
    }

    public static Relationship calls(String from, String to) {
        return new Relationship(from, to, RelationshipType.CALLS, "Method call relationship", 0.7);
    }

    /**
     * Checks if this relationship is bidirectional (e.g., mutual dependencies)
     */
    public boolean isBidirectional() {
        return type == RelationshipType.USES || type == RelationshipType.REFERENCES;
    }

    /**
     * Checks if this relationship indicates strong coupling
     */
    public boolean isStrongCoupling() {
        return strength > 0.7 && (type == RelationshipType.INHERITS_FROM || 
                                 type == RelationshipType.DEPENDS_ON);
    }

    // Getters
    public String getFrom() { return from; }
    public String getTo() { return to; }
    public RelationshipType getType() { return type; }
    public String getDescription() { return description; }
    public double getStrength() { return strength; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relationship that = (Relationship) o;
        return Objects.equals(from, that.from) &&
                Objects.equals(to, that.to) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, type);
    }

    @Override
    public String toString() {
        return "Relationship{" +
                from + " " + type + " " + to +
                ", strength=" + String.format("%.2f", strength) +
                '}';
    }
}