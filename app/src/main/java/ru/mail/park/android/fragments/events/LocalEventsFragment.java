package ru.mail.park.android.fragments.events;

import android.content.DialogInterface;
import android.os.Bundle;

import hirondelle.date4j.DateTime;
import ru.mail.park.android.App;
import park.mail.ru.android.R;
import ru.mail.park.android.database.SchedulerDBHelper;
import ru.mail.park.android.dialogs.DialogConfirm;
import ru.mail.park.android.dialogs.DialogDashboardRename;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import javax.inject.Inject;


public class LocalEventsFragment extends EventsFragment {

	@Inject
	public SchedulerDBHelper dbManager;
	private DialogDashboardRename dialogRename;

	public LocalEventsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		App.getComponent().inject(this);
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		initialDialogs();
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
			final ListenerWrapper wrapper = dbManager.selectDashboard(dashID, new OnLoadDashboardListener());
			wrappers.add(wrapper);
		} else {
			dashboard = (Dashboard) savedInstanceState.getSerializable(DASHBOARD_BUNDLE);
			if (dashboard != null) {
				final ListenerWrapper wrapper = dbManager.selectDashboard(dashboard.getDashID(), new OnLoadDashboardListener());
				wrappers.add(wrapper);
			}
		}

		return view;
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.local_events, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.menu_today:
				calendarFragment.moveToDateTime(DateTime.today(Tools.TIME_ZONE));
				return true;

			case R.id.menu_upload_dashboard:
				return true;

			case R.id.menu_create_event:
				// Create fragment and set arguments
				final Fragment fragment = new CreateEventFragment();
				final Bundle bundle = new Bundle();
				final DateTime today = DateTime.today(Tools.TIME_ZONE);

				bundle.putBoolean(CreateEventFragment.IS_NEW_BUNDLE, true);
				bundle.putSerializable(CreateEventFragment.DATE_BUNDLE, Tools.getDate(today));
				bundle.putLong(CreateEventFragment.DASH_ID_BUNDLE, dashboard.getDashID());
				fragment.setArguments(bundle);

				// Replace content in FrameLayout-container
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.container, fragment)
						.addToBackStack(null)
						.commit();
				return true;

			case R.id.menu_rename_dashboard:
				dialogRename.show(getFragmentManager(), DialogDashboardRename.DIALOG_TAG);
				return true;

			case R.id.menu_delete_dashboard:
				isAlive.set(false);
				final DialogConfirm dialogConfirm = new DialogConfirm();
				dialogConfirm.setTitle(getResources().getString(R.string.delete_dashboard_confirm));
				dialogConfirm.setListener(new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dbManager.deleteDashboard(
								dashboard.getDashID(),
								new OnDeleteDashboardListener()
						);
					}
				});
				dialogConfirm.show(getFragmentManager(), null);
				return true;

			default:
				return false;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		setHasOptionsMenu(true);
	}

	private void initialDialogs() {
		// If user rotates the screen the click-listeners will be lost
		// We find an existing DialogFragment by tag to restore listeners after rotation
		dialogRename = (DialogDashboardRename) getFragmentManager()
				.findFragmentByTag(DialogDashboardRename.DIALOG_TAG);
		// otherwise - create new one
		if (dialogRename == null) {
			dialogRename = new DialogDashboardRename();
		}

		dialogRename.setOnRenameListener(new DialogDashboardRename.OnRenameListener() {
			@Override
			public void onRename(@NonNull String newTitle) {
				// Validate title
				newTitle = newTitle.trim();
				if (newTitle.length() < Tools.TITLE_MIN_LENGTH) {
					Toast.makeText(getContext(), R.string.too_short_title, Toast.LENGTH_SHORT).show();
					return;
				}

				dashboard.setTitle(newTitle);
				final ListenerWrapper wrapper = dbManager.renameDashboard(dashboard, new OnRenameDashboardListener());
				wrappers.add(wrapper);
			}
		});
	}

	class OnLoadDashboardListener implements SchedulerDBHelper.OnSelectCompleteListener<Dashboard> {

		@Override
		public void onSuccess(Dashboard data) {
			dashboard = data;
			updateActionBarTitle();
			createCalendarFragment(null);
			calendarFragment.enableLongClicks();
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

	class OnDeleteDashboardListener implements SchedulerDBHelper.OnDeleteCompleteListener {

		@Override
		public void onSuccess(int numberOfRowsAffected) {
			if (numberOfRowsAffected != 1) {
				onFailure(null);
			}
			getFragmentManager().popBackStack();
		}

		@Override
		public void onFailure(@Nullable Exception exception) {
			Toast.makeText(getContext(), R.string.db_failure, Toast.LENGTH_LONG).show();
		}
	}

	class OnRenameDashboardListener implements SchedulerDBHelper.OnUpdateCompleteListener {

		@Override
		public void onSuccess(int numberOfRowsAffected) {
			if (numberOfRowsAffected != 1) {
				onFailure(null);
			}
			handler.post(new Runnable() {
				@Override
				public void run() {
					updateActionBarTitle();
				}
			});
		}

		@Override
		public void onFailure(@Nullable Exception exception) {
			Toast.makeText(getContext(), R.string.db_failure, Toast.LENGTH_LONG).show();
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
