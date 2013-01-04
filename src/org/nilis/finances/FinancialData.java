package org.nilis.finances;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nilis.utils.data.DataPair;

public class FinancialData {
	public static long roundToSeconds(long timestamp) {
		return 1000 * (timestamp / 1000);
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

	public static class PriceGroupedTicksBundle extends HashMap<Double, List<BooksTick>> {
		private static final long serialVersionUID = -2190416372504052471L;
		protected long startTime = 0;
		protected long period = 0;
		public boolean completed;
		
		public PriceGroupedTicksBundle(long startTime, long period, boolean completed) {
			this.startTime = startTime;
			this.period = period;
			this.completed = completed;
			resetCache();
		}
		
		public long startTime() {
			return startTime;
		}
		
		public long period() {
			return period;
		}
		
		public void addTick(BooksTick tick) {
			if(!containsKey(tick.price)) {
				put(tick.price, new LinkedList<BooksTick>());
			}
			get(tick.price).add(tick);
			resetCache();
		}
		
		public double reducedVolumeByPrice(double price) {
			double ret = 0;
			if(containsKey(price)) {
				for(BooksTick tick : get(price)) {
					ret += tick.volume * tick.direction;
				}
			}
			return ret;
		}
		
		public double fullVolumeByPrice(double price) {
			double ret = 0;
			if(containsKey(price)) {
				for(BooksTick tick : get(price)) {
					ret += tick.volume;
				}
			}
			return ret;
		}
		
		public double buyVolumeByPrice(double price) {
			double ret = 0;
			if(containsKey(price)) {
				for(BooksTick tick : get(price)) {
					if(tick.direction > 0) {
						ret += tick.volume;
					}
				}
			}
			return ret;
		}
		
		public double sellVolumeByPrice(double price) {
			double ret = 0;
			if(containsKey(price)) {
				for(BooksTick tick : get(price)) {
					if(tick.direction < 0) {
						ret += tick.volume;
					}
				}
			}
			return ret;
		}
		
		public double fullMaxVolume() {
			if(fullMaxVolumeCache >= 0) {
				return fullMaxVolumeCache;
			}
			double ret = maxSellVolume()+maxBuyVolume();
			fullMaxVolumeCache = ret;
			return ret;
		}
		
		public double maxReducedVolume() {
			if(reducedMaxVolumeCache >= 0) {
				return reducedMaxVolumeCache;
			}
			double ret = 0;
			for(double price : keySet()) {
				ret = Math.max(ret, reducedVolumeByPrice(price));
			}
			reducedMaxVolumeCache = ret;
			return ret;
		}
		
		public double maxBuyVolume() {
			if(buyMaxVolumeCache >= 0) {
				return buyMaxVolumeCache;
			}
			double ret = 0;
			for(double price : keySet()) {
				ret = Math.max(ret, buyVolumeByPrice(price));
			}
			buyMaxVolumeCache = ret;
			return ret;
		}
		
		public double maxSellVolume() {
			if(sellMaxVolumeCache >= 0) {
				return sellMaxVolumeCache;
			}
			double ret = 0;
			for(double price : keySet()) {
				ret = Math.max(ret, sellVolumeByPrice(price));
			}
			sellMaxVolumeCache = ret;
			return ret;
		}
		
		public double sellVolumeFactor() {
			if(volumesFactorCache >= 0) {
				return volumesFactorCache;
			}
			double ret = 0;
			if(fullMaxVolume() > 0) {
				ret = maxSellVolume() / fullMaxVolume();
				volumesFactorCache = ret;
			}
			return ret;
		}
		
		public double maxPrice() {
			if(maxPriceCache > Double.MIN_VALUE) {
				return maxPriceCache;
			}
			double ret = Double.MIN_VALUE;
			for(double price : keySet()) {
				ret = Math.max(ret, price);
			}
			maxPriceCache = ret;
			return ret;
		}
		
		
		public long latestTickTime() {
			if(latestTimeCache != null) {
				return latestTimeCache;
			}
			long ret = Long.MIN_VALUE;
			for(double price : keySet()) {
				for(BooksTick tick : get(price)) {
					ret = Math.max(ret, tick.time);
				}
			}
			latestTimeCache = ret;
			return ret;
		}
		
		public double minPrice() {
			if(minPriceCache < Double.MAX_VALUE) {
				return minPriceCache;
			}
			double ret = Double.MAX_VALUE;
			for(double price : keySet()) {
				ret = Math.min(ret, price);
			}
			minPriceCache = ret;
			return ret;
		}
		
		public double volumesDelta() {
			if(!volumesDeltaCache.equals(Double.NaN)) {
				return volumesDeltaCache;
			}
			double ret = 0;
			for(double price : keySet()) {
				ret += reducedVolumeByPrice(price);
			}
			volumesDeltaCache = ret;
			return ret;
		}
		
		public double fullVolume() {
			if(!fullVolumeCache.equals(Double.NaN)) {
				return fullVolumeCache;
			}
			double ret = 0;
			for(double price : keySet()) {
				ret += fullVolumeByPrice(price);
			}
			fullVolumeCache = ret;
			return ret;
		}
		
		public double volumeDeltaFactor() {
			return volumesDelta() / fullVolume();
		}
		
		protected Long   latestTimeCache;
		protected Double volumesDeltaCache;
		protected Double fullVolumeCache;
		protected double maxPriceCache;
		protected double minPriceCache;
		protected double reducedMaxVolumeCache;
		protected double fullMaxVolumeCache;
		protected double sellMaxVolumeCache;
		protected double buyMaxVolumeCache;
		protected double volumesFactorCache;
		protected void resetCache() {
			latestTimeCache = null;
			volumesDeltaCache = Double.NaN;
			fullVolumeCache = Double.NaN;
			maxPriceCache = Double.MIN_VALUE;
			minPriceCache = Double.MAX_VALUE;
			reducedMaxVolumeCache = -1;
			fullMaxVolumeCache = -1;
			sellMaxVolumeCache = -1;
			buyMaxVolumeCache = -1;
			volumesFactorCache = -1;
		}
		
		public int reducedDirectionByPrice(double price) {
			double ret = 0;
			if(containsKey(price)) {
				for(BooksTick tick : get(price)) {
					ret += tick.volume * tick.direction;
				}
			}
			return ret > 0 ? 1 : -1;
		}
		
		@SuppressWarnings("unused")
		public long ticksCount() {
			long ret = 0;
			for(List<BooksTick> list : values()) {
				for(BooksTick tick : list) {
					ret++;
				}
			}
			return ret;
		}
		
		public String toString() {
			String ret = "[PriceGroupedTicksBundle\n";
			ret += "ticks count: "+ticksCount() + "\n";

			ret+= "\n, " + (completed == true ? "completed" : "not completed");
			ret+="]";
			return ret;
		}
	};
	
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
