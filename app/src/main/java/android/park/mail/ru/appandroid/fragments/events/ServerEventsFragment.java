package android.park.mail.ru.appandroid.fragments.events;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.calendar.SchedulerCaldroidFragment;
import android.park.mail.ru.appandroid.database.SchedulerDBHelper;
import android.park.mail.ru.appandroid.network.ServerAPI;
import android.park.mail.ru.appandroid.models.Dashboard;
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


public class ServerEventsFragment extends EventsFragment {

	public ServerEventsFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		final View view = super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState == null) {
			progressBar.setVisibility(ProgressBar.VISIBLE);
			final Long dashID = getArguments().getLong(DASHBOARD_ID);
			ServerAPI.getInstance().getEvents(dashID, new LoadEventsListener());
			// TODO Singleton

		} else {
			dashboard = (Dashboard) savedInstanceState.getSerializable(DASHBOARD);
			if (dashboard != null) {
				setCalendar(dashboard);
			}
		}

		return view;
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
						Toast.makeText(getContext(), R.string.network_failure,
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
