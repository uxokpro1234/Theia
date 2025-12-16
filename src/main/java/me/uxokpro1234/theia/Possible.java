package me.uxokpro1234.theia;

/**
 * @author Tigermouthbear
 * 4/13/20
 * */

public class Possible {

    public enum Severity{
        WARN,
        ALERT,
        CHECK
    }
    private final Severity severity;
    private final String description;
    private final String clazz;

    // Constructor
    public Possible(Severity severity, String description, String clazz) {
        this.severity = severity;
        this.description = description;
        this.clazz = clazz;
    }

    // Getters
    public Severity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public String getClazz() {
        return clazz;
    }

    @Override
    public String toString() {
        return "Possible{" +
                "severity=" + severity +
                ", description='" + description + '\'' +
                ", clazz='" + clazz + '\'' +
                '}';
    }
}