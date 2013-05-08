package org.nilis.finances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.nilis.finances.FinancialData.PriceTick;

public class Bar extends PriceTick{

	protected boolean isTick = false;
	public boolean isTick() {
		return isTick;
	}
	
	protected int direction = 0;
	public int direction() {
		return direction;
	}
	
	public Bar(String symbol, long timeInSeconds, double ask, double bid) {
		super(symbol, timeInSeconds, ask, bid);
		isTick = true;
	}
	
	public Bar(PriceTick tick) {
		super(tick.symbol, tick.time, tick.ask, tick.bid);
		isTick = true;
	}
	
	public Bar(Bar tick) {
		super(tick.symbol, tick.time, tick.ask, tick.bid);
		isTick = true;
	}
	
	
	public Bar(Bar first, Bar second) {
		super(first);
		startBar = first;
		endBar = second;
		children.add(first);
		children.add(second);
		calculateDirection();
	}

	protected void calculateDirection() {
		direction = (int) Math.signum(closeBid() - openBid());
	}
	
	protected static Comparator<Bar> earliestBarComparator = new Comparator<Bar>() {

		@Override
		public int compare(Bar o1, Bar o2) {
			return (int) (o1.time - o2.time);
		}
	};
	
	protected Bar startBar = null;
	protected Bar endBar = null;
	
	public double openBid() {
		if(startBar != null) {
			return startBar.openBid();
		}
		return bid;
	}
	
	public double closeBid() {
		if(endBar != null) {
			return endBar.closeBid();
		}
		return bid;
	}
	
	public long openTime() {
		if(startBar != null) {
			return startBar.openTime();
		}
		return time;
	}
	
	public long closingTime() {
		if(endBar != null) {
			return endBar.closingTime();
		}
		return time;
	}
	
	protected static Bar getEarliestBarFromCollection(List<Bar> bars) {
		Bar earliestBar = new Bar(null, Long.MAX_VALUE, 0, 0);
		if(bars != null) {
			Collections.sort(bars, earliestBarComparator);
			if(bars.size() > 0) {
				earliestBar = bars.get(0);
			}
		}
		return earliestBar;
	}
	
	protected static List<Bar> convertTicksToBars(List<PriceTick> ticks) {
		List<Bar> ret = new LinkedList<Bar>();
		for(PriceTick tick : ticks) {
			if(!(tick instanceof Bar)) {
				ret.add(new Bar(tick));
			} else {
				ret.add((Bar)tick);
			}
		}
		return ret;
	}
	
	public Bar(List<PriceTick> ticks) {
		super((PriceTick)getEarliestBarFromCollection(convertTicksToBars(ticks)));
		int fullTicksSize = ticks.size();
		List<Bar> bars = convertTicksToBars(ticks);
		
		if(bars.size() > 1) {
			for(int i=0; i<bars.size(); i++) {
				Bar bar = bars.get(i);
				if(bar.isTick()) {
					if(i<bars.size()-1) {
						children.add(new Bar(bar, bars.get(i+1)));
					}
				} else {
					children.add(bar);
				}
			}
			boolean negativeDetected = false;
			boolean positiveDetected = false;
			boolean opposingDetected = negativeDetected && positiveDetected;
			//boolean hasZeroes = false;
			while(!(children.size() <= 2 || childrenFormZigZag() || childrenConsolidatedByDirection())) {
				//hasZeroes = false;
				groupSameDirectionBars(fullTicksSize);
				
				for(Bar bar : children) {
					if(bar.direction() == 1) {
						positiveDetected = true;
					} else if(bar.direction() == -1) {
						negativeDetected = true;
					}
				}
				opposingDetected = negativeDetected && positiveDetected;
				
				if(children.size() > 3 && opposingDetected) {
					groupZigZags();
				}
				
				//grouped = true;
			}
		} else {
			isTick = true;
		}
		if(children.size() == 1) {
			Bar childBar = children.get(0);
			children.clear();
			children = new ArrayList<Bar>(childBar.children);
		}
		if(children.size() > 0) {
			startBar = children.get(0);
			endBar = children.get(children.size()-1);
		}
		calculateDirection();
		
		if(!isTick() && direction() != 0 && openBid() != 0 && closeBid() != 0) {
			levels.add(new Level(closeBid(), duration(), direction(), closingTime()));
		}
		//printChildren();
	}
	
	public long duration() {
		return closingTime() - openTime();
	}

	protected void groupSameDirectionBars(int fullTicksSize) {
		if(childrenFormZigZag()) {
			return;
		}
		List<Bar> bars;
		bars = new LinkedList<Bar>(children);
		children.clear();
		int dir = 0;
		List<PriceTick> currentBarsGroup = new LinkedList<PriceTick>();
		Bar currentBar = null;
		for(int i=0; i<bars.size(); i++) {
			currentBar = bars.get(i);
			
			if(dir == 0) {
				currentBarsGroup.add(currentBar);
				dir = currentBar.direction();
				if(i == bars.size() - 1) {
					addChildren(currentBarsGroup, fullTicksSize);
				}
			} else {
				if(currentBar.direction() == 0 || currentBar.direction() == dir) {
					currentBarsGroup.add(currentBar);
					if(i == bars.size() - 1) {
						addChildren(currentBarsGroup, fullTicksSize);
					}
				} else {
					addChildren(currentBarsGroup, fullTicksSize);
					currentBarsGroup.clear();
					dir = currentBar.direction();
					//currentBarsGroup.add(currentBar);
					i--;
				}
			}
		}
	}

	protected boolean childrenFormZigZag() {
		if(children.size() == 3) {
			int first = children.get(0).direction();
			int second = children.get(1).direction();
			int third = children.get(2).direction();
			if(first != 0 && second != 0 && third != 0) {
				if(first == second*(-1) && first == third) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean childrenConsolidatedByDirection() {
		int negativeFound = 0;
		//int neutralFound = 0;
		int positiveFound = 0;
		for(Bar bar : children) {
			if(bar.direction() == -1) {
				negativeFound++;
			} else if(bar.direction() == 0) {
				//neutralFound++;
			} else if(bar.direction() == 1) {
				positiveFound++;
			}
		}
		if(negativeFound > 0 && positiveFound == 0) {
			return true;
		}
		if(negativeFound == 0 && positiveFound > 0) {
			return true;
		} else if(negativeFound == 0 && positiveFound == 0) {
			return true;
		}
		return false;
	}

	protected void addChildren(List<PriceTick> currentBarsGroup, int fullBarsCount) {
		if(currentBarsGroup.size() == fullBarsCount) {
			for(PriceTick bar : currentBarsGroup) {
				children.add((Bar)bar);
			}
			return;
		}
		if(currentBarsGroup.size() > 1) {
			children.add(new Bar(currentBarsGroup));
		} else if(currentBarsGroup.size() == 1) {
			children.add((Bar) currentBarsGroup.get(0));
		}
	}

	protected List<Bar> children = new ArrayList<Bar>();
	public List<Bar> children() {
		return children;
	}
	
	protected void groupZigZags() {
		if(childrenFormZigZag()) {
			return;
		}
		List<Bar> bars = new LinkedList<Bar>(children);
		int zigzagCapableSize = ((int)(children.size() / 3) * 3);
		children.clear();
		List<PriceTick> currentZigZagGroup = new LinkedList<PriceTick>();
		
		for(int i=0; i<=zigzagCapableSize-1; i+=3) {
			if(bars.get(i).direction() == bars.get(i+1).direction()*(-1) &&
					bars.get(i).direction() == bars.get(i+2).direction()) {
				currentZigZagGroup.add(bars.get(i));
				currentZigZagGroup.add(bars.get(i+1));
				currentZigZagGroup.add(bars.get(i+2));
				children.add(new Bar(currentZigZagGroup));
				currentZigZagGroup.clear();
			}
		} for(int i=zigzagCapableSize; i<bars.size(); i++) {
			children.add(bars.get(i));
		}
	}
	
	public static class Level {
		protected double price;
		protected double power;
		protected int direction;
		protected long timeAppeared;
		public Level(double price, double power, int direction, long timeAppeared) {
			this.price = price;
			this.power = power;
			this.direction = direction;
			this.timeAppeared = timeAppeared;
		}
		
		public double price() {
			return price;
		}
		public double power() {
			return power;
		}
		
		public int direction() {
			return direction;
		}
		
		public long timeAppeared() {
			return timeAppeared;
		}
	}
	
	protected List<Level> levels = new ArrayList<Level>();
	public List<Level> levels() {
		List<Level> ret = new ArrayList<>(levels);
		for(Bar child : children) {
			ret.addAll(child.levels());
		}
		return ret;
	}
	
	public void printChildren() {
		System.out.print(toString());
		for(Bar bar : children) {
			System.out.print(bar.toString());
		}
	}
	
	public String toString() {
		return "[Bar: "+ symbol + (isTick() ? " (tick) " : "") +
				"\ntime: " + new Date(openTime()).toString() + " - "+new Date(closingTime()).toString()+"" +
				"\nprice: " + openBid() + " - " + closeBid() + " " +
				"\ndirection: " + direction +
				"\nchildren: " + children.size() +
				"]\n";
	}
}
