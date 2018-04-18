package android.park.mail.ru.appandroid.fragments.dashboards;

import android.os.Bundle;
import android.park.mail.ru.appandroid.models.ShortDashboard;
import android.park.mail.ru.appandroid.recycler.DashboardAdapter;
import android.park.mail.ru.appandroid.utils.ListenerWrapper;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public abstract class DashboardsFragment extends Fragment {

	public static final String DATASET = "dataset_bundle";

	// TODO ButterKnife @Bind
	protected DashboardAdapter adapter;
	protected ProgressBar progressBar;
	protected ArrayList<ShortDashboard> dataset;
	protected RecyclerView recyclerView;
	@NonNull protected List<ListenerWrapper> wrappers = new LinkedList<>();


	public DashboardsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	@CallSuper
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DATASET, dataset == null ? null : dataset.toArray());
		setHasOptionsMenu(true);
	}

	@Override
	public void onStop() {
		super.onStop();
		for (ListenerWrapper wrapper : wrappers) {
			wrapper.unregister();
		}
	}

	protected void updateDataset(@Nullable ArrayList<ShortDashboard> newDataset) {
		dataset = newDataset;
		if (adapter != null) {
			adapter.setNewDataset(dataset);
			adapter.notifyDataSetChanged();
		}
	}

	protected final void setActionBarTitle(@NonNull final String title) {
		final ActionBar bar = ((AppCompatActivity)getActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(title);
		}
	}

	// TODO image for empty dataset
}
