package android.park.mail.ru.appandroid.calendar;

import android.content.Context;
import android.park.mail.ru.appandroid.models.Event;
import android.support.annotation.Nullable;

import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CellView;

import java.util.ArrayList;
import java.util.Map;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;


public class SchedulerCaldroidGridAdapter extends CaldroidGridAdapter {

	private ArrayList<Event> events;

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
				cellView.setText(event.getTitle());
			}
		}

	}
}
