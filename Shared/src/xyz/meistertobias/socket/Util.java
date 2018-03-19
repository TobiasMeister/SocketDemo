package xyz.meistertobias.socket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public class Util {
	
	public static final Supplier<String> TIMESTAMP = () -> LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	
	public static final Supplier<String> TIMESTAMP_PRECISE = () -> LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
}
