package ru.mail.park.android.fragments.events;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import hirondelle.date4j.DateTime;
import ru.mail.park.android.App;
import park.mail.ru.android.R;
import ru.mail.park.android.database.SchedulerDBHelper;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.utils.ListenerWrapper;
import ru.mail.park.android.utils.Tools;

import android.support.annotation.NonNull;
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

		progressBar.setVisibility(ProgressBar.VISIBLE);
		if (savedInstanceState == null) {
			final Long dashID = getArguments().getLong(DASHBOARD_ID);
			final ListenerWrapper wrapper =
					dbManager.selectDashboard(dashID, new OnLoadDashboardListener());
			wrappers.add(wrapper);

		} else {
			dashboard = (Dashboard) savedInstanceState.getSerializable(DASHBOARD);
			if (dashboard != null) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						final DateTime now = DateTime.now(Tools.TIME_ZONE);
						eventsLabels = calculateCirclesForEventDates(
								now.minusDays(365),
								now.plusDays(365),
								dashboard.getEvents()
						);
						new Handler(Looper.getMainLooper()).post(new Runnable() {
							@Override
							public void run() {
								progressBar.setVisibility(ProgressBar.GONE);
							}
						});
					}
				});
				setCalendar(dashboard);
			}
		}

		return view;
	}

	@Override
	protected void setCalendar(@NonNull final Dashboard dashboard) {
		super.setCalendar(dashboard);

		calendarFragment.enableClicks();
	}

	class OnLoadDashboardListener implements
			SchedulerDBHelper.OnSelectCompleteListener<Dashboard> {

		private final Handler handler = new Handler(Looper.getMainLooper());

		@Override
		public void onSuccess(Dashboard data) {
			dashboard = data;
			final DateTime now = DateTime.now(Tools.TIME_ZONE);
			eventsLabels = calculateCirclesForEventDates(
					now.minusDays(365),
					now.plusDays(365),
					dashboard.getEvents()
			);
			handler.post(new Runnable() {
				@Override
				public void run() {
					setCalendar(dashboard);
					progressBar.setVisibility(ProgressBar.GONE);
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
