package org.nilis.utils.debug;

import java.nio.channels.GatheringByteChannel;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
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
			runtime.gc();
			synchronized (log) {
				log.put(new Date(), runtime.totalMemory() - runtime.freeMemory());
			}
		}
	};
	
	static TimerTask publishInfoTimerTask = new TimerTask() {
		
		@Override
		public void run() {
			synchronized (log) {
				String out = "";
				for(Date time : log.keySet()) {
					out=out+"<ol>"+time+"   "+log.get(time)+"</ol>";
				}
				out="<ul>"+out+"</ul>";
				D.i(HtmlOutputUtils.linearTimedChart(log, "Memory"));
			}
		}
	};
	
	public static void init() {
		gatheringTimer.scheduleAtFixedRate(gatheringTimerTask, 0, 10*1000);
		publishingTimer.scheduleAtFixedRate(publishInfoTimerTask, 1000*5, 1000*20);
	}
}
