package android.park.mail.ru.appandroid.fragments.events;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.park.mail.ru.appandroid.App;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.database.SchedulerDBHelper;
import android.park.mail.ru.appandroid.models.Dashboard;
import android.park.mail.ru.appandroid.utils.ListenerWrapper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import javax.inject.Inject;


public class LocalEventsFragment extends EventsFragment {

	@Inject
	public SchedulerDBHelper dbManager;

	public LocalEventsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		App.getComponent().inject(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		final View view = super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState == null) {
			progressBar.setVisibility(ProgressBar.VISIBLE);
			final Long dashID = getArguments().getLong(DASHBOARD_ID);
			final ListenerWrapper wrapper =
					dbManager.selectDashboard(dashID, new OnLoadDashboardListener());
			wrappers.add(wrapper);

		} else {
			dashboard = (Dashboard) savedInstanceState.getSerializable(DASHBOARD);
			if (dashboard != null) {
				setCalendar(dashboard);
			}
		}

		return view;
	}

	class OnLoadDashboardListener implements
			SchedulerDBHelper.OnSelectCompleteListener<Dashboard> {

		private final Handler handler = new Handler(Looper.getMainLooper());

		@Override
		public void onSuccess(Dashboard data) {
			dashboard = data;
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getContext(), R.string.success_load_events,
							Toast.LENGTH_SHORT).show();
					setCalendar(dashboard);
					progressBar.setVisibility(ProgressBar.INVISIBLE);
				}
			});
		}

		@Override
		public void onFailure(Exception exception) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getContext(), R.string.db_failure, Toast.LENGTH_LONG).show();
					progressBar.setVisibility(ProgressBar.INVISIBLE);
				}
			});
		}
	}
}
