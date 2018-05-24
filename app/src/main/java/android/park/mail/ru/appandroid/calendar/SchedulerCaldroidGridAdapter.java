package android.park.mail.ru.appandroid.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.models.Event;
import android.park.mail.ru.appandroid.utils.Tools;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CellView;

import java.util.ArrayList;
import java.util.Map;

import hirondelle.date4j.DateTime;


public class SchedulerCaldroidGridAdapter extends CaldroidGridAdapter {

	private ArrayList<Event> events;
	private Resources resources;

	private static int ULTRA_HIGH_COLOR;
	private static int HIGH_COLOR;
	private static int MEDIUM_COLOR;
	private static int LOW_COLOR;


	public SchedulerCaldroidGridAdapter(Context context, int month, int year,
	                                    Map<String, Object> caldroidData,
	                                    Map<String, Object> extraData,
	                                    ArrayList<Event> events,
	                                    Resources resources) {
		super(context, month, year, caldroidData, extraData);
		ULTRA_HIGH_COLOR = resources.getColor(R.color.event_color_ultra_high);
		HIGH_COLOR = resources.getColor(R.color.event_color_high);
		MEDIUM_COLOR = resources.getColor(R.color.event_color_medium);
		LOW_COLOR = resources.getColor(R.color.event_color_low);
		this.resources = resources;
		this.events = events;
	}

	public void setEvents(@Nullable ArrayList<Event> events) {
		this.events = (events != null) ? events : new ArrayList<Event>();
	}

	@Override
	protected void customizeTextView(int position, CellView cellView) {
		super.customizeTextView(position, cellView);

		DateTime dateTime = this.datetimeList.get(position);
		for (final Event event : events) {

			final DateTime eventTime = Tools.getDate(event);

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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			cellView.setAutoSizeTextTypeWithDefaults(resources.getDimensionPixelSize(R.dimen.cell_text_size));
		} else {
			cellView.setTextSize(resources.getDimensionPixelSize(R.dimen.cell_text_size));
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
