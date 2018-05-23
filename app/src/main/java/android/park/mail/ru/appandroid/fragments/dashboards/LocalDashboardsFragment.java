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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
	private DialogDashboardCreator dialogDashboardCreator;

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
		progressBar.setVisibility(View.VISIBLE);

		floatingButton = view.findViewById(R.id.fab);
		floatingButton.setVisibility(View.VISIBLE);
		floatingButton.setOnClickListener(new onFloatingButtonClickListener());

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


	private void setDialogListeners() {
		// If user rotates the screen the click-listeners will be lost
		// We find an existing DialogFragment by tag to restore listeners after rotation
		dialogDashboardCreator = (DialogDashboardCreator) getFragmentManager()
				.findFragmentByTag(DialogDashboardCreator.CREATE_DIALOG_TAG);
		// otherwise - create new one
		if (dialogDashboardCreator == null) {
			dialogDashboardCreator = new DialogDashboardCreator();
		}

		dialogDashboardCreator.setOnPositiveClick(new DialogInterface.OnClickListener() {

			private static final int TITLE_MIN_LENGTH = 3;

			@Override
			public void onClick(DialogInterface dialog, int which) {

				// Validation of title
				final String newTitle = dialogDashboardCreator.getInputText().trim();
				if (newTitle.length() < TITLE_MIN_LENGTH) {
					Toast.makeText(getContext(), R.string.too_short_title, Toast.LENGTH_SHORT).show();
					return;
				}

				// TODO delete stubs
				// Added to database
				final Dashboard dashboard = new Dashboard("Dmitriy", 1L,
						newTitle, null, null);
				final ListenerWrapper wrapper = dbManager.insertDashboard(
						dashboard, new SchedulerDBHelper.OnInsertCompleteListener() {

							final Handler handler = new Handler(Looper.getMainLooper());

							@Override
							public void onSuccess(@NonNull Long rowID) {
								dashboard.setDashID(rowID);
								handler.post(new Runnable() {
									@Override
									public void run() {
										adapter.addItem(new ShortDashboard(dashboard));
										recyclerView.scrollToPosition(dataset.size() - 1);
									}
								});
							}

							@Override
							public void onFailure(Exception exception) {
								handler.post(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(getContext(), R.string.db_failure, Toast.LENGTH_LONG).show();
									}
								});
							}
						});
				wrappers.add(wrapper);
			}
		});
	}


	class onFloatingButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			dialogDashboardCreator.show(getFragmentManager(), DialogDashboardCreator.CREATE_DIALOG_TAG);
		}
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
