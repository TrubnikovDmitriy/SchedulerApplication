package ru.mail.park.android.fragments.events;

import android.content.DialogInterface;
import android.os.Bundle;

import hirondelle.date4j.DateTime;
import retrofit2.Response;
import ru.mail.park.android.App;
import ru.mail.park.android.R;
import ru.mail.park.android.database.RealtimeDatabaseHelper;
import ru.mail.park.android.dialogs.DialogConfirm;
import ru.mail.park.android.dialogs.DialogDashboardRename;
import ru.mail.park.android.fragments.calendar.CreateEventFragment;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.models.ShortDashboard;
import ru.mail.park.android.network.ServerAPI;
import ru.mail.park.android.recycler.EventAdapter;
import ru.mail.park.android.utils.Tools;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import javax.inject.Inject;


public class LocalEventsFragment extends EventsFragment {

	@Inject public ServerAPI serverAPI;
	private final RealtimeDatabaseHelper dbHelper = new RealtimeDatabaseHelper();

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
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable final ViewGroup container,
	                         @Nullable final Bundle savedInstanceState) {

		final View view = super.onCreateView(inflater, container, savedInstanceState);

		progressBar.setVisibility(ProgressBar.VISIBLE);
		adapter.setListener(new OnCardEventClickListener());

		if (savedInstanceState == null && getArguments() != null) {
			final ShortDashboard shortDashboard = (ShortDashboard) getArguments().get(SHORT_DASHBOARD);
			if (shortDashboard != null) {
				dashboard = new Dashboard();
				dashboard.setTitle(shortDashboard.getTitle());
				dashboard.setDashID(shortDashboard.getDashID());
			}
		} else if (savedInstanceState != null) {
			dashboard = (Dashboard) savedInstanceState.getSerializable(DASHBOARD_BUNDLE);
		}



		// Check authentication
		final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user == null) {
			Toast.makeText(getContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
			return view;
		}

		if (dashboard != null) {
			dbHelper.getDashboard(
					user.getUid(),
					dashboard.getDashID(),
					new OnLoadEventsFirebaseListener(dashboard)
			);
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

			case R.id.menu_create_event:
				// Create fragment and set arguments
				final Fragment fragment = new CreateEventFragment();
				final Bundle bundle = new Bundle();
				final DateTime today = DateTime.today(Tools.TIME_ZONE);

				bundle.putBoolean(CreateEventFragment.IS_NEW_BUNDLE, true);
				bundle.putSerializable(CreateEventFragment.DATE_BUNDLE, Tools.getDate(today));
				bundle.putString(CreateEventFragment.DASH_ID_BUNDLE, dashboard.getDashID());
				fragment.setArguments(bundle);

				// Replace content in FrameLayout-container
				requireFragmentManager()
						.beginTransaction()
						.replace(R.id.container, fragment)
						.addToBackStack(null)
						.commit();
				return true;

			case R.id.menu_rename_dashboard:
				dialogRename.show(requireFragmentManager(), DialogDashboardRename.DIALOG_TAG);
				return true;

			case R.id.menu_delete_dashboard:
				// Check authentication
				final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if (user == null) {
					Toast.makeText(getContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
					return true;
				}

				isAlive.set(false);
				final DialogConfirm dialogConfirm = new DialogConfirm();
				dialogConfirm.setTitle(getResources().getString(R.string.delete_dashboard_confirm));
				dialogConfirm.setListener(new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dbHelper.getPrivateDashInfo(user.getUid(), dashboard.getDashID()).removeValue();
						dbHelper.getPrivateDashEvents(user.getUid(), dashboard.getDashID()).removeValue();
						requireFragmentManager().popBackStack();
					}
				});
				dialogConfirm.show(requireFragmentManager(), null);
				return true;

			default:
				return false;
		}
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		setHasOptionsMenu(true);
	}

	private void initialDialogs() {
		// If user rotates the screen the click-listeners will be lost
		// We find an existing DialogFragment by tag to restore listeners after rotation
		if (getFragmentManager() == null) {
			Log.e("Null", "Can't get FragmentManager");
			return;
		}
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
					Toast.makeText(getContext(), R.string.fcm_update_event, Toast.LENGTH_SHORT).show();
					return;
				}

				dashboard.setTitle(newTitle);
				final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if (user == null) {
					Toast.makeText(getContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
					return;
				}

				final DatabaseReference titleRef = dbHelper
						.getPrivateDashInfo(user.getUid(), dashboard.getDashID())
						.child(RealtimeDatabaseHelper.TITLE);
				titleRef.setValue(newTitle);
				titleRef.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						updateActionBarTitle();
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}

	class OnLoadEventsFirebaseListener implements ValueEventListener {

		@NonNull final Dashboard dashboard;

		OnLoadEventsFirebaseListener(@NonNull Dashboard dashboard) {
			this.dashboard = dashboard;
		}

		@Override
		public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			final ArrayList<Event> events = new ArrayList<>();
			for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {
				Event event = dbHelper.parseEvent(snapshot);
				events.add(event);
			}
			dashboard.setEvents(events);
			updateActionBarTitle();
			createCalendarFragment(null);
			calendarFragment.enableLongClicks();
			updateEventSetFromBackground(dashboard);
			removeProgressBar();
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError) {
			Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
			progressBar.setVisibility(ProgressBar.INVISIBLE);
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
			bundle.putString(CreateEventFragment.DASH_ID_BUNDLE, event.getDashID());
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			requireFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}
	}
}
