package android.park.mail.ru.appandroid.fragments.calendar;

import android.os.Bundle;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.calendar.SchedulerCaldroidGridAdapter;
import android.park.mail.ru.appandroid.models.Event;
import android.park.mail.ru.appandroid.utils.Tools;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import hirondelle.date4j.DateTime;


public class SchedulerCaldroidFragment extends CaldroidFragment {

	private ArrayList<Event> events = new ArrayList<>();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setCaldroidListener(new SchedulerCaldroidListener());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public SchedulerCaldroidGridAdapter getNewDatesGridAdapter(int month, int year) {
		return new SchedulerCaldroidGridAdapter(
				getActivity(),
				month, year,
				getCaldroidData(),
				new HashMap<String, Object>(),
				events
		);
	}

	public void setEvents(@NonNull ArrayList<Event> events) {
		this.events = events;
	}

	@SuppressWarnings("unused")
	public void updateEvents(@NonNull ArrayList<Event> events) {
		this.events = events;
		for (CaldroidGridAdapter adapter : getDatePagerAdapters()) {
			((SchedulerCaldroidGridAdapter) adapter).setEvents(events);
		}
	}

	@Nullable
	private Event getEventByDate(@NonNull final DateTime date) {
		for (final Event event : events) {
			final DateTime eventDate = Tools.getDate(event);
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

			final Event event = getEventByDate(Tools.getDate(date));
			bundle.putBoolean(CreateEventFragment.IS_NEW_BUNDLE, (event == null));
			bundle.putSerializable(CreateEventFragment.EVENT_BUNDLE, event);
			bundle.putSerializable(CreateEventFragment.DATE_BUNDLE, date);
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}
	}
}
