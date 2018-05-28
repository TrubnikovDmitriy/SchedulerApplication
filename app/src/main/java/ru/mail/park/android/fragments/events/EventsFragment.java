package ru.mail.park.android.fragments.events;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;

import hirondelle.date4j.DateTime;
import park.mail.ru.android.R;
import ru.mail.park.android.fragments.calendar.SchedulerCaldroidFragment;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.recycler.EventAdapter;
import ru.mail.park.android.utils.ListenerWrapper;
import ru.mail.park.android.utils.Tools;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.common.collect.HashMultimap;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CalendarHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public abstract class EventsFragment extends Fragment {

	protected final static ExecutorService executor = Executors.newSingleThreadExecutor();

	public static final String DASHBOARD_ID = "DASHBOARD_ID";
	protected static final String DASHBOARD_BUNDLE = "DASHBOARD_BUNDLE";
	protected static final String CALENDAR_BUNDLE = "CALENDAR_BUNDLE";
	private static final int MAX_DRAWABLES_EVENTS = 8;


	protected final Handler handler = new Handler(Looper.getMainLooper());

	protected Resources resources;
	protected Dashboard dashboard;
	protected ProgressBar progressBar;
	protected SchedulerCaldroidFragment calendarFragment;
	protected RecyclerView recyclerView;
	protected EventAdapter adapter;
	@Nullable protected HashMap<DateTime, Drawable> eventsLabels;
	@NonNull protected List<ListenerWrapper> wrappers = new LinkedList<>();
	@NonNull protected HashMultimap<DateTime, Event> dateAssociatedWithEvents = HashMultimap.create();


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
		resources = view.getResources();

		adapter = new EventAdapter(
				resources.getStringArray(R.array.event_priority),
				resources.getStringArray(R.array.event_type)
		);
		recyclerView = view.findViewById(R.id.recycler_events);
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		return view;
	}

	@Override
	@CallSuper
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DASHBOARD_BUNDLE, dashboard);
		setHasOptionsMenu(true);
		if (calendarFragment != null) {
			calendarFragment.saveStatesToKey(outState, CALENDAR_BUNDLE);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		for (ListenerWrapper wrapper : wrappers) {
			wrapper.unregister();
		}
		progressBar.setVisibility(View.GONE);
	}


	protected void createCalendarFragment(@Nullable Bundle savedInstanceState) {

		calendarFragment = new SchedulerCaldroidFragment();

		if (savedInstanceState != null) {
			calendarFragment.restoreStatesFromKey(savedInstanceState, DASHBOARD_BUNDLE);
		} else {
			final Bundle bundle = new Bundle();
			bundle.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
			bundle.putLong(SchedulerCaldroidFragment.DASH_ID_BUNDLE, dashboard.getDashID());
			bundle.putBoolean(SchedulerCaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
			calendarFragment.setArguments(bundle);
		}

		if (getFragmentManager() != null) {
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.caldroid_container, calendarFragment)
					.commit();
		}
	}

	protected void updateActionBarTitle() {
		// Set title of dashboard in ActionBar
		final Activity activity = getActivity();
		if (activity != null) {
			final ActionBar bar = ((AppCompatActivity) activity).getSupportActionBar();
			if (bar != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						bar.setTitle(dashboard.getTitle());
					}
				});
			}
		}
	}

	protected void removeProgressBar() {
		// When data is loaded, we can set the listeners on calendar's cells
		calendarFragment.setOnDateClickListener(new OnDateClickListener());
		// And hide the progress bar indicating we are ready to work
		handler.post(new Runnable() {
			@Override
			public void run() {
				progressBar.setVisibility(View.GONE);
			}
		});
	}

	protected void updateEventSetFromBackground(@NonNull final Dashboard dashboard) {
		final DateTime now = DateTime.now(Tools.TIME_ZONE);
		eventsLabels = calculateBackgroundsForEventDates(
				now.minusDays(365),
				now.plusDays(365),
				dashboard.getEvents()
		);
		handler.post(new Runnable() {
			@Override
			public void run() {
				calendarFragment.setBackgroundDrawableForDateTimes(eventsLabels);
				calendarFragment.refreshView();
			}
		});
	}

	protected HashMap<DateTime, Drawable> calculateBackgroundsForEventDates(
			@NonNull final DateTime minDate,
			@NonNull final DateTime maxDate,
			@Nullable ArrayList<Event> events) {

		if (events == null) {
			return new HashMap<>();
		}

		// Clear previous data-set to avoid double events
		dateAssociatedWithEvents.clear();

		// Hour, min, sec, msec, nsec must be reset to zero, otherwise
		// it won't be able to update backgrounds of calendar's cells
		DateTime iteratedDate = new DateTime(
				minDate.getYear(),
				minDate.getMonth(),
				minDate.getDay(),
				0, 0, 0, 0
		);
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


		final Float cellSize = Tools.getCalendarCellWidthInPixels(resources);
		// Now each date contains a full set of events
		for (final DateTime eventDate : dateAssociatedWithEvents.keySet()) {

			// Layers of "circle" - multicolored marks indicating that this date has an event
			final LinkedList<Drawable> eventCircleLayers = new LinkedList<>();
			// The first layer must be a default background of cell
			eventCircleLayers.addLast(resources.getDrawable(com.caldroid.R.drawable.cell_bg));

			// Iterated on all events that belong to current eventDate
			// and extract event's color (priority) (in eventCircleLayers)
			for (final Event eventByDate : dateAssociatedWithEvents.get(eventDate)) {
				if (eventByDate == null) {
					throw new IllegalArgumentException("Event is null");
				}
				// Limit the number of drawables
				if (eventCircleLayers.size() == MAX_DRAWABLES_EVENTS) {
					break;
				}

				switch (eventByDate.getPriority()) {
					case LOW:
						eventCircleLayers.addLast(resources.getDrawable(R.drawable.event_low));
						break;

					case MEDIUM:
						eventCircleLayers.addLast(resources.getDrawable(R.drawable.event_medium));
						break;

					case HIGH:
						eventCircleLayers.addLast(resources.getDrawable(R.drawable.event_high));
						break;

					case ULTRA_HIGH:
						eventCircleLayers.addLast(resources.getDrawable(R.drawable.event_ultra_high));
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
			backgrounds.put(eventDate, layerDrawable);
		}

		return backgrounds;
	}

	protected class OnDateClickListener implements SchedulerCaldroidFragment.OnDateClickListener {
		@Override
		public void onSelectDate(@NonNull Date date) {
			final Set<Event> eventsSet = dateAssociatedWithEvents.get(CalendarHelper.convertDateToDateTime(date));
			adapter.setNewDataset(new ArrayList<>(eventsSet));
			adapter.notifyDataSetChanged();
		}
	}
}
