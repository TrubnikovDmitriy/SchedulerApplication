package android.park.mail.ru.appandroid.fragments.dashboards;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.database.SchedulerDBHelper;
import android.park.mail.ru.appandroid.dialogs.DashboardCreate;
import android.park.mail.ru.appandroid.models.Dashboard;
import android.park.mail.ru.appandroid.models.ShortDashboard;
import android.park.mail.ru.appandroid.recycler.DashboardAdapter;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class LocalDashboardsFragment extends DashboardsFragment {

	private SchedulerDBHelper dbHelper;
	private final DashboardCreate dashboardCreate = new DashboardCreate();

	public LocalDashboardsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper = new SchedulerDBHelper(getContext());

		dashboardCreate.setOnPositiveClick(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				final String newTitle = dashboardCreate.getInputText().trim();
				if (newTitle.isEmpty()) {
					Toast.makeText(getContext(), R.string.empty_title, Toast.LENGTH_SHORT).show();
					return;
				}

				// TODO delete stubs and try exceptions
				final Dashboard dashboard = new Dashboard("Dmitriy", 1L,
						newTitle, null, null);
				dbHelper.insertDashboard(dashboard);
				dataset.add(new ShortDashboard(dashboard));
				adapter.notifyItemInserted(dataset.size() - 1);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_dashboards,
				container, false);

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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.local_dashboards, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.create_dashboard:
				dashboardCreate.show(getFragmentManager(), null);
				return true;

			default:
				return false;
		}
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
