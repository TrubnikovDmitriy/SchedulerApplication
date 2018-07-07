package ru.mail.park.android.fragments.events;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import hirondelle.date4j.DateTime;
import ru.mail.park.android.R;
import ru.mail.park.android.database.RealtimeDatabase;
import ru.mail.park.android.dialogs.DialogDashboardRename;
import ru.mail.park.android.fragments.calendar.EventFragmentEditCreate;
import ru.mail.park.android.fragments.calendar.SchedulerCaldroidFragment;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.recycler.EventAdapter;
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
import android.widget.Toast;

import com.google.common.collect.HashMultimap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CalendarHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public abstract class EventsFragment extends Fragment {

	protected final ExecutorService executor = Executors.newSingleThreadExecutor();

	private static final int MAX_DRAWABLES_EVENTS = 8;
	public static final String DASHBOARD = "DASHBOARD";
	public static final String CALENDAR_BUNDLE = "CALENDAR_BUNDLE";

	protected final Handler handler = new Handler(Looper.getMainLooper());
	protected DialogDashboardRename dialogRename;

	@BindView(R.id.progressbar_event_load) ProgressBar progressBar;
	@BindView(R.id.recycler_events) RecyclerView recyclerView;
	protected SchedulerCaldroidFragment calendarFragment;
	protected EventAdapter adapter;
	protected Dashboard dashboard;
	protected Resources resources;

	// This flag is necessary to stop heavy background processes
	protected AtomicBoolean isAlive = new AtomicBoolean(true);
	@Nullable protected HashMap<DateTime, Drawable> eventDrawables;
	@NonNull protected HashMultimap<DateTime, Event> dateAssociatedWithEvents = HashMultimap.create();


	public EventsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		initialDialogs();
	}

	@Override
	@CallSuper
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_events, container, false);
		ButterKnife.bind(this, view);

		resources = view.getResources();

		adapter = new EventAdapter(
				resources.getStringArray(R.array.event_priority),
				resources.getStringArray(R.array.event_type)
		);
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		return view;
	}

	@Override
	@CallSuper
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DASHBOARD, dashboard);
		setHasOptionsMenu(true);
		if (calendarFragment != null) {
			calendarFragment.saveStatesToKey(outState, CALENDAR_BUNDLE);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		isAlive.set(false);
		progressBar.setVisibility(View.INVISIBLE);
	}

	protected abstract void initialDialogs();

	protected void createCalendarFragment(@Nullable Bundle savedInstanceState) {

		calendarFragment = new SchedulerCaldroidFragment();

		if (savedInstanceState != null) {
			calendarFragment.restoreStatesFromKey(savedInstanceState, DASHBOARD);
		} else {
			final Bundle bundle = new Bundle();
			bundle.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
			bundle.putString(SchedulerCaldroidFragment.DASH_ID_BUNDLE, dashboard.getDashID());
			bundle.putBoolean(SchedulerCaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
			calendarFragment.setArguments(bundle);
		}

		requireFragmentManager()
				.beginTransaction()
				.replace(R.id.caldroid_container, calendarFragment)
				.commit();
	}

	protected void updateActionBarTitle() {
		// Set title of dashboard in ActionBar
		final Activity activity = requireActivity();
		final ActionBar bar = ((AppCompatActivity) activity).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(dashboard.getTitle());
		}
	}

	protected void updateEventSetFromBackground(@NonNull final Dashboard dashboard) {
		isAlive.set(true);
		final DateTime now = DateTime.now(Tools.TIME_ZONE);
		eventDrawables = calculateBackgroundsForEventDates(
				now.minusDays(365),
				now.plusDays(365),
				dashboard.getEvents()
		);
		handler.post(new Runnable() {
			@Override
			public void run() {
				progressBar.setVisibility(View.INVISIBLE);
				calendarFragment.setBackgroundDrawableForDateTimes(eventDrawables);
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
			// Check, is it still relevant
			if (!isAlive.get()) {
				return new HashMap<>();
			}
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
			if (!isAlive.get()) {
				return new HashMap<>();
			}
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

	protected class OnLongDateClickListener implements SchedulerCaldroidFragment.OnLongDateClickListener {

		@NonNull private final String dashID;

		OnLongDateClickListener(@NonNull String dashID) {
			this.dashID = dashID;
		}

		@Override
		public void onLongClickDate(@NonNull final Date date) {

			final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			if (user == null) {
				Toast.makeText(requireContext(), R.string.auth_access_db, Toast.LENGTH_SHORT).show();
				return;
			}

			// Create fragment and set arguments
			final Fragment fragment = new EventFragmentEditCreate();
			final Bundle bundle = new Bundle();

			bundle.putSerializable(EventFragmentEditCreate.DATE_BUNDLE, date);
			bundle.putString(EventFragmentEditCreate.DASH_ID_BUNDLE, dashID);
			bundle.putString(EventFragmentEditCreate.PATH_TO_NODE,
					RealtimeDatabase.getPathToPrivateEvents(user.getUid(), dashID));
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			requireFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}
	}

	abstract class OnLoadEventsFirebaseListener implements ValueEventListener {

		@NonNull final Dashboard dashboard;

		OnLoadEventsFirebaseListener(@NonNull Dashboard dashboard) {
			this.dashboard = dashboard;
		}

		@Override
		public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					final ArrayList<Event> events = new ArrayList<>();
					for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
						Event event = RealtimeDatabase.parseEvent(snapshot);
						events.add(event);
					}
					dashboard.setEvents(events);
					updateEventSetFromBackground(dashboard);
				}
			});
			updateActionBarTitle();
			createCalendarFragment(null);
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError) {
			final Context context = getContext();
			if (context != null) {
				Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
			}
			progressBar.setVisibility(ProgressBar.INVISIBLE);
		}
	}
}
