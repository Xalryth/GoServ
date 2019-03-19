package Logging;

public class ConsoleLogger implements Logger {
    @Override
    public void logError(Exception e) {
        System.out.println(e.getMessage());
        System.out.println(e.getStackTrace());
    }

    @Override
    public void logMessage(String msg) {
        System.out.println(msg);
    }
}
