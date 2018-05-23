package android.park.mail.ru.appandroid.calendar;

import android.content.Context;
import android.graphics.Color;
import android.park.mail.ru.appandroid.models.Event;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CellView;

import java.util.ArrayList;
import java.util.Map;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;


public class SchedulerCaldroidGridAdapter extends CaldroidGridAdapter {

	private ArrayList<Event> events;

	public static final int ULTRA_HIGH_COLOR = Color.RED;
	public static final int HIGH_COLOR = Color.BLUE;
	public static final int MEDIUM_COLOR = Color.GREEN;
	public static final int LOW_COLOR = Color.LTGRAY;

	SchedulerCaldroidGridAdapter(Context context, int month, int year,
	                             Map<String, Object> caldroidData,
	                             Map<String, Object> extraData,
	                             ArrayList<Event> events) {

		super(context, month, year, caldroidData, extraData);
		this.events = events;
	}

	void setEvents(@Nullable ArrayList<Event> events) {
		this.events = (events != null) ? events : new ArrayList<Event>();
	}

	@Override
	protected void customizeTextView(int position, CellView cellView) {
		super.customizeTextView(position, cellView);

		DateTime dateTime = this.datetimeList.get(position);
		for (final Event event : events) {
			final DateTime eventTime = DateTime.forInstant(
					event.getTimestamp() * 1000,
					TimeZone.getTimeZone("Russia/Moscow")
			);

			if (eventTime.isSameDayAs(dateTime)) {
				createEvent(cellView, event);
				continue;
			}
			if (event.getType() == Event.EventType.EVERY_DAY) {
				createEvent(cellView, event);
				continue;
			}
			if (event.getType() == Event.EventType.EVERY_WEEK
					&& eventTime.getWeekDay().equals(dateTime.getWeekDay())) {
				createEvent(cellView, event);
				continue;
			}
			if (event.getType() == Event.EventType.EVERY_MONTH
					&& eventTime.getDay().equals(dateTime.getDay())) {
				createEvent(cellView, event);
				continue;
			}
			if (event.getType() == Event.EventType.EVERY_YEAR
					&& eventTime.getDayOfYear().equals(dateTime.getDayOfYear())) {
				createEvent(cellView, event);
			}
		}
	}

	private void createEvent(@NonNull final CellView cellView,
	                         @NonNull final Event event) {
		setBackgroundColor(cellView, event.getPriority());
		cellView.setText(event.getTitle());
	}

	private void setBackgroundColor(@NonNull final CellView cellView,
	                                @Nullable final Event.Priority priority) {
		if (priority == null) {
			return;
		}

		switch(priority) {
			case LOW:
				cellView.setBackgroundColor(LOW_COLOR);
				break;

			case MEDIUM:
				cellView.setBackgroundColor(MEDIUM_COLOR);
				break;

			case HIGH:
				cellView.setBackgroundColor(HIGH_COLOR);
				break;

			case ULTRA_HIGH:
				cellView.setBackgroundColor(ULTRA_HIGH_COLOR);
				break;
		}
	}
}
