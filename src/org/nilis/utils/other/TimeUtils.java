package org.nilis.utils.other;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
	/**
	 * @return GMT value of the passed time
	 */
	public static long toGMTTime(final long time) {
		final int offset = Calendar.getInstance().getTimeZone().getOffset(time);
		return time + offset;
	}

	/**
	 * @return current timestamp string
	 */
	public static String getCurrentTimestampString() {
		final long time = System.currentTimeMillis();
		final int offset = Calendar.getInstance().getTimeZone().getOffset(time);
		return String.valueOf((time + offset) / 1000);
	}

	/**
	 * @return current timestamp string
	 */
	public static String getCurrentLongTimestampString() {
		final long time = System.currentTimeMillis();
		final int offset = Calendar.getInstance().getTimeZone().getOffset(time);
		return String.valueOf((time + offset));
	}

	/**
	 * Transform timestamp to local phone time
	 * 
	 * @param timestamp
	 *            timestamp
	 * @return local local time converted from passed timestamp
	 */
	public static long getLocalTime(final long timestamp) {
		final int offset = Calendar.getInstance().getTimeZone().getOffset(System.currentTimeMillis());
		return timestamp * 1000 - offset;
	}

	/**
	 * Transform phone time to timestamp string
	 * 
	 * @param time
	 *            local time in milliseconds
	 * @return timestamp in seconds
	 */
	public static String getTimestampString(final long time) {
		final int offset = Calendar.getInstance().getTimeZone().getOffset(time);
		return String.valueOf((time + offset) / 1000);
	}
	
	
	/**
	 * @param c - calendar instance with date and time set
	 * @return timestamp representing given date with 00:00:00 time
	 */
	public static long getDateValueTime(Calendar c) {
		if(c==null) {
			return 0;
		}
		
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE,0);
		c.set(Calendar.SECOND, 0);
		return c.getTimeInMillis();
	}
	
	public static long getDateValueTime(Date date) {
		if(date==null) {
			return 0;
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return getDateValueTime(c);
	}
	
	public static long getDateValueTime(long timeInMillis) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeInMillis);
		
		return getDateValueTime(c);
	}
	
	public static Date dayRoundedDate(final Date dateToShow) {
		Date tempDateToShow = new Date(dateToShow.getTime());
		Calendar calendar = Calendar.getInstance();
		int timezoneOffset = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / (60*60*1000);
		tempDateToShow.setHours(timezoneOffset);
		tempDateToShow.setMinutes(0);
		tempDateToShow.setSeconds(0);
		tempDateToShow.setTime(tempDateToShow.getTime() - tempDateToShow.getTime() % 1000);
		return tempDateToShow;
	}
	
	static public boolean isSameDay(final Date date1, final Date date2) {
		if (date1.getYear() == date2.getYear() && date1.getMonth() == date2.getMonth()
				&& date1.getDate() == date2.getDate()) {
			return true;
		}
		return false;
	}
}
