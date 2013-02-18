package org.nilis.finances;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		
		public PriceTick(String symbol, long timeInSeconds, double ask, double bid) {
			this.symbol = symbol;
			this.time = timeInSeconds;
			this.bid = bid;
			this.ask = bid;
		}
		
		public String toString() {
			return "[Tick: "+ symbol +
					"\n, " + new Date(time).toString() + " ("+time+")" +
					"\n, " + bid +
					"\n, " + ask +
					"]";
		}
	}
	
	public static class PriceTicksBundle {
		protected List<PriceTick> ticks = new LinkedList<PriceTick>();
		protected Map<Long, List<PriceTick>> timedTicks = new HashMap<Long, List<PriceTick>>();
		protected Map<Double, List<PriceTick>> pricedTicks = new HashMap<Double, List<PriceTick>>();
		
		protected Map<Long, DataPair<Double, Double>> timedPrices = new HashMap<Long, DataPair<Double, Double>>();
		
		protected long startTime = 0;
		protected long period = 0;
		public boolean completed;
		
		protected long firstTick = Long.MAX_VALUE;
		protected long lastTick = Long.MIN_VALUE;
		
		protected double openPrice = 0;
		public double getOpenPrice() {
			return openPrice;
		}

		public double getClosePrice() {
			return closePrice;
		}
		
		public boolean bullish() {
			return closePrice > openPrice;
		}

		protected double closePrice = 0;
		
		protected static <TKey> void addToMap(Map<TKey, List<PriceTick>> map, TKey key, PriceTick tick) {
			if(!map.containsKey(key)) {
				map.put(key, new LinkedList<PriceTick>());
			}
			map.get(key).add(tick);
		}
		
		public PriceTicksBundle(long startTime, long period) {
			this(startTime, period, true);
		}
		
		public PriceTicksBundle(long startTime, long period, boolean completed) {
			this.startTime = startTime;
			this.period = period;
			this.completed = completed;
			resetCache();
		}
		
		protected double maxPrice = 0;
		protected double minPrice = Double.MAX_VALUE;
		public long getStartTime() {
			return startTime;
		}

		public long getPeriod() {
			return period;
		}

		public double getMaxPrice() {
			return maxPrice;
		}

		public double getMinPrice() {
			return minPrice;
		}

		
		protected void resetCache() {
			
		}

		public long startTime() {
			return startTime;
		}
		
		public long period() {
			return period;
		}
		
		protected int maxTicksOfTheSamePrice = 0;
		public int maxTicksOfTheSamePrice() {
			return maxTicksOfTheSamePrice;
		}
		
		protected void addToTimedPricesMap(PriceTick tick) {
			if(!timedPrices.containsKey(tick.time)) {
				timedPrices.put(tick.time, new DataPair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE));
			}
			double min = timedPrices.get(tick.time).getTag();
			double max = timedPrices.get(tick.time).getData();
			timedPrices.get(tick.time).setTag(Math.min(min, tick.bid));
			timedPrices.get(tick.time).setData(Math.max(max, tick.bid));
		}
		
		public void addTick(PriceTick tick) {
			maxPrice = Math.max(maxPrice, tick.bid);
			minPrice = Math.min(minPrice, tick.bid);
			if(tick.time < firstTick) {
				openPrice = tick.bid;
				firstTick = tick.time;
			}
			if(tick.time > lastTick) {
				closePrice = tick.bid;
				lastTick = tick.time;
			}
			ticks.add(tick);
			addToMap(timedTicks, tick.time, tick);
			addToMap(pricedTicks, tick.bid, tick);
			addToTimedPricesMap(tick);
			maxTicksOfTheSamePrice = Math.max(maxTicksOfTheSamePrice, countByPrice(tick.bid));
		}
		
		public int countByPrice(double price) {
			if(!pricedTicks.containsKey(price)) {
				return 0;
			}
			return pricedTicks.get(price).size();
		}
		
		public List<PriceTick> ticks() {
			return ticks;
		}
		
		public Set<Double> prices() {
			return pricedTicks.keySet();
		}
		
		public DataPair<Double, Double> priceRangeByTime(long time) {
			return timedPrices.get(time);
		}
		
		public Set<Long> times() {
			return timedPrices.keySet();
		}
		
		public boolean empty() {
			return ticks == null || ticks.size() <= 0;
		}
		
		public String toString() {
			return "[PriceTicksBundle: "+
					"\n, " + new Date(startTime).toString() + " ("+startTime+")" +
					"\n, " + minPrice +
					"\n, " + maxPrice +
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

	public static Map<Long, PriceGroupedTicksBundle> groupTicksByPeriods(long groupPeriod, List<BooksTick> ticks) {
		Map<Long, PriceGroupedTicksBundle> ret = new HashMap<Long, PriceGroupedTicksBundle>();
		if(ticks != null) {
			for(BooksTick tick : ticks) {
				long key = groupPeriod * (tick.time / groupPeriod);
				if(!ret.containsKey(key)) {
					ret.put(key, new PriceGroupedTicksBundle(key, groupPeriod, true));
				}
				ret.get(key).addTick(tick);
			}
		}
		return ret;
	}
	
	public static PriceGroupedTicksBundle packTicksIntoGroup(long startTime, long groupPeriod, List<BooksTick> ticks) {
		PriceGroupedTicksBundle ret = new PriceGroupedTicksBundle(startTime, groupPeriod, true);
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
