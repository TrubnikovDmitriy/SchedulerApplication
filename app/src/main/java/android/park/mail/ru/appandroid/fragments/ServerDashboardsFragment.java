package android.park.mail.ru.appandroid.fragments;

import android.os.Bundle;
import android.park.mail.ru.appandroid.network.HttpClient;
import android.park.mail.ru.appandroid.network.NetworkManager;
import android.park.mail.ru.appandroid.pojo.ShortDashboard;
import android.park.mail.ru.appandroid.recycler.DashboardAdapter;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.park.mail.ru.appandroid.R;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import okhttp3.Response;
import okhttp3.ResponseBody;


public class ServerDashboardsFragment extends Fragment {

	public static final String DATASET = "dataset";


	private DashboardAdapter adapter;
	private ProgressBar progressBar;
	private ArrayList<ShortDashboard> dataset;

	public ServerDashboardsFragment() { }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		final View view = inflater.inflate(R.layout.fragment_server_dashboards,
				container, false);
		RecyclerView recyclerView = view.findViewById(R.id.recycle_dash);
		progressBar = view.findViewById(R.id.progressbar_dash_load);
		progressBar.setVisibility(ProgressBar.VISIBLE);


		if (savedInstanceState == null) {
			// Receiving data from server
			NetworkManager.getSchedulers(new LoadTitlesListener());

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


		adapter = new DashboardAdapter(dataset);
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(DATASET, dataset.toArray());
		super.onSaveInstanceState(outState);
	}

	private void updateDataset(@Nullable ArrayList<ShortDashboard> newDataset) {
		dataset = newDataset;
		adapter.setNewDataset(dataset);
		adapter.notifyDataSetChanged();
	}

	class LoadTitlesListener implements HttpClient.OnRequestCompleteListener {

		private ArrayList<ShortDashboard> dashes = new ArrayList<>();

		@Override
		public void onSuccess(Response response) {

			try(ResponseBody body = response.body()) {

				// Parse HTTP response
				if (body != null && response.code() == 200) {
					JSONObject bodyJSON = new JSONObject(body.string());
					JSONArray dashboards = bodyJSON.getJSONArray("dashboards");

					for (int i = 0; i < dashboards.length(); ++i) {
						dashes.add(new ShortDashboard(
								dashboards.getJSONObject(i).getString("title"),
								dashboards.getJSONObject(i).getInt("dashID")
						));
					}
				}

			} catch (IOException | JSONException e) {

				Log.e("Parse", "Exception while parsing", e);
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getContext(), R.string.parse_err,
								Toast.LENGTH_LONG).show();
						progressBar.setVisibility(ProgressBar.INVISIBLE);
						updateDataset(dashes);
					}
				});
				return;
			}

			// Update data set in adapter
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(
							getContext(),
							dashes.isEmpty() ?
									R.string.empty_dataset : R.string.success_load_dataset,
							Toast.LENGTH_LONG
					).show();
					progressBar.setVisibility(ProgressBar.INVISIBLE);
					updateDataset(dashes);
				}
			});
		}

		@Override
		public void onFailure() {
			Log.e("Network", "Error connection");
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getContext(), R.string.network_err,
							Toast.LENGTH_LONG).show();
					progressBar.setVisibility(ProgressBar.INVISIBLE);
					updateDataset(dashes);
				}
			});
		}
	}
}
