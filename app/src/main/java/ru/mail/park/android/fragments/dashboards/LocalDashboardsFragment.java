package ru.mail.park.android.fragments.dashboards;

import android.content.DialogInterface;
import android.os.Bundle;

import ru.mail.park.android.R;
import ru.mail.park.android.App;
import ru.mail.park.android.database.RealtimeDatabaseHelper;
import ru.mail.park.android.database.SchedulerDBHelper;
import ru.mail.park.android.dialogs.DialogDashboardCreator;
import ru.mail.park.android.fragments.events.LocalEventsFragment;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.ShortDashboard;
import ru.mail.park.android.recycler.DashboardAdapter;
import ru.mail.park.android.utils.Tools;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

import javax.inject.Inject;


public class LocalDashboardsFragment extends DashboardsFragment {

	@Inject
	public SchedulerDBHelper dbManager;
	private RealtimeDatabaseHelper dbHelper = new RealtimeDatabaseHelper();
	private DialogDashboardCreator dialogDashboardCreator;

	public LocalDashboardsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		App.getComponent().inject(this);
		super.onCreate(savedInstanceState);
		initialDialogs();

		// Check authentication
		final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user == null) {
			Toast.makeText(getContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
			return;
		}

		// Listener for update list of private dashboards
		dbHelper.getPrivateDashboards(user.getUid())
				.child(RealtimeDatabaseHelper.INFO)
				.addChildEventListener(new RealtimeDatabaseChildListener());
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_dashboards,
				container, false);
		setActionBarTitle(getResources().getString(R.string.local_dashes_title));
		recyclerView = view.findViewById(R.id.recycle_dash);

		progressBar = view.findViewById(R.id.progressbar_dash_load);
		progressBar.setVisibility(View.INVISIBLE); // TODO remove progress bar?

		floatingButton = view.findViewById(R.id.fab);
		floatingButton.setVisibility(View.VISIBLE);
		floatingButton.setOnClickListener(new onFloatingButtonClickListener());

		adapter = new DashboardAdapter(dataset, new OnDashboardClickListener());

		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
				getResources().getInteger(R.integer.span_count),
				StaggeredGridLayoutManager.VERTICAL
		));

		// Receiving data from DB
//		dbHelper.getShortDashboards(
//				Tools.checkAuthAndGetUser().getUid(),
//				new OnLoadLocalDashboards()
//		);

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
	}

	private void initialDialogs() {
		// If user rotates the screen the click-listeners will be lost
		// We find an existing DialogFragment by tag to restore listeners after rotation
		dialogDashboardCreator = (DialogDashboardCreator) getFragmentManager()
				.findFragmentByTag(DialogDashboardCreator.DIALOG_TAG);
		// otherwise - create new one
		if (dialogDashboardCreator == null) {
			dialogDashboardCreator = new DialogDashboardCreator();
		}

		dialogDashboardCreator.setOnPositiveClick(new DialogInterface.OnClickListener() {

			private static final int TITLE_MIN_LENGTH = 3;

			@Override
			public void onClick(DialogInterface dialog, int which) {

				// Check authentication
				final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if (user == null) {
					Toast.makeText(getContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
					return;
				}

				// Validation of title
				final String newTitle = dialogDashboardCreator.getInputText().trim();
				if (newTitle.length() < TITLE_MIN_LENGTH) {
					Toast.makeText(getContext(), R.string.fcm_update_event, Toast.LENGTH_SHORT).show();
					return;
				}

				// Added to database
				final Dashboard dashboard = new Dashboard(
						user.getDisplayName(), user.getUid(),
						newTitle, null, null
				);

				dbHelper.createDashboard(
						dashboard,
						new OnFailureListener() {
							@Override
							public void onFailure(@NonNull Exception e) {
								Log.e("createDashboard", "OnFailureListener", e);
								Toast.makeText(getContext(), R.string.db_failure, Toast.LENGTH_LONG).show();
							}
						}
				);
			}
		});
	}

	@NonNull
	private ShortDashboard getShortDashFromDataSnapshot(@NonNull final DataSnapshot dataSnapshot) {
		final ShortDashboard dashboard = dataSnapshot.getValue(ShortDashboard.class);
		if (dashboard == null) {
			throw new NullPointerException();
		}
		dashboard.setDashID(dataSnapshot.getKey());
		return dashboard;
	}


	class onFloatingButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// Check authentication
			final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			if (user == null) {
				Toast.makeText(getContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
				return;
			}

			// To prevent double tap
			if (!dialogDashboardCreator.isAdded()) {
				dialogDashboardCreator.show(
						getFragmentManager(), DialogDashboardCreator.DIALOG_TAG);
			}
		}
	}

	class OnDashboardClickListener implements DashboardAdapter.OnDashboardClickListener {

		@Override
		public void onClick(@NonNull final ShortDashboard dashboard) {

			// Create fragment and set arguments
			final Fragment fragment = new LocalEventsFragment();
			final Bundle bundle = new Bundle();
			bundle.putSerializable(LocalEventsFragment.SHORT_DASHBOARD, dashboard);
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			if (getFragmentManager() == null) {
				throw new NullPointerException("Can't get FragmentManager");
			}
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}
	}

	class OnLoadLocalDashboards implements ValueEventListener {
		@Override
		public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
			final Iterator<DataSnapshot> iterable = dataSnapshot.getChildren().iterator();
			final ArrayList<ShortDashboard> dashboards = new ArrayList<>();
			while (iterable.hasNext()) {
				final DataSnapshot childSnapshot = iterable.next();
				final ShortDashboard dashboard = getShortDashFromDataSnapshot(childSnapshot);
				dashboards.add(dashboard);
			}

			if (dashboards.isEmpty()) {
				Toast.makeText(getContext(), R.string.empty_dataset, Toast.LENGTH_LONG).show();
			}
			progressBar.setVisibility(ProgressBar.INVISIBLE);
			updateDataset(dashboards);

		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError) {
			Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
			progressBar.setVisibility(ProgressBar.INVISIBLE);
			updateDataset(null);
		}
	}

	class RealtimeDatabaseChildListener implements ChildEventListener {
		@Override
		public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
			if (dataset == null) {
				dataset = new ArrayList<>();
			}
			final ShortDashboard dashboard = getShortDashFromDataSnapshot(dataSnapshot);
			adapter.addItem(dashboard);
			dataset.add(dashboard);
			recyclerView.scrollToPosition(dataset.size() - 1);
		}

		@Override
		public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
			try {
				final ShortDashboard removed = getShortDashFromDataSnapshot(dataSnapshot);
				final int position = dataset.indexOf(removed);

				recyclerView.scrollToPosition(position);
				adapter.removeItem(position);
			} catch (Exception e) {
				Log.e("e", "delete", e);
			}
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError) {
			Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
		}

		// Other functions is not needed since it can't invoke in local-dashboards
		@Override
		public void onChildChanged(@NonNull DataSnapshot ds, @Nullable String s) { }
		@Override
		public void onChildMoved(@NonNull DataSnapshot ds, @Nullable String s) {  }
	}

}
