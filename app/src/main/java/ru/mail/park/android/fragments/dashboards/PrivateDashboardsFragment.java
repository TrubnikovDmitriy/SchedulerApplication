package ru.mail.park.android.fragments.dashboards;

import android.content.DialogInterface;
import android.os.Bundle;

import ru.mail.park.android.R;
import ru.mail.park.android.database.RealtimeDatabase;
import ru.mail.park.android.dialogs.DialogDashboardCreator;
import ru.mail.park.android.fragments.events.PrivateEventsFragment;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.recycler.DashboardAdapter;
import ru.mail.park.android.utils.Tools;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;


public class PrivateDashboardsFragment extends DashboardsFragment {

	public PrivateDashboardsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialDialogs();

		// Check authentication
		final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user == null) {
			Toast.makeText(requireContext(), R.string.auth_access_db, Toast.LENGTH_LONG).show();
			return;
		}

		// Listener for update list of private dashboards
		RealtimeDatabase
				.getPrivateInfoList(user.getUid())
				.addChildEventListener(new InfoDashboardChildListener());
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		final View view = super.onCreateView(inflater, container, savedInstanceState);
		setActionBarTitle(getResources().getString(R.string.local_dashes_title));

		adapter = new DashboardAdapter(dataset, new OnDashboardClickListener());
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
				getResources().getInteger(R.integer.span_count),
				StaggeredGridLayoutManager.VERTICAL
		));

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
	}

	private void initialDialogs() {
		// If user rotates the screen the click-listeners will be lost
		// We find an existing DialogFragment by tag to restore listeners after rotation
		dialogDashboardCreator = (DialogDashboardCreator) requireFragmentManager()
				.findFragmentByTag(DialogDashboardCreator.DIALOG_TAG);
		// otherwise - create new one
		if (dialogDashboardCreator == null) {
			dialogDashboardCreator = new DialogDashboardCreator();
		}

		dialogDashboardCreator.setOnPositiveClick(new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				// Check authentication
				final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
				if (user == null) {
					Toast.makeText(requireContext(), R.string.create_access_dashboard, Toast.LENGTH_LONG).show();
					return;
				}

				// Validation of title
				final String newTitle = dialogDashboardCreator.getInputText().trim();
				if (newTitle.length() < Tools.TITLE_MIN_LENGTH) {
					Toast.makeText(requireContext(), R.string.too_short_title, Toast.LENGTH_SHORT).show();
					return;
				}

				// Added to database
				final Dashboard dashboard = new Dashboard(
						user.getDisplayName(), user.getUid(),
						newTitle, null, null
				);

				final DatabaseReference referenceToList = RealtimeDatabase.getPrivateInfoList(user.getUid());
				final String dashID = RealtimeDatabase.createDashboard(
						referenceToList,
						dashboard.toPrivateMap(),
						new RealtimeDatabase.FailEventListener()
				);
				dashboard.setDashID(dashID);
			}
		});
	}

	class OnDashboardClickListener implements DashboardAdapter.OnDashboardClickListener {

		@Override
		public void onClick(@NonNull final Dashboard dashboard) {

			// Create fragment and set arguments
			final Fragment fragment = new PrivateEventsFragment();
			final Bundle bundle = new Bundle();
			bundle.putSerializable(PrivateEventsFragment.DASHBOARD, dashboard);
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
