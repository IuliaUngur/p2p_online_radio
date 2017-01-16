package utils;

public class MessageHandler {

	public enum LogType {
		debug,
		info,
		warning
	}

	public static void log(LogType type, String message)
	{
		System.out.println(message);
	}
}
