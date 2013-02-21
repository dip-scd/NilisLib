package org.nilis.finances;

import org.nilis.finances.FinancialData.BooksTick;
import org.nilis.flow.Criteria;

public class BookTicksBundleUtils {
	public static BookTicksBundle getGroupWithPricesInPricesGroup(
			BookTicksBundle booksGroup,
			PriceTicksBundle pricesGroup,
			double borderArea) {
		return getGroupFilteredByCriteria(booksGroup, 
				new InsidePriceGroupCriteria(borderArea, pricesGroup));
	}
	
	public static BookTicksBundle getGroupWithPricesOutsidePricesGroup(
			BookTicksBundle booksGroup,
			PriceTicksBundle pricesGroup,
			double borderArea) {
		return getGroupFilteredByCriteria(booksGroup, new OutsidePriceGroupCriteria(borderArea, pricesGroup));
	}
	
	protected static abstract class PricesGroupBasedCriteria implements Criteria<BooksTick> {
		protected double priceBorderArea = 0;
		protected PriceTicksBundle pricesGroup =  null;
		
		public PricesGroupBasedCriteria(double priceBorderArea, PriceTicksBundle pricesGroup) {
			this.priceBorderArea = priceBorderArea;
			this.pricesGroup = pricesGroup;
		}
		
		@Override
		abstract public boolean valid(BooksTick tick);
	}
	
	public static class OutsidePriceGroupCriteria extends PricesGroupBasedCriteria {
		public OutsidePriceGroupCriteria(double priceBorderArea,
				PriceTicksBundle pricesGroup) {
			super(priceBorderArea, pricesGroup);
		}

		@Override
		public boolean valid(BooksTick tick) {
			if(pricesGroup.bidRangeByTime(tick.time) == null) {
				return false;
			}
			double minPrice = pricesGroup.minBidByTime(tick.time)-priceBorderArea;
			double maxPrice = pricesGroup.maxAskByTime(tick.time)+priceBorderArea;

			if(tick.price >= maxPrice || tick.price <= minPrice) {
				return true;
			}
			return false;
		}
	}
	
	public static class InsidePriceGroupCriteria extends PricesGroupBasedCriteria {
		public InsidePriceGroupCriteria(double priceBorderArea,
				PriceTicksBundle pricesGroup) {
			super(priceBorderArea, pricesGroup);
		}

		@Override
		public boolean valid(BooksTick tick) {
			if(pricesGroup.bidRangeByTime(tick.time) == null) {
				return false;
			}
			double minPrice = pricesGroup.minBidByTime(tick.time)-priceBorderArea;
			double maxPrice = pricesGroup.maxAskByTime(tick.time)+priceBorderArea;

			if(tick.price <= maxPrice && tick.price >= minPrice) {
				return true;
			}
			return false;
		}
	}
	
	public static BookTicksBundle getGroupFilteredByCriteria(BookTicksBundle group, Criteria<BooksTick> criteria) {
		BookTicksBundle ret = new BookTicksBundle(group.startTime(), group.period());
		for(BooksTick tick : group.ticks()) {
			if(criteria.valid(tick)) {
				ret.addTick(tick);
			}
		}
		return ret;
	}
	
}
