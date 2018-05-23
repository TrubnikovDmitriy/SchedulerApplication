package android.park.mail.ru.appandroid.calendar;

import android.app.FragmentManager;
import android.os.Bundle;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.fragments.events.CreateEventFragment;
import android.park.mail.ru.appandroid.fragments.events.LocalEventsFragment;
import android.park.mail.ru.appandroid.models.Event;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


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


	final class SchedulerCaldroidListener extends CaldroidListener {

		@Override
		public void onSelectDate(Date date, View view) {
			onLongClickDate(date, view);
		}

		@Override
		public void onLongClickDate(Date date, View view) {

			// Create fragment and set arguments
			final Fragment fragment = new CreateEventFragment();

			// Replace content in FrameLayout-container
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}
	}
}
