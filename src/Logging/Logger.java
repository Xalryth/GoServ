package Logging;

public interface Logger {
    void logError(Exception e);
    void logMessage(String msg);
}
