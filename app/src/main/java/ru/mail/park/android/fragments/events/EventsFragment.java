package ru.mail.park.android.fragments.events;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;

import hirondelle.date4j.DateTime;
import park.mail.ru.android.R;
import ru.mail.park.android.fragments.calendar.SchedulerCaldroidFragment;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.utils.ListenerWrapper;
import ru.mail.park.android.utils.Tools;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.common.collect.HashMultimap;
import com.roomorama.caldroid.CaldroidFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public abstract class EventsFragment extends Fragment {

	public static final String DASHBOARD_ID = "dash_id";
	protected static final String DASHBOARD = "dashboard_bundle";
	protected Dashboard dashboard;
	protected ProgressBar progressBar;
	protected SchedulerCaldroidFragment calendarFragment;
	@NonNull
	protected List<ListenerWrapper> wrappers = new LinkedList<>();
	@Nullable
	protected HashMap<DateTime, Drawable> eventsLabels;
	@NonNull
	protected HashMultimap<DateTime, Event> dateAssociatedWithEvents = HashMultimap.create();


	public EventsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	@CallSuper
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_events, container, false);
		progressBar = view.findViewById(R.id.progressbar_event_load);
		return view;
	}

	@Override
	@CallSuper
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DASHBOARD, dashboard);
		setHasOptionsMenu(true);
	}

	@Override
	public void onStop() {
		super.onStop();
		for (ListenerWrapper wrapper : wrappers) {
			wrapper.unregister();
		}
	}

	protected void setCalendar(@NonNull final Dashboard dashboard) {

		calendarFragment = new SchedulerCaldroidFragment();

		final Bundle bundle = new Bundle();
		bundle.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
		bundle.putLong(SchedulerCaldroidFragment.DASH_ID_BUNDLE, dashboard.getDashID());
		bundle.putBoolean(SchedulerCaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
		bundle.putBoolean(SchedulerCaldroidFragment.ENABLE_CLICK_ON_DISABLED_DATES, false);
		calendarFragment.setArguments(bundle);

		// Setup user's events
		calendarFragment.setBackgroundDrawableForDateTimes(eventsLabels);

//		DateTime now = DateTime.now(Tools.TIME_ZONE);
//		final DateTime minDate = now.minusDays(365);
//		final DateTime maxDate = now.plusDays(365);
//
//		calendarFragment.setMaxDate(new Date(now.plusDays(365).getMilliseconds(Tools.TIME_ZONE)));
//		calendarFragment.setMinDate(new Date(now.minusDays(365).getMilliseconds(Tools.TIME_ZONE)));

//		calendarFragment.setEvents(dashboard.getEvents());

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.caldroid_container, calendarFragment)
				.commit();

		// Set title of dashboard in ActionBar
		final ActionBar bar = ((AppCompatActivity)getActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(dashboard.getTitle());
		}
	}

	protected HashMap<DateTime, Drawable> calculateCirclesForEventDates(
			@NonNull final DateTime minDate,
			@NonNull final DateTime maxDate,
			@NonNull ArrayList<Event> events) {

		DateTime iteratedDate = minDate;
		final HashMap<DateTime, Drawable> backgrounds = new HashMap<>();

		// Try to group all events by the dates (in multiMapEventDates)
		while (iteratedDate.lteq(maxDate)) {
			// Find all events that point to iterated date
			for (final Event event : events) {

				final DateTime eventTime = Tools.getDateTime(event);

				if (iteratedDate.isSameDayAs(eventTime)) {
					dateAssociatedWithEvents.put(iteratedDate, event);
					continue;
				}
				if (event.getType() == Event.EventType.EVERY_DAY) {
					dateAssociatedWithEvents.put(iteratedDate, event);
					continue;
				}
				if (event.getType() == Event.EventType.EVERY_WEEK
						&& iteratedDate.getWeekDay().equals(eventTime.getWeekDay())) {
					dateAssociatedWithEvents.put(iteratedDate, event);
					continue;
				}
				if (event.getType() == Event.EventType.EVERY_MONTH
						&& iteratedDate.getDay().equals(eventTime.getDay())) {
					dateAssociatedWithEvents.put(iteratedDate, event);
					continue;
				}
				if (event.getType() == Event.EventType.EVERY_YEAR
						&& iteratedDate.getDayOfYear().equals(eventTime.getDayOfYear())) {
					dateAssociatedWithEvents.put(iteratedDate, event);
				}
			}
			iteratedDate = iteratedDate.plusDays(1);
		}


		final Float cellSize = Tools.getCalendarCellWidthInPixels(getResources());
		// Now each date contains a full set of events
		for (final DateTime eventDate : dateAssociatedWithEvents.keySet()) {

			// Layers of "circle" - multicolored marks indicating that this date has an event
			final LinkedList<Drawable> eventCircleLayers = new LinkedList<>();
			// The first layer must be a default background of cell
			eventCircleLayers.addLast(getResources().getDrawable(com.caldroid.R.drawable.cell_bg));

			// Iterated on all events that belong to current eventDate
			// and extract event's color (priority) (in eventCircleLayers)
			for (final Event eventByDate : dateAssociatedWithEvents.get(eventDate)) {
				if (eventByDate == null) {
					throw new IllegalArgumentException("Event is null");
				}

				switch (eventByDate.getPriority()) {
					case LOW:
						eventCircleLayers.addLast(getResources().getDrawable(R.drawable.event_low));
						break;

					case MEDIUM:
						eventCircleLayers.addLast(getResources().getDrawable(R.drawable.event_medium));
						break;

					case HIGH:
						eventCircleLayers.addLast(getResources().getDrawable(R.drawable.event_high));
						break;

					case ULTRA_HIGH:
						eventCircleLayers.addLast(getResources().getDrawable(R.drawable.event_ultra_high));
						break;

					default:
						throw new IllegalArgumentException();
				}
			}


			// Finally, we compressed all the drawable layers into one
			final LayerDrawable layerDrawable = new LayerDrawable(
					eventCircleLayers.toArray(new Drawable[] {}));
			// To the events of one and the same day do not obscure each other
			for (int i = 1; i < eventCircleLayers.size(); ++i) {
				int order = i - 1;
				float paddingBottom = 5f;
				float paddingRight = cellSize * order * 0.1f + 5f;
				float paddingLeft = cellSize * (0.65f - order * 0.1f);
				float paddingTop = cellSize * 0.65f;

				layerDrawable.setLayerInset(
						i,
						(int) paddingLeft,
						(int) paddingTop,
						(int) paddingRight,
						(int) paddingBottom
				);
			}

			// At the end, add ready layer to map
			backgrounds.put(
					// Hour, min, sec, msec, nsec must be reset to zero, otherwise
					// it won't be able to update backgrounds of calendar's cells
					new DateTime(eventDate.getYear(), eventDate.getMonth(), eventDate.getDay(), 0, 0, 0, 0),
					layerDrawable
			);
		}

		return backgrounds;
	}

}
