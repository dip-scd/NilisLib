package org.nilis.finances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nilis.finances.FinancialData.BooksTick;
import org.nilis.utils.debug.D;

public class BookTicksBundle extends HashMap<Double, List<BooksTick>> {
	private static final long serialVersionUID = -2190416372504052471L;
	protected long startTime = 0;
	protected long period = 0;
	public boolean completed;
	
	protected Map<Long, BookTicksBundle> timedSubBundles = new HashMap<Long, BookTicksBundle>();
	
//	public static int MAX_PRICE_KEY = 0;O
//	public static int MIN_PRICE_KEY = 1;
//	
//	protected Map<Long, Map<Integer, BooksTick>> timedTicksStats = new HashMap<Long, Map<Integer, BooksTick>>();
	protected List<BooksTick> ticks = new LinkedList<BooksTick>();
	
	public BookTicksBundle(long startTime, long period) {
		this(startTime, period, true);
	}
	
	public BookTicksBundle(long startTime, long period, boolean completed) {
		this.startTime = startTime;
		this.period = period;
		this.completed = completed;
		resetCache();
	}
	
	public Set<Long> times() {
		return timedSubBundles.keySet();
	}
	
	public BookTicksBundle subBundleByTime(Long time) {
		return timedSubBundles.get(time);
	}
	
	public List<BooksTick> ticks() {
		return ticks;
	}
	
	public void clear() {
		ticks.clear();
	}
	
	public long startTime() {
		return startTime;
	}
	
	public long period() {
		return period;
	}
	
	protected double maxTickVolume = 0;
	public double maxTickVolume() {
		return maxTickVolume;
	}
	
	public void addTicks(List<BooksTick> ticks) {
		for(BooksTick tick : ticks) {
			addTick(tick);
		}
	}
	
	public void addTick(BooksTick tick) {
		if(!containsKey(tick.price)) {
			put(tick.price, new LinkedList<BooksTick>());
		}
		get(tick.price).add(tick);
		
		if(period > 1000) {
			if(!timedSubBundles.containsKey(tick.time)) {
				timedSubBundles.put(tick.time, new BookTicksBundle(tick.time, 1000));
			}
			timedSubBundles.get(tick.time).addTick(tick);
		}
		ticks.add(tick);
		maxTickVolume = Math.max(maxTickVolume, tick.volume);
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
	
	public double volumeDelta() {
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
		return volumeDelta() / fullVolume();
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
	
	public double volumesDeltaFromPrice(double price) {
		double ret = 0;
		double buyVolume = 0;
		double sellVolume = 0;
		for(double existingPrice : keySet()) {
			if(existingPrice >= price && reducedDirectionByPrice(existingPrice) == -1) {
				sellVolume += sellVolumeByPrice(existingPrice);
			} else if(existingPrice < price && reducedDirectionByPrice(existingPrice) == 1) {
				buyVolume += buyVolumeByPrice(existingPrice);
			}
		}
		ret = - sellVolume + buyVolume;
		return ret;
	}
	
	protected static Comparator<Double> negativeComparator = new Comparator<Double>() {
		@Override
		public int compare(Double o1, Double o2) {
			return (int) Math.signum(o1 - o2);
		}
	};
	
	protected static Comparator<Double> positiveComparator = new Comparator<Double>() {
		@Override
		public int compare(Double o1, Double o2) {
			return (int) Math.signum(o2 - o1);
		}
	};
	
	public List<Double> availablePricesByDirection(final int direction) {
		LinkedList<Double> ret = new LinkedList<Double>();
		for(double price : keySet()) {
			if(reducedDirectionByPrice(price) == (-direction)) {
				ret.add(price);
			}
		}
		Collections.sort(ret, direction < 0 ? positiveComparator : negativeComparator);
		return ret;
	}
	
	public double movementWouldBeCausedByVolume(double volume, int direction) {
		double ret = 0;
		double currentPrice = 0;
		double start = 0;
		double unusedVolume = volume;
		List<Double> prices = availablePricesByDirection(direction);
		if(prices.size() > 0) {
			start = prices.get(0);
		}
		for(double price : prices) {
			currentPrice = price;
			unusedVolume -= reducedVolumeByPrice(price);
			if(unusedVolume <= 0) {
				break;
			}
			//ret+=1;
		}
		ret = currentPrice - start;
		return ret;
	}
	
	public double potentialMovementDisbalance(double volume) {
		return  movementWouldBeCausedByVolume(volume, 1)+ 
				movementWouldBeCausedByVolume(volume, -1);
	}
	
	public double potentialPartialMovementDisbalance(double part) {
		return potentialMovementDisbalance(fullVolume()*part);
	}
	
	public double potentialMultiMovementDisbalance(Set<Double> parts) {
		double ret = 0;
		//double partsMultipier = 0;
		for(double part : parts) {
			if(part > 0) {
				//double currentMultipier = (1.0/part);
				//partsMultipier += (currentMultipier);
				ret += potentialPartialMovementDisbalance(part);// * currentMultipier;						
			}
		}
		return ret;
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
	
	public BookTicksBundle getChangesGroup() {
		BookTicksBundle ret = new BookTicksBundle(startTime, period);
		List<BooksTick> currTicksList;
		long thresoldTime = startTime + period / 2;
		int dir = 0;
		for(double price : keySet()) {
			currTicksList = get(price);
			for(BooksTick tick : currTicksList) {
				if(tick.time < thresoldTime) {
					dir = -tick.direction;
				} else {
					dir = tick.direction;
				}
				ret.addTick(new BooksTick(tick.symbol,
						tick.time, 
						tick.price,
						tick.volume,
						dir));
			}
		}
		return ret;
	}
	
	
	public String toString() {
		String ret = "[PriceGroupedTicksBundle "+new Date(startTime)+"\n";
		ret += "ticks count: "+ticksCount() + "\n";

		ret+= "\n, " + (completed == true ? "completed" : "not completed");
		ret+="]";
		return ret;
	}
}