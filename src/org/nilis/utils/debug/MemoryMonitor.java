package org.nilis.utils.debug;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MemoryMonitor {
	
	private static volatile Map<Date, Long> log = new LinkedHashMap<Date, Long>();
	private static Runtime runtime = Runtime.getRuntime();
	private static Timer gatheringTimer = new Timer(true);
	private static Timer publishingTimer = new Timer(true);
	
	private static long lastMemoryValue = 0;
	private static final long MINIMAL_DELTA_IN_KB_TO_PUBLISH = 6000;
	
	static TimerTask gatheringTimerTask = new TimerTask() {
		
		@Override
		public void run() {
			synchronized (log) {
				if(Math.abs( (runtime.totalMemory() - runtime.freeMemory())/1024 - lastMemoryValue) > MINIMAL_DELTA_IN_KB_TO_PUBLISH) {
					publishInfoTimerTask.run();
				}
			}
		}
	};
	
	static TimerTask publishInfoTimerTask = new TimerTask() {
		
		@Override
		public void run() {
			synchronized (log) {
				lastMemoryValue = (runtime.totalMemory() - runtime.freeMemory())/1024;
				log.put(new Date(), Long.valueOf(Math.round(lastMemoryValue)));
				D.i(HtmlOutputUtils.linearTimedChart(log, "Memory, KB", "memory_use", false));
				log.clear();
			}
		}
	};
	
	public static void init() {
		D.i(HtmlOutputUtils.linearTimedChart(log, "Memory, KB", "memory_use", true));
		gatheringTimer.scheduleAtFixedRate(gatheringTimerTask, 0, 2*1000);
		publishingTimer.scheduleAtFixedRate(publishInfoTimerTask, 1000*5, 1000*300);
	}
}
