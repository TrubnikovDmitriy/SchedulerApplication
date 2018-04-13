package android.park.mail.ru.appandroid.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.calendar.SchedulerCaldroidFragment;
import android.park.mail.ru.appandroid.network.ServerAPI;
import android.park.mail.ru.appandroid.models.Dashboard;
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


public class ServerEventsFragment extends Fragment {

	public static final String DASHBOARD_ID = "dash_id";
	private static final String DASHBOARD = "dashboard_bundle";
	private Dashboard dashboard;
	private ProgressBar progressBar;

	public ServerEventsFragment() { }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_events, container, false);
		progressBar = view.findViewById(R.id.progressbar_event_load);

		if (savedInstanceState == null) {
			progressBar.setVisibility(ProgressBar.VISIBLE);
			final Long dashID = getArguments().getLong(DASHBOARD_ID);
			ServerAPI.getInstance().getEvents(dashID, new LoadEventsListener());

		} else {
			dashboard = (Dashboard) savedInstanceState.getSerializable(DASHBOARD);
			setCalendar(dashboard);
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(DASHBOARD, dashboard);
		super.onSaveInstanceState(outState);
	}


	private void setCalendar(final Dashboard dashboard) {

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

	class LoadEventsListener implements ServerAPI.OnRequestCompleteListener<Dashboard> {

		private final Handler handler = new Handler(Looper.getMainLooper());

		@Override
		public void onSuccess(Response<Dashboard> response, Dashboard payload) {
			if (response.code() == 200) {
				dashboard = payload;
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getContext(), R.string.success_load_events,
								Toast.LENGTH_SHORT).show();
						progressBar.setVisibility(ProgressBar.INVISIBLE);
						setCalendar(dashboard);
					}
				});
			} else {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getContext(), R.string.not_success_response,
								Toast.LENGTH_LONG).show();
						progressBar.setVisibility(ProgressBar.INVISIBLE);
					}
				});
			}
		}

		@Override
		public void onFailure(Exception exception) {
			Log.e("Network", "Parse or network", exception);

			// IOException - network problem
			if (exception instanceof IOException) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getContext(), R.string.network_err, Toast.LENGTH_LONG).show();
						progressBar.setVisibility(ProgressBar.INVISIBLE);
					}
				});
				return;
			}

			// RuntimeException - parse problem
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getContext(), R.string.parse_err, Toast.LENGTH_LONG).show();
					progressBar.setVisibility(ProgressBar.INVISIBLE);
				}
			});
		}
	}

}
