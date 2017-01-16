package utils.log;

import javax.swing.event.EventListenerList;


public class LogHandler {

	/**
	 * @author albrecht
	 *
	 */
	
	/**
	 * log levels 
	 */
	public enum LogType {
		debug,
		info,
		warning
	}
	
	/**
	 * list of listeners
	 */
    private static EventListenerList listenerList = new EventListenerList();

    /**
	 * log a message with level warning
	 * @param sender the instance of the sender
	 * @param message the message to log
	 */
	public static void warning(Object sender, String message)
	{
		if(sender == null) {
			sender = new Object(); // this is a hack, but better than not logging at all.
			System.err.println("warning: sender of a log message cannot be null.");
		}
		System.err.println(sender.getClass() + ": " + message);
		fireLogEvent(new LogEvent(sender, LogType.warning, message));
	}

	/**
	 * log a message with level info
	 * @param sender the instance of the sender
	 * @param message the message to log
	 */
	public static void info(Object sender, String message) {
		if(sender == null) {
			sender = new Object(); // this is a hack, but better than not logging at all.
			System.err.println("warning: sender of a log message cannot be null.");
		}
		System.out.println(sender.getClass() + ": " + message);
		fireLogEvent(new LogEvent(sender, LogType.info, message));
	}

	/**
	 * log a message with level debug
	 * @param sender the instance of the sender
	 * @param message the message to log
	 */
	public static void debug(Object sender, String message) {
		if(sender == null) {
			sender = new Object(); // this is a hack, but better than not logging at all.
			System.err.println("warning: sender of a log message cannot be null.");
		}
		System.out.println(sender.getClass() + ": " + message);
		fireLogEvent(new LogEvent(sender, LogType.debug, message));
	}

    /**
     * This methods allows classes to register for log events
     * @param listener
     */
    public static void addLogEventListener(LogEventListener listener) {
        listenerList.add(LogEventListener.class, listener);
    }

    /**
     * This methods allows classes to unregister for log events
     * @param listener
     */
    public static void removeLogEventListener(LogEventListener listener) {
        listenerList.remove(LogEventListener.class, listener);
    }

    /**
     * fire log events, i.e. notify all listeners (iteration)
     * @param evt
     */
    public static void fireLogEvent(LogEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        // Each listener occupies two elements - the first is the listener class
        // and the second is the listener instance
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == LogEventListener.class) {
                ((LogEventListener)listeners[i+1]).logMessageOccured(evt);
            }
        }
    }
	
}


