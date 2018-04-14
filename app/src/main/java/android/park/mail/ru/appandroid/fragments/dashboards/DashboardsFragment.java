package android.park.mail.ru.appandroid.fragments.dashboards;

import android.os.Bundle;
import android.park.mail.ru.appandroid.models.ShortDashboard;
import android.park.mail.ru.appandroid.recycler.DashboardAdapter;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;

import java.util.ArrayList;


public abstract class DashboardsFragment extends Fragment {

	public static final String DATASET = "dataset_bundle";

	// TODO ButterKnife @Bind
	protected DashboardAdapter adapter;
	protected ProgressBar progressBar;
	protected ArrayList<ShortDashboard> dataset;


	public DashboardsFragment() { }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(DATASET, dataset == null ? null : dataset.toArray());
		super.onSaveInstanceState(outState);
	}

	protected void updateDataset(@Nullable ArrayList<ShortDashboard> newDataset) {
		dataset = newDataset;
		if (adapter != null) {
			adapter.setNewDataset(dataset);
			adapter.notifyDataSetChanged();
		}
	}

	// TODO image for empty dataset
}
