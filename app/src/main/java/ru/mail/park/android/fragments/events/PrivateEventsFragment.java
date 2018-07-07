package ru.mail.park.android.fragments.events;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import hirondelle.date4j.DateTime;
import ru.mail.park.android.R;
import ru.mail.park.android.database.RealtimeDatabase;
import ru.mail.park.android.dialogs.DialogConfirm;
import ru.mail.park.android.dialogs.DialogDashboardRename;
import ru.mail.park.android.fragments.calendar.EventFragmentEditCreate;
import ru.mail.park.android.fragments.calendar.EventFragmentEditUpdate;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.recycler.EventAdapter;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;


public class PrivateEventsFragment extends EventsFragment {

	public PrivateEventsFragment() { }

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable final ViewGroup container,
	                         @Nullable final Bundle savedInstanceState) {

		final View view = super.onCreateView(inflater, container, savedInstanceState);

		progressBar.setVisibility(ProgressBar.VISIBLE);
		adapter.setListener(new OnCardEventClickListener());

		if (savedInstanceState == null && getArguments() != null) {
			dashboard = (Dashboard) getArguments().get(DASHBOARD);
		} else if (savedInstanceState != null) {
			dashboard = (Dashboard) savedInstanceState.get(DASHBOARD);
		}

		// Check authentication
		final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user == null) {
			Toast.makeText(requireContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
			return view;
		}

		if (dashboard != null) {
			RealtimeDatabase
					.getPrivateEvents(user.getUid(), dashboard.getDashID())
					.addListenerForSingleValueEvent(new OnLoadPrivateEvents(dashboard));
			initialDialogs();
		}

		return view;
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.private_events, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Check authentication
		final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user == null) {
			Toast.makeText(requireContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
			return false;
		}

		switch (item.getItemId()) {

			case R.id.menu_today:
				calendarFragment.moveToDateTime(DateTime.today(Tools.TIME_ZONE));
				return true;

			case R.id.menu_create_event:
				// Create fragment and set arguments
				final Fragment fragment = new EventFragmentEditCreate();
				final Bundle bundle = new Bundle();
				final DateTime today = DateTime.today(Tools.TIME_ZONE);

				bundle.putSerializable(EventFragmentEditCreate.DATE_BUNDLE, Tools.getDate(today));
				bundle.putString(EventFragmentEditCreate.DASH_ID_BUNDLE, dashboard.getDashID());
				bundle.putString(EventFragmentEditCreate.PATH_TO_NODE,
						RealtimeDatabase.getPathToPrivateEvents(user.getUid(), dashboard.getDashID()));
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
				isAlive.set(false);
				final DialogConfirm dialogConfirm = new DialogConfirm();
				dialogConfirm.setTitle(getResources().getString(R.string.delete_dashboard_confirm));
				dialogConfirm.setListener(new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						RealtimeDatabase.getPrivateInfo(user.getUid(), dashboard.getDashID()).removeValue();
						RealtimeDatabase.getPrivateEvents(user.getUid(), dashboard.getDashID()).removeValue();
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
		dialogRename = (DialogDashboardRename) requireFragmentManager()
				.findFragmentByTag(DialogDashboardRename.DIALOG_TAG);
		// otherwise - create new one
		if (dialogRename == null) {
			dialogRename = new DialogDashboardRename();
		}

		dialogRename.setOldTitle(dashboard.getTitle());
		dialogRename.setOnRenameListener(new DialogDashboardRename.OnRenameListener() {
			@Override
			public void onRename(@NonNull String newTitle) {
				// Validate title
				newTitle = newTitle.trim();
				if (newTitle.length() < Tools.TITLE_MIN_LENGTH) {
					Toast.makeText(requireContext(), R.string.too_short_title, Toast.LENGTH_SHORT).show();
					return;
				}

				final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if (user == null) {
					Toast.makeText(requireContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
					return;
				}
				dashboard.setTitle(newTitle);

				final DatabaseReference titleRef = RealtimeDatabase
						.getPrivateInfo(user.getUid(), dashboard.getDashID())
						.child(RealtimeDatabase.TITLE);
				titleRef.setValue(newTitle);
				titleRef.addListenerForSingleValueEvent(new ValueEventListener() {

					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						updateActionBarTitle();
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						final Context context = getContext();
						if (context != null) {
							Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		});
	}

	class OnCardEventClickListener implements EventAdapter.OnCardEventClickListener {
		@Override
		public void onEventCardClick(@NonNull final Event event) {

			final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			if (user == null) {
				Toast.makeText(requireContext(), R.string.auth_access_db, Toast.LENGTH_SHORT).show();
				return;
			}

			// Create fragment and set arguments
			final Fragment fragment = new EventFragmentEditUpdate();
			final Bundle bundle = new Bundle();

			bundle.putSerializable(EventFragmentEditUpdate.EVENT_BUNDLE, event);
			bundle.putSerializable(EventFragmentEditUpdate.DATE_BUNDLE, Tools.getDate(event.getTimestamp()));
			bundle.putString(EventFragmentEditUpdate.DASH_ID_BUNDLE, event.getDashID());
			bundle.putString(EventFragmentEditUpdate.PATH_TO_NODE,
					RealtimeDatabase.getPathToPrivateEvents(user.getUid(), event.getDashID()));
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			requireFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}
	}

	class OnLoadPrivateEvents extends OnLoadEventsFirebaseListener {

		OnLoadPrivateEvents(@NonNull Dashboard dashboard) {
			super(dashboard);
		}

		@Override
		public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			super.onDataChange(dataSnapshot);

			// When data is loaded, we can set the listeners on calendar's cells
			final String dashID = dashboard.getDashID();
			calendarFragment.setOnLongDateClickListener(new OnLongDateClickListener(dashID));
			calendarFragment.setOnDateClickListener(new OnDateClickListener());
		}
	}
}
