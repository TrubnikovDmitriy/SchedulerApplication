package ru.mail.park.android.fragments.events;

import android.os.Bundle;

import ru.mail.park.android.App;
import park.mail.ru.android.R;
import ru.mail.park.android.database.SchedulerDBHelper;
import ru.mail.park.android.fragments.calendar.CreateEventFragment;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.recycler.EventAdapter;
import ru.mail.park.android.utils.ListenerWrapper;
import ru.mail.park.android.utils.Tools;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.roomorama.caldroid.CalendarHelper;

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
	                         @Nullable final ViewGroup container,
	                         @Nullable final Bundle savedInstanceState) {

		final View view = super.onCreateView(inflater, container, savedInstanceState);

		progressBar.setVisibility(ProgressBar.VISIBLE);
		adapter.setListener(new OnCardEventClickListener());

		if (savedInstanceState == null) {
			final Long dashID = getArguments().getLong(DASHBOARD_ID);
			final ListenerWrapper wrapper =
					dbManager.selectDashboard(dashID, new OnLoadDashboardListener());
			wrappers.add(wrapper);

		} else {
			dashboard = (Dashboard) savedInstanceState.getSerializable(DASHBOARD_BUNDLE);
			if (dashboard != null) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						updateActionBarTitle();
						createCalendarFragment(null);
						calendarFragment.enableClicks();
						updateEventSetFromBackground(dashboard);
						removeProgressBar();
					}
				});
			}
		}

		return view;
	}

	class OnLoadDashboardListener implements
			SchedulerDBHelper.OnSelectCompleteListener<Dashboard> {

		@Override
		public void onSuccess(Dashboard data) {
			dashboard = data;
			updateActionBarTitle();
			createCalendarFragment(null);
			calendarFragment.enableClicks();
			updateEventSetFromBackground(data);
			removeProgressBar();
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

	class OnCardEventClickListener implements EventAdapter.OnCardEventClickListener {
		@Override
		public void onEventCardClick(@NonNull final Event event) {
			// Create fragment and set arguments
			final Fragment fragment = new CreateEventFragment();
			final Bundle bundle = new Bundle();

			bundle.putBoolean(CreateEventFragment.IS_NEW_BUNDLE, false);
			bundle.putSerializable(CreateEventFragment.EVENT_BUNDLE, event);
			bundle.putSerializable(CreateEventFragment.DATE_BUNDLE, Tools.getDate(event.getTimestamp()));
			bundle.putLong(CreateEventFragment.DASH_ID_BUNDLE, event.getDashID());
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
