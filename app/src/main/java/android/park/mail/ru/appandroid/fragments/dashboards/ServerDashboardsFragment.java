package android.park.mail.ru.appandroid.fragments.dashboards;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.park.mail.ru.appandroid.fragments.events.ServerEventsFragment;
import android.park.mail.ru.appandroid.network.ServerAPI;
import android.park.mail.ru.appandroid.models.ShortDashboard;
import android.park.mail.ru.appandroid.recycler.DashboardAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import android.park.mail.ru.appandroid.R;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Response;


public class ServerDashboardsFragment extends DashboardsFragment {

	public ServerDashboardsFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.fragment_dashboards,
				container, false);
		RecyclerView recyclerView = view.findViewById(R.id.recycle_dash);
		setActionBarTitle(getResources().getString(R.string.cloud_dashes_title));
		progressBar = view.findViewById(R.id.progressbar_dash_load);
		progressBar.setVisibility(ProgressBar.VISIBLE);


		if (savedInstanceState == null) {
			// Receiving data from server
			ServerAPI.getInstance().getDashboards(new NetworkLoadDashboardsListener());

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
		inflater.inflate(R.menu.server_dashboards, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	class OnDashboardClickListener implements DashboardAdapter.OnDashboardClickListener {

		@Override
		public void onClick(@NonNull final ShortDashboard dashboard) {
			// Create fragment and set arguments
			final Fragment fragment = new ServerEventsFragment();
			final Bundle bundle = new Bundle();
			bundle.putLong(ServerEventsFragment.DASHBOARD_ID, dashboard.getDashID());
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
		}
	}

	class NetworkLoadDashboardsListener implements ServerAPI.OnRequestCompleteListener<List<ShortDashboard>> {

		private ArrayList<ShortDashboard> dashboards;
		private final Handler handler = new Handler(Looper.getMainLooper());

		@Override
		public void onSuccess(Response<List<ShortDashboard>> response, List<ShortDashboard> list) {
			if (response.code() == 200) {
				dashboards = new ArrayList<>(list);
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(
								getContext(),
								dashboards.isEmpty() ? R.string.empty_dataset : R.string.success_load_dashboards,
								Toast.LENGTH_SHORT
						).show();
						progressBar.setVisibility(ProgressBar.INVISIBLE);
						updateDataset(dashboards);
					}
				});
			} else {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getContext(), R.string.network_failure,
								Toast.LENGTH_LONG).show();
						progressBar.setVisibility(ProgressBar.INVISIBLE);
						updateDataset(null);
					}
				});
			}
		}

		@Override
		public void onFailure(Exception exception) {

			// IOException - network problem
			if (exception instanceof IOException) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getContext(), R.string.network_err, Toast.LENGTH_LONG).show();
						progressBar.setVisibility(ProgressBar.INVISIBLE);
						updateDataset(null);
					}
				});
				return;
			}

			// RuntimeException - parse problem
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getContext(), R.string.parse_err, Toast.LENGTH_LONG).show();
					progressBar.setVisibility(ProgressBar.INVISIBLE);
					updateDataset(null);
				}
			});
		}
	}
}
