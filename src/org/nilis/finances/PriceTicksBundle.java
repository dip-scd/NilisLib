package org.nilis.finances;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nilis.data.utils.RuntimeDataStructuresUtils;
import org.nilis.finances.FinancialData.BooksTick;
import org.nilis.finances.FinancialData.PriceTick;
import org.nilis.utils.data.DataPair;

public class PriceTicksBundle {
	protected List<PriceTick> ticks = new LinkedList<PriceTick>();
	protected Map<Long, List<PriceTick>> timedTicks = new HashMap<Long, List<PriceTick>>();
	protected Map<Double, List<PriceTick>> pricedTicks = new HashMap<Double, List<PriceTick>>();
	
	protected Map<Long, DataPair<Double, Double>> timedBidPrices = new HashMap<Long, DataPair<Double, Double>>();
	protected Map<Long, DataPair<Double, Double>> timedAskPrices = new HashMap<Long, DataPair<Double, Double>>();
	
	
	protected long startTime = 0;
	protected long period = 0;
	public boolean completed;
	
	protected long firstTick = Long.MAX_VALUE;
	protected long lastTick = Long.MIN_VALUE;
	
	protected double openBid = 0;
	protected double closeBid = 0;
	
	protected double openAsk = 0;
	protected double closeAsk = 0;
	
	protected double maxBid = 0;
	protected double minBid = Double.MAX_VALUE;
	
	protected double maxAsk = 0;
	protected double minAsk = Double.MAX_VALUE;
	
	public double getOpenPrice() {
		return openBid;
	}

	public double getClosePrice() {
		return closeBid;
	}
	
	public boolean bullish() {
		return closeBid > openBid;
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

	public double maxBid() {
		return maxBid;
	}

	public double minBid() {
		return minBid;
	}
	
	public double maxAsk() {
		return maxAsk;
	}

	public double minAsk() {
		return minAsk;
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
		if(!timedBidPrices.containsKey(tick.time)) {
			timedBidPrices.put(tick.time, new DataPair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE));
		}
		double min = timedBidPrices.get(tick.time).getTag();
		double max = timedBidPrices.get(tick.time).getData();
		timedBidPrices.get(tick.time).setTag(Math.min(min, tick.bid));
		timedBidPrices.get(tick.time).setData(Math.max(max, tick.bid));
		
		if(!timedAskPrices.containsKey(tick.time)) {
			timedAskPrices.put(tick.time, new DataPair<Double, Double>(Double.MAX_VALUE, Double.MIN_VALUE));
		}
		min = timedAskPrices.get(tick.time).getTag();
		max = timedAskPrices.get(tick.time).getData();
		timedAskPrices.get(tick.time).setTag(Math.min(min, tick.ask));
		timedAskPrices.get(tick.time).setData(Math.max(max, tick.ask));
	}
	
	public void addTicks(List<PriceTick> ticks) {
		for(PriceTick tick : ticks) {
			addTick(tick);
		}
	}
	
	public void addTick(PriceTick tick) {
		maxBid = Math.max(maxBid, tick.bid);
		minBid = Math.min(minBid, tick.bid);
		
		maxAsk = Math.max(maxAsk, tick.ask);
		minAsk = Math.min(minAsk, tick.ask);
		if(tick.time < firstTick) {
			openBid = tick.bid;
			openAsk = tick.ask;
			firstTick = tick.time;
		}
		if(tick.time > lastTick) {
			closeBid = tick.bid;
			closeAsk = tick.ask;
			lastTick = tick.time;
		}
		ticks.add(tick);
		RuntimeDataStructuresUtils.addToListMap(timedTicks, tick.time, tick);
		RuntimeDataStructuresUtils.addToListMap(pricedTicks, tick.bid, tick);
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
	
	public void clear() {
		ticks.clear();
		pricedTicks.clear();
		timedTicks.clear();
		timedBidPrices.clear();
		timedAskPrices.clear();
		resetCache();
	}
	
	public Set<Double> prices() {
		return pricedTicks.keySet();
	}
	
	public DataPair<Double, Double> bidRangeByTime(long time) {
		return timedBidPrices.get(time);
	}
	
	public double maxAskByTime(long time) {
		return timedAskPrices.get(time).getData();
	}
	
	public double minBidByTime(long time) {
		return timedBidPrices.get(time).getTag();
	}
	
	public double bidMovement() {
		return closeBid - openBid;
	}
	
	public Set<Long> times() {
		return timedBidPrices.keySet();
	}
	
	public boolean empty() {
		return ticks == null || ticks.size() <= 0;
	}
	
	public String toString() {
		return "[PriceTicksBundle: "+
				"\n, " + new Date(startTime).toString() + " ("+startTime+")" +
				"\n, " + minBid +
				"\n, " + maxBid +
				"\nticks count: " + ticks.size() +
				"]";
	}
}