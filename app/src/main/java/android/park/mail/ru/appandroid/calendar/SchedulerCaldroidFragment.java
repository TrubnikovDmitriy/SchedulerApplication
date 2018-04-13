package android.park.mail.ru.appandroid.calendar;

import android.park.mail.ru.appandroid.models.Event;
import android.support.annotation.NonNull;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class SchedulerCaldroidFragment extends CaldroidFragment {

	private ArrayList<Event> events = new ArrayList<>();

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

	public void updateEvents(@NonNull ArrayList<Event> events) {
		this.events = events;
		for (CaldroidGridAdapter adapter : getDatePagerAdapters()) {
			((SchedulerCaldroidGridAdapter) adapter).setEvents(events);
		}
	}
}
