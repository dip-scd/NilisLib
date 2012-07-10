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
	
	static TimerTask gatheringTimerTask = new TimerTask() {
		
		@Override
		public void run() {
			//runtime.gc();
			synchronized (log) {
				log.put(new Date(), Long.valueOf(Math.round((runtime.totalMemory() - runtime.freeMemory())/1024)));
			}
		}
	};
	
	static TimerTask publishInfoTimerTask = new TimerTask() {
		
		@Override
		public void run() {
			synchronized (log) {
				D.i(HtmlOutputUtils.linearTimedChart(log, "Memory, KB", "memory_use", false));
				log.clear();
			}
		}
	};
	
	public static void init() {
		D.i(HtmlOutputUtils.linearTimedChart(log, "Memory, KB", "memory_use", true));
		gatheringTimer.scheduleAtFixedRate(gatheringTimerTask, 0, 3*1000);
		publishingTimer.scheduleAtFixedRate(publishInfoTimerTask, 1000*5, 1000*8);
	}
}
