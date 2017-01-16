package utils.log;

import java.util.EventObject;

import utils.log.LogHandler.LogType;

public class LogEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private String message;
	private LogType logType;
	
	public LogEvent(Object source, LogType logType, String message) {
        super(source);
        this.logType = logType;
        this.message = message;
    }
	
	public String getMessage() {
		return message;
	}
	
	public LogType getLogType() {
		return logType;
	}
}
