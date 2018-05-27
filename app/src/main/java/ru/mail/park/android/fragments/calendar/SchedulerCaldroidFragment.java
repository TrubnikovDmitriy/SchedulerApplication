package ru.mail.park.android.fragments.calendar;

import android.os.Bundle;
import park.mail.ru.android.R;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.utils.Tools;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import com.roomorama.caldroid.CalendarHelper;

import java.util.ArrayList;
import java.util.Date;
import hirondelle.date4j.DateTime;


public class SchedulerCaldroidFragment extends CaldroidFragment {

	public static final String DASH_ID_BUNDLE = "DASH_ID_BUNDLE";
	private static DateTime currentMonth = DateTime.now(Tools.TIME_ZONE);
	private boolean isFirst = true;


	private ArrayList<Event> events = new ArrayList<>();
	private Long dashID;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			dashID = savedInstanceState.getLong(DASH_ID_BUNDLE);
		}
		if (getArguments() != null) {
			dashID = getArguments().getLong(DASH_ID_BUNDLE);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setCalendarDateTime(currentMonth);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(DASH_ID_BUNDLE, dashID);
	}


	public void setEvents(@NonNull ArrayList<Event> events) {
		this.events = events;
	}

	public void enableClicks() {
		this.setCaldroidListener(new SchedulerCaldroidListener());
	}

	@Nullable
	private Event getEventByDate(@NonNull final DateTime date) {
		for (final Event event : events) {
			final DateTime eventDate = Tools.getDateTime(event);
			if (date.isSameDayAs(eventDate)) {
				return event;
			}
		}
		return null;
	}

	final class SchedulerCaldroidListener extends CaldroidListener {

		@Override
		public void onSelectDate(Date date, View view) {
			onLongClickDate(date, view);
		}

		@Override
		public void onLongClickDate(Date date, View view) {

			// Create fragment and set arguments
			final Fragment fragment = new CreateEventFragment();
			final Bundle bundle = new Bundle();

			final Event event = getEventByDate(CalendarHelper.convertDateToDateTime(date));
			bundle.putBoolean(CreateEventFragment.IS_NEW_BUNDLE, (event == null));
			bundle.putSerializable(CreateEventFragment.EVENT_BUNDLE, event);
			bundle.putSerializable(CreateEventFragment.DATE_BUNDLE, date);
			bundle.putLong(CreateEventFragment.DASH_ID_BUNDLE, dashID);
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}

		@Override
		public void onChangeMonth(int month, int year) {
			if (isFirst) {
				isFirst = false;
			} else {
				currentMonth = new DateTime(year, month, 1, 0, 0, 0, 0);
			}
		}
	}
}
