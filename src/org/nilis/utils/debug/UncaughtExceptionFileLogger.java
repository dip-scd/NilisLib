package org.nilis.utils.debug;

import java.lang.Thread.UncaughtExceptionHandler;

public class UncaughtExceptionFileLogger implements UncaughtExceptionHandler {
	
	private UncaughtExceptionHandler defaultUEH;
	
	public UncaughtExceptionFileLogger() {
		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		D.enableDebug();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable throwable) {
		D.logToFile(throwable);
		
		defaultUEH.uncaughtException(thread, throwable);
	}

}
