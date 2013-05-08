package org.nilis.finances;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.nilis.utils.data.DataPair;

public class FinancialData {
	public static long roundToSeconds(long timestamp) {
		return 1000 * (timestamp / 1000);
	}
	
	public static class PriceTick {
		public String symbol;
		public long time;
		public double bid;
		public double ask;
		
		public PriceTick(PriceTick tick) {
			this.symbol = tick.symbol;
			this.time = tick.time;
			this.bid = tick.bid;
			this.ask = tick.ask;
		}
		
		public PriceTick(String symbol, long timeInSeconds, double ask, double bid) {
			this.symbol = symbol;
			this.time = timeInSeconds;
			this.bid = bid;
			this.ask = ask;
		}
		
		public String toString() {
			return "[Tick: "+ symbol +
					"\n, " + new Date(time).toString() + " ("+time+")" +
					"\n, " + bid +
					"\n, " + ask +
					"]";
		}
	}
	
	public static class BooksTick {
		public String symbol;
		public long time;
		public double price;
		public double volume;
		public int direction;
		
		public BooksTick(String symbol, long timeInSeconds, double price, double volume, int direction) {
			this.symbol = symbol;
			this.time = timeInSeconds;
			this.price = price;
			this.volume = volume;
			this.direction = direction;
		}
		
		public String toString() {
			return "[Tick: "+ symbol +
					"\n, " + new Date(time).toString() + " ("+time+")" +
					"\n, " + price +
					"\n, " + volume +
					"\n, " + (direction == 1 ? "BUY" : "SELL") +
					"]";
		}
	}

	public static Map<Long, BookTicksBundle> groupTicksByPeriods(long groupPeriod, List<BooksTick> ticks) {
		Map<Long, BookTicksBundle> ret = new HashMap<Long, BookTicksBundle>();
		if(ticks != null) {
			for(BooksTick tick : ticks) {
				long key = groupPeriod * (tick.time / groupPeriod);
				if(!ret.containsKey(key)) {
					ret.put(key, new BookTicksBundle(key, groupPeriod, true));
				}
				ret.get(key).addTick(tick);
			}
		}
		return ret;
	}
	
	public static BookTicksBundle packTicksIntoGroup(long startTime, long groupPeriod, List<BooksTick> ticks) {
		BookTicksBundle ret = new BookTicksBundle(startTime, groupPeriod, true);
		if(ticks != null) {
			for(BooksTick tick : ticks) {
				ret.addTick(tick);
			}
		}
		return ret;
	}
	
	public static Map<Long, PriceTicksBundle> groupPriceTicksByPeriods(long groupPeriod, List<PriceTick> ticks) {
		Map<Long, PriceTicksBundle> ret = new HashMap<Long, PriceTicksBundle>();
		if(ticks != null) {
			for(PriceTick tick : ticks) {
				long key = groupPeriod * (tick.time / groupPeriod);
				if(!ret.containsKey(key)) {
					ret.put(key, new PriceTicksBundle(key, groupPeriod, true));
				}
				ret.get(key).addTick(tick);
			}
		}
		return ret;
	}
	
	public static PriceTicksBundle packPriceTicksIntoGroup(long startTime, long groupPeriod, List<PriceTick> ticks) {
		PriceTicksBundle ret = new PriceTicksBundle(startTime, groupPeriod, true);
		if(ticks != null) {
			for(PriceTick tick : ticks) {
				ret.addTick(tick);
			}
		}
		return ret;
	}
	
	public static double pointSizeOfTheSymbol(String symbol) {
		return 0.00001;
	}

	public static class TimeRange extends DataPair<Long, Long> {
		public TimeRange(long start, long end) {
			super(start, end);
		}

		public Long start() {
			return getTag();
		}
		
		public Long end() {
			return getData();
		}
		
		public String toString() {
			return "TimeRange: "+new Date(start())+" .. "+new Date(end())+"\n";
		}
		
		public long duration() {
			return end() - start();
		}
	};
}
