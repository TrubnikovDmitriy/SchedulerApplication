package android.park.mail.ru.appandroid.fragments.dashboards;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.park.mail.ru.appandroid.App;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.database.SchedulerDBHelper;
import android.park.mail.ru.appandroid.dialogs.DialogDashboardCreator;
import android.park.mail.ru.appandroid.fragments.events.LocalEventsFragment;
import android.park.mail.ru.appandroid.models.Dashboard;
import android.park.mail.ru.appandroid.models.ShortDashboard;
import android.park.mail.ru.appandroid.recycler.DashboardAdapter;
import android.park.mail.ru.appandroid.utils.ListenerWrapper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;


public class LocalDashboardsFragment extends DashboardsFragment {

	@Inject
	public SchedulerDBHelper dbManager;
	private DialogDashboardCreator dialogDashboardCreator = new DialogDashboardCreator();

	public LocalDashboardsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		App.getComponent().inject(this);
		super.onCreate(savedInstanceState);
		setDialogListeners();
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_dashboards,
				container, false);
		setActionBarTitle(getResources().getString(R.string.local_dashes_title));
		recyclerView = view.findViewById(R.id.recycle_dash);

		progressBar = view.findViewById(R.id.progressbar_dash_load);
		progressBar.setVisibility(ProgressBar.VISIBLE);

		if (savedInstanceState == null) {
			// Receiving data from DB
			final ListenerWrapper wrapper =
					dbManager.selectShortDashboards(new DatabaseLoadDashboardsListener());
			wrappers.add(wrapper);

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

		adapter = new DashboardAdapter(dataset, new OnDashboardClickListener());
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.local_dashboards, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.create_dashboard:
				dialogDashboardCreator.show(getFragmentManager(), null);
				return true;

			default:
				return false;
		}
	}

	private void setDialogListeners() {
		// TODO обработать кейс с поворотом экрана и уже открытого диалога
		dialogDashboardCreator.setOnPositiveClick(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				final String newTitle = dialogDashboardCreator.getInputText().trim();
				if (newTitle.isEmpty()) {
					Toast.makeText(getContext(), R.string.empty_title, Toast.LENGTH_SHORT).show();
					return;
				}

				// TODO delete stubs
				final Dashboard dashboard = new Dashboard("Dmitriy", 1L,
						newTitle, null, null);
				final ListenerWrapper wrapper =
						dbManager.insertDashboard(dashboard, new SchedulerDBHelper.OnInsertCompleteListener() {
					@Override
					public void onSuccess(@NonNull Long rowID) {
						dashboard.setDashID(rowID);
						dataset.add(new ShortDashboard(dashboard));
					}

					@Override
					public void onFailure(Exception exception) {
						Toast.makeText(getContext(), R.string.db_failure, Toast.LENGTH_LONG).show();
					}
				});
				wrappers.add(wrapper);

			}
		});
	}

	class OnDashboardClickListener implements DashboardAdapter.OnDashboardClickListener {

		@Override
		public void onClick(@NonNull final ShortDashboard dashboard) {

			// Create fragment and set arguments
			final Fragment fragment = new LocalEventsFragment();
			final Bundle bundle = new Bundle();
			bundle.putLong(LocalEventsFragment.DASHBOARD_ID, dashboard.getDashID());
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}
	}

	class DatabaseLoadDashboardsListener implements
			SchedulerDBHelper.OnSelectCompleteListener<ArrayList<ShortDashboard>> {

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
