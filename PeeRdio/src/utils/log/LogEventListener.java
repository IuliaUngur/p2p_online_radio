package utils.log;

import java.util.EventListener;

public interface LogEventListener extends EventListener {
    public void logMessageOccured(LogEvent evt);
}
