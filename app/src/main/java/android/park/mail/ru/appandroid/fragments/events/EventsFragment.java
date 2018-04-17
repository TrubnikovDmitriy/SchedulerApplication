package android.park.mail.ru.appandroid.fragments.events;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.calendar.SchedulerCaldroidFragment;
import android.park.mail.ru.appandroid.models.Dashboard;
import android.park.mail.ru.appandroid.network.ServerAPI;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;

import java.io.IOException;

import retrofit2.Response;


public abstract class EventsFragment extends Fragment {

	public static final String DASHBOARD_ID = "dash_id";
	protected static final String DASHBOARD = "dashboard_bundle";
	protected Dashboard dashboard;
	protected ProgressBar progressBar;

	public EventsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_events, container, false);
		progressBar = view.findViewById(R.id.progressbar_event_load);
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		setHasOptionsMenu(true);
		outState.putSerializable(DASHBOARD, dashboard);
		super.onSaveInstanceState(outState);
	}


	protected void setCalendar(@NonNull final Dashboard dashboard) {

		final SchedulerCaldroidFragment caldroid = new SchedulerCaldroidFragment();
		caldroid.setEvents(dashboard.getEvents());

		Bundle args = new Bundle();
		args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS, false);
		caldroid.setArguments(args);

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.caldroid_container, caldroid)
				.commit();
	}

}
