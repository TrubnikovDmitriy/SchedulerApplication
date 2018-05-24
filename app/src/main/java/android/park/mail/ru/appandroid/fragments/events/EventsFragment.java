package android.park.mail.ru.appandroid.fragments.events;

import android.os.Bundle;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.fragments.calendar.SchedulerCaldroidFragment;
import android.park.mail.ru.appandroid.models.Dashboard;
import android.park.mail.ru.appandroid.utils.ListenerWrapper;
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

import com.roomorama.caldroid.CaldroidFragment;

import java.util.LinkedList;
import java.util.List;


public abstract class EventsFragment extends Fragment {

	public static final String DASHBOARD_ID = "dash_id";
	protected static final String DASHBOARD = "dashboard_bundle";
	protected Dashboard dashboard;
	protected ProgressBar progressBar;
	@NonNull protected List<ListenerWrapper> wrappers = new LinkedList<>();

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

		final SchedulerCaldroidFragment caldroid = new SchedulerCaldroidFragment();

		final Bundle bundle = new Bundle();
		bundle.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
		caldroid.setArguments(bundle);

		caldroid.setEvents(dashboard.getEvents());


		getFragmentManager()
				.beginTransaction()
				.replace(R.id.caldroid_container, caldroid)
				.commit();

		// Set title of dashboard in ActionBar
		final ActionBar bar = ((AppCompatActivity)getActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(dashboard.getTitle());
		}
	}

}
