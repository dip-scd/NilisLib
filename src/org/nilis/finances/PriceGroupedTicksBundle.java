package org.nilis.finances;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.nilis.finances.FinancialData.BooksTick;

public class PriceGroupedTicksBundle extends HashMap<Double, List<BooksTick>> {
	private static final long serialVersionUID = -2190416372504052471L;
	protected long startTime = 0;
	protected long period = 0;
	public boolean completed;
	
	public PriceGroupedTicksBundle(long startTime, long period) {
		this(startTime, period, true);
	}
	
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
	
	public PriceGroupedTicksBundle getChangesGroup() {
		PriceGroupedTicksBundle ret = new PriceGroupedTicksBundle(startTime, period);
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
	
	public PriceGroupedTicksBundle getBreathGroup() {
		PriceGroupedTicksBundle ret = new PriceGroupedTicksBundle(startTime, period);
		List<BooksTick> currTicksList;
		long thresoldTime = (long) (startTime + period*0.75);
		int dir = 0;
		for(double price : keySet()) {
			currTicksList = get(price);
			for(BooksTick tick : currTicksList) {
				double multipier = 1.0;
				if(tick.time < thresoldTime) {
					dir = -tick.direction;
				} else {
					multipier = 3.0;
					dir = tick.direction;
				}
				ret.addTick(new BooksTick(tick.symbol,
						tick.time, 
						tick.price,
						tick.volume*multipier,
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