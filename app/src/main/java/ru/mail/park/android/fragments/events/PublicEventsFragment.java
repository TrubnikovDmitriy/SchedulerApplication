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
import ru.mail.park.android.fragments.calendar.SchedulerCaldroidFragment;
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

import java.util.Date;


public class PublicEventsFragment extends EventsFragment {

	private boolean isCurrentUserIsAuthor = false;
	private Menu optionsMenu;


	public PublicEventsFragment() { }

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable final ViewGroup container,
	                         @Nullable final Bundle savedInstanceState) {

		final View view = super.onCreateView(inflater, container, savedInstanceState);
		progressBar.setVisibility(ProgressBar.VISIBLE);
		adapter.setListener(new OnCardEventClickListener());

		if (getArguments() != null) {
			dashboard = (Dashboard) getArguments().get(DASHBOARD);
		} else if (savedInstanceState != null) {
			dashboard = (Dashboard) savedInstanceState.get(DASHBOARD);
		}

		if (dashboard != null) {
			isCurrentUserIsAuthor = isCurrentUserIsAuthor(dashboard.getAuthorUID());
			// Loading events
			RealtimeDatabase
					.getPublicEvents(dashboard.getDashID())
					.addListenerForSingleValueEvent(new OnLoadPublicEvents(dashboard));
		}

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		optionsMenu = menu;

		// There are two versions of options menus:
		if (isCurrentUserIsAuthor) {
			// one for the author (editable)
			inflater.inflate(R.menu.private_events, menu);
		} else {
			// another one for other users (non-editable, but subscribable)
			inflater.inflate(R.menu.public_events, menu);
			menu.findItem(R.id.menu_unsubscribe).setVisible(false);
			// Set switcher for subscribe/unsubscribe
			RealtimeDatabase
					.getPublicWatchers(dashboard.getDashID())
					.addChildEventListener(new OnSubscribeListener());
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		final DatabaseReference mySubscribe;

		switch (item.getItemId()) {

			case R.id.menu_today:
				calendarFragment.moveToDateTime(DateTime.today(Tools.TIME_ZONE));
				return true;

			case R.id.menu_subscribe:
				if (user == null) {
					Toast.makeText(requireContext(), R.string.subscribe_access_db , Toast.LENGTH_SHORT).show();
					return true;
				}
				mySubscribe = RealtimeDatabase
						.getPublicWatchers(dashboard.getDashID())
						.child(user.getUid());
				mySubscribe.addListenerForSingleValueEvent(new RealtimeDatabase.FailEventListener());
				mySubscribe.setValue(user.getDisplayName());
				return true;

			case R.id.menu_unsubscribe:
				if (user == null) {
					Toast.makeText(requireContext(), R.string.subscribe_access_db , Toast.LENGTH_SHORT).show();
					return true;
				}
				mySubscribe = RealtimeDatabase
						.getPublicWatchers(dashboard.getDashID())
						.child(user.getUid());
				mySubscribe.addListenerForSingleValueEvent(new RealtimeDatabase.FailEventListener());
				mySubscribe.removeValue();
				return true;

			case R.id.menu_create_event:
				if (user == null || !isCurrentUserIsAuthor) {
					Toast.makeText(requireContext(), R.string.owner_access_db , Toast.LENGTH_SHORT).show();
					return true;
				}
				// Create fragment and set arguments
				final Fragment fragment = new EventFragmentEditCreate();
				final Bundle bundle = new Bundle();
				final DateTime today = DateTime.today(Tools.TIME_ZONE);

				bundle.putSerializable(EventFragmentEditCreate.DATE_BUNDLE, Tools.getDate(today));
				bundle.putString(EventFragmentEditCreate.DASH_ID_BUNDLE, dashboard.getDashID());
				bundle.putString(EventFragmentEditCreate.PATH_TO_NODE,
						RealtimeDatabase.getPathToPublicEvents(dashboard.getDashID()));
				fragment.setArguments(bundle);

				// Replace content in FrameLayout-container
				requireFragmentManager()
						.beginTransaction()
						.replace(R.id.container, fragment)
						.addToBackStack(null)
						.commit();
				return true;

			case R.id.menu_delete_dashboard:
				if (user == null || !isCurrentUserIsAuthor) {
					Toast.makeText(requireContext(), R.string.owner_access_db , Toast.LENGTH_SHORT).show();
					return true;
				}
				final DialogConfirm dialogConfirm = new DialogConfirm();
				dialogConfirm.setTitle(getResources().getString(R.string.delete_dashboard_confirm));
				dialogConfirm.setListener(new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						RealtimeDatabase.getPublicInfo(dashboard.getDashID()).removeValue();
						RealtimeDatabase.getPublicEvents(dashboard.getDashID()).removeValue();
						RealtimeDatabase.getPublicWatchers(dashboard.getDashID()).removeValue();
						requireFragmentManager().popBackStack();
					}
				});
				dialogConfirm.show(requireFragmentManager(), null);
				return true;

			case R.id.menu_rename_dashboard:
				if (user == null || !isCurrentUserIsAuthor) {
					Toast.makeText(requireContext(), R.string.owner_access_db , Toast.LENGTH_SHORT).show();
					return true;
				}
				dialogRename.show(requireFragmentManager(), DialogDashboardRename.DIALOG_TAG);
				return true;

			default:
				return false;
		}
	}


	private static boolean isCurrentUserIsAuthor(@NonNull final String authorUID) {
		final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		return (user != null) && (user.getUid().equals(authorUID));
	}

	@Override
	protected void initialDialogs() {
		// If user rotates the screen the click-listeners will be lost
		// We find an existing DialogFragment by tag to restore listeners after rotation
		dialogRename = (DialogDashboardRename) requireFragmentManager()
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
					Toast.makeText(requireContext(), R.string.too_short_title, Toast.LENGTH_SHORT).show();
					return;
				}

				dashboard.setTitle(newTitle);
				final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if (user == null) {
					Toast.makeText(requireContext(), R.string.owner_access_db, Toast.LENGTH_SHORT).show();
					return;
				}

				final DatabaseReference titleRef = RealtimeDatabase
						.getPublicInfo(dashboard.getDashID())
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


	class OnLoadPublicEvents extends OnLoadEventsFirebaseListener {

		OnLoadPublicEvents(@NonNull Dashboard dashboard) {
			super(dashboard);
		}

		@Override
		public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			super.onDataChange(dataSnapshot);

			// When data is loaded, we can set the listeners on calendar's cells
			final String dashID = dashboard.getDashID();
			calendarFragment.setOnDateClickListener(new OnDateClickListener());
			calendarFragment.setOnLongDateClickListener(
					new OnLongDateClickListenerWithAuthorCheck(dashID));
		}
	}

	class OnLongDateClickListenerWithAuthorCheck implements SchedulerCaldroidFragment.OnLongDateClickListener {

		@NonNull private final String dashID;

		OnLongDateClickListenerWithAuthorCheck(@NonNull String dashID) {
			this.dashID = dashID;
		}

		@Override
		public void onLongClickDate(@NonNull Date date) {
			if (!isCurrentUserIsAuthor) {
				Toast.makeText(requireContext(), R.string.owner_access_db, Toast.LENGTH_SHORT).show();
				return;
			}

			// Create fragment and set arguments
			final Fragment fragment = new EventFragmentEditCreate();
			final Bundle bundle = new Bundle();

			bundle.putSerializable(EventFragmentEditCreate.DATE_BUNDLE, date);
			bundle.putString(EventFragmentEditCreate.DASH_ID_BUNDLE, dashID);
			bundle.putString(EventFragmentEditCreate.PATH_TO_NODE,
					RealtimeDatabase.getPathToPublicEvents(dashID));
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			requireFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}
	}

	class OnSubscribeListener extends RealtimeDatabase.FirebaseEventListener {

		@Override
		public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			if (user == null) {
				 return;
			}
			// "Close" eye
			final String subscriberUID = dataSnapshot.getKey();
			if (user.getUid().equals(subscriberUID)) {
				optionsMenu.findItem(R.id.menu_subscribe).setVisible(false);
				optionsMenu.findItem(R.id.menu_unsubscribe).setVisible(true);
			}
		}

		@Override
		public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
			final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			if (user == null) {
				return;
			}
			// "Open" eye
			final String subscriberUID = dataSnapshot.getKey();
			if (user.getUid().equals(subscriberUID)) {
				optionsMenu.findItem(R.id.menu_unsubscribe).setVisible(false);
				optionsMenu.findItem(R.id.menu_subscribe).setVisible(true);
			}
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError) {
			final Context context = getContext();
			if (context != null) {
				Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	class OnCardEventClickListener implements EventAdapter.OnCardEventClickListener {

		@Override
		public void onEventCardClick(@NonNull final Event event) {

			if (!isCurrentUserIsAuthor) {
				Toast.makeText(requireContext(), R.string.owner_access_db, Toast.LENGTH_SHORT).show();
				return;
			}

			// Create fragment and set arguments
			final Fragment fragment = new EventFragmentEditUpdate();
			final Bundle bundle = new Bundle();

			bundle.putSerializable(EventFragmentEditUpdate.EVENT_BUNDLE, event);
			bundle.putSerializable(EventFragmentEditUpdate.DATE_BUNDLE, Tools.getDate(event.getTimestamp()));
			bundle.putString(EventFragmentEditUpdate.DASH_ID_BUNDLE, event.getDashID());
			bundle.putString(EventFragmentEditUpdate.PATH_TO_NODE,
					RealtimeDatabase.getPathToPublicEvents(event.getDashID()));
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