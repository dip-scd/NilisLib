package org.nilis.utils.calendar;

public class CalendarEvent {
	protected String title;
	protected String description;
	protected long startTimestamp;
	protected long endTimestamp;
	protected String location;
	protected String duration;

	public CalendarEvent(final String eventTitle, final String eventDescription, final long start, final long end,
			final String eventLocation, final String eventDuration) {
		title = eventTitle;
		description = eventDescription;
		startTimestamp = start;
		endTimestamp = end;
		location = eventLocation;
		duration = eventDuration;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	public long getEndTimestamp() {
		return endTimestamp;
	}

	public String getLocation() {
		return location;
	}

	public String getDuration() {
		return duration;
	}
}
