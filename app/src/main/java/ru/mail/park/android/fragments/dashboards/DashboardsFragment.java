package ru.mail.park.android.fragments.dashboards;

import android.content.Context;
import android.os.Bundle;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.park.android.R;
import ru.mail.park.android.database.RealtimeDatabase;
import ru.mail.park.android.dialogs.DialogDashboardCreator;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.recycler.DashboardAdapter;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;


public abstract class DashboardsFragment extends Fragment {

	protected DashboardAdapter adapter;
	protected DialogDashboardCreator dialogDashboardCreator;
	@NonNull protected ArrayList<Dashboard> dataset = new ArrayList<>();


	@BindView(R.id.progressbar_dash_load) protected ProgressBar progressBar;
	@BindView(R.id.recycle_dash) protected RecyclerView recyclerView;
	@BindView(R.id.fab) protected FloatingActionButton floatingButton;


	public DashboardsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	@CallSuper
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View view = inflater.inflate(
				R.layout.fragment_dashboards,
				container,
				false
		);
		ButterKnife.bind(this, view);

		floatingButton.setOnClickListener(new OnFloatingButtonClickListener());

		return view;
	}

	@Override
	@CallSuper
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		setHasOptionsMenu(true);
	}


	protected final void setActionBarTitle(@NonNull final String title) {
		final ActionBar bar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(title);
		}
	}


	class OnFloatingButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			// To prevent double tap
			if (!dialogDashboardCreator.isAdded()) {
				dialogDashboardCreator.show(
						requireFragmentManager(), DialogDashboardCreator.DIALOG_TAG);
			}
		}
	}

	class InfoDashboardChildListener extends RealtimeDatabase.FirebaseEventListener {

		@Override
		public void onChildAdded(@NonNull DataSnapshot dataSnapshot,
		                         @Nullable String previousChildName) {
			final Dashboard dashboard = RealtimeDatabase.parseDashboard(dataSnapshot);
			dataset.add(dashboard);
			adapter.notifyItemInserted(dataset.size() - 1);
			recyclerView.scrollToPosition(dataset.size() - 1);
		}

		@Override
		public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
			final Dashboard removed = RealtimeDatabase.parseDashboard(dataSnapshot);
			final int position = findIndexDashboardByID(dataset, removed.getDashID());
			if (position == -1) {
				Log.e(DashboardsFragment.this.toString(),"Failed to find Dashboard in dataset");
				return;
			}

			recyclerView.scrollToPosition(position);
			dataset.remove(position);
			adapter.notifyItemRemoved(position);
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError) {
			final Context context = getContext();
			if (context != null) {
				Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private static int findIndexDashboardByID(@Nullable final List<Dashboard> list,
	                                          @Nullable final String dashID) {

		int index = -1;
		if (list == null || dashID == null) {
			return index;
		}

		for (final Dashboard dashboard : list) {
			++index;
			if (dashID.equals(dashboard.getDashID())) {
				break;
			}
		}
		return index;
	}

	// TODO image for empty dataset
}
