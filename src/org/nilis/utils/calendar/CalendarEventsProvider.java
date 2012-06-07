package org.nilis.utils.calendar;

import java.util.ArrayList;
import java.util.List;

import org.nilis.utils.data.DataProvider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class CalendarEventsProvider implements DataProvider<Long, List<CalendarEvent>> {

	private static final Uri OLDER_CALENDAR_URI = Uri.parse("content://calendar/events");
	private static final Uri NEWER_CALENDAR_URI = Uri.parse("content://com.android.calendar/events");
	private Context context = null;

	public CalendarEventsProvider(final Context contextToUse) {
		context = contextToUse;
	}

	private Cursor getEventsCursor(final String[] projection, final String sortOrder) {
		Cursor ret = context.getContentResolver().query(OLDER_CALENDAR_URI, projection, null, null, sortOrder);
		if (ret == null) {
			ret = context.getContentResolver().query(NEWER_CALENDAR_URI, projection, null, null, sortOrder);
		}
		return ret;
	}

	// @Override
	// public void get() {
	//
	// }

	@Override
	public void get(final Long startTimestampFilter, final OnDataListener<Long, List<CalendarEvent>> listener) {
		final List<CalendarEvent> ret = new ArrayList<CalendarEvent>();

		final Cursor cursor = getEventsCursor(new String[] { "title", "description", "eventTimezone", "eventLocation",
				"eventStatus", "dtstart", "duration", "hasAlarm", "allDay", "dtend" }, "dtstart ASC");

		try {
			if (cursor != null && cursor.moveToFirst()) {
				final int _title = cursor.getColumnIndex("title");
				final int _description = cursor.getColumnIndex("description");
				final int _startDate = cursor.getColumnIndex("dtstart");
				final int _endDate = cursor.getColumnIndex("dtend");
				final int _eventLocation = cursor.getColumnIndex("eventLocation");
				final int _duration = cursor.getColumnIndex("duration");
				do {
					if (cursor.getLong(_startDate) > startTimestampFilter.longValue()) {
						ret.add(new CalendarEvent(cursor.getString(_title), cursor.getString(_description), cursor
								.getLong(_startDate), cursor.getLong(_endDate), cursor.getString(_eventLocation),
								cursor.getString(_duration)));
					}
				} while (cursor.moveToNext());
			}
		} catch (final Exception e) {
			listener.onDataFailed(startTimestampFilter, e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		listener.onDataReceived(startTimestampFilter, ret);
	}
}
