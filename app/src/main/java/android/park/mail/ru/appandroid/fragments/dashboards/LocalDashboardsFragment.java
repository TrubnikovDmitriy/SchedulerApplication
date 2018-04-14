package android.park.mail.ru.appandroid.fragments.dashboards;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.database.SchedulerDBHelper;
import android.park.mail.ru.appandroid.models.ShortDashboard;
import android.park.mail.ru.appandroid.recycler.DashboardAdapter;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class LocalDashboardsFragment extends DashboardsFragment {

	private SchedulerDBHelper dbHelper;

	public LocalDashboardsFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_dashboards,
				container, false);
		dbHelper = new SchedulerDBHelper(getContext());

		RecyclerView recyclerView = view.findViewById(R.id.recycle_dash);
		progressBar = view.findViewById(R.id.progressbar_dash_load);
		progressBar.setVisibility(ProgressBar.VISIBLE);

		if (savedInstanceState == null) {
			// Receiving data from DB
			dbHelper.getDashboards(new DatabaseLoadDashboardsListener());

		} else {
			Object[] objects = (Object[]) savedInstanceState.getSerializable(DATASET);

			if (objects != null) {
				// Try to cast Object[] to ShortDashboard[]
				ShortDashboard[] dashes = Arrays.copyOf(
						objects, objects.length, ShortDashboard[].class);
				dataset = new ArrayList<>(Arrays.asList(dashes));
			}
			progressBar.setVisibility(ProgressBar.INVISIBLE);
		}

		adapter = new DashboardAdapter(dataset, null);
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		return view;
	}


	class DatabaseLoadDashboardsListener implements SchedulerDBHelper.OnQueryCompleteListener<ShortDashboard> {

		private final Handler handler = new Handler(Looper.getMainLooper());

		@Override
		public void onSuccess(final ArrayList<ShortDashboard> data) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(
							getContext(),
							data.isEmpty() ? R.string.empty_dataset : R.string.success_load_dashboards,
							Toast.LENGTH_SHORT
					).show();
					progressBar.setVisibility(ProgressBar.INVISIBLE);
					updateDataset(data);
				}
			});
		}

		@Override
		public void onFailure(Exception exception) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getContext(), R.string.db_failure, Toast.LENGTH_LONG).show();
					progressBar.setVisibility(ProgressBar.INVISIBLE);
					updateDataset(null);
				}
			});
		}
	}

}
