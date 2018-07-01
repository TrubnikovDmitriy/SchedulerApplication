package ru.mail.park.android.fragments.events;

import android.os.Bundle;

import hirondelle.date4j.DateTime;
import ru.mail.park.android.App;
import ru.mail.park.android.R;
import ru.mail.park.android.network.ServerAPI;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.utils.ListenerWrapper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Response;
import ru.mail.park.android.utils.Tools;


public class ServerEventsFragment extends EventsFragment {

	@Inject
	public ServerAPI networkManager;

	public ServerEventsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		App.getComponent().inject(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable final ViewGroup container,
	                         @Nullable final Bundle savedInstanceState) {

		final View view = super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState == null) {
			progressBar.setVisibility(ProgressBar.VISIBLE);
			final String dashID = getArguments().getString(DASHBOARD_ID);
			ListenerWrapper wrapper = networkManager.getEvents(dashID, new LoadEventsListener());
			wrappers.add(wrapper);

		} else {
			dashboard = (Dashboard) savedInstanceState.getSerializable(DASHBOARD_BUNDLE);
			if (dashboard != null) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						updateActionBarTitle();
						createCalendarFragment(savedInstanceState);
						updateEventSetFromBackground(dashboard);
						removeProgressBar();
					}
				});
			}
		}

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.server_events, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.subscribe:
				networkManager.subscribe(dashboard.getDashID(), new ServerAPI.OnRequestCompleteListener<Dashboard>() {
					@Override
					public void onSuccess(Response<Dashboard> response, Dashboard body) {
						if (response.isSuccessful()) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(getContext(), R.string.subscribe, Toast.LENGTH_LONG).show();
								}
							});
						} else {
							onFailure(null);
						}
					}

					@Override
					public void onFailure(@Nullable Exception exception) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getContext(), R.string.parse_err, Toast.LENGTH_LONG).show();
							}
						});
					}
				});
				return true;

			default:
				return false;
		}
	}

	class LoadEventsListener implements ServerAPI.OnRequestCompleteListener<Dashboard> {

		@Override
		public void onSuccess(Response<Dashboard> response, Dashboard payload) {

			if (response.code() == 200) {
				dashboard = payload;
				updateActionBarTitle();;
				createCalendarFragment(null);
				updateEventSetFromBackground(dashboard);
				removeProgressBar();

			} else {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getContext(), R.string.network_failure,
								Toast.LENGTH_LONG).show();
						progressBar.setVisibility(ProgressBar.GONE);
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
