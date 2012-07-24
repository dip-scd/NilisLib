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
	private static final long MINIMAL_DELTA_IN_KB_TO_PUBLISH = 100;
	
	private static String memoryLegend = "Memory, KB";
	private static String chartTag = "memory_use";
	
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
				D.i(HtmlOutputUtils.linearTimedChart(log, memoryLegend, chartTag, false));
				log.clear();
			}
		}
	};
	
	
	public static void init() {
		D.i(HtmlOutputUtils.linearTimedChart(log, memoryLegend, chartTag, true));
		gatheringTimer.scheduleAtFixedRate(gatheringTimerTask, 0, 2*1000);
		publishingTimer.scheduleAtFixedRate(publishInfoTimerTask, 1000*5, 1000*300);
	}
	
	private static Map<String, Map<Date, Long>> memoryDeltas = new LinkedHashMap<String, Map<Date,Long>>();

	private static long lastMeasuredMemory = 0;
	public static void logMemory(String memoryLogId) {
		logMemory(memoryLogId, true);
	}
	
	private static String memoryLegentPrefix = "Memory (";
	private static String memoryLegentPostfix = "), KB";
	public static void logMemory(String memoryLogId, boolean submitLog) {
		runtime.gc();
		lastMeasuredMemory = (runtime.totalMemory() - runtime.freeMemory())/1024;
		boolean clearChart = false;
		if(!memoryDeltas.containsKey(memoryLogId)) {
			clearChart = true;
			memoryDeltas.put(memoryLogId, new LinkedHashMap<Date, Long>());
		}
		memoryDeltas.get(memoryLogId).put(new Date(), lastMeasuredMemory);
		if(submitLog || memoryDeltas.get(memoryLogId).size() > 60) {
			D.i(HtmlOutputUtils.linearTimedChart(memoryDeltas.get(memoryLogId), memoryLegentPrefix+memoryLogId+memoryLegentPostfix,
					memoryLogId, clearChart));
			memoryDeltas.get(memoryLogId).clear();
		}
	}
}
