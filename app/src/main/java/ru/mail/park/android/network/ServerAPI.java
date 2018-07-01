package ru.mail.park.android.network;

import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.ShortDashboard;
import ru.mail.park.android.utils.ListenerWrapper;
import android.support.annotation.NonNull;

import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ServerAPI {

	private final DashboardService service;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();


	public ServerAPI() {
		service = new Retrofit.Builder()
				.baseUrl("http://138.68.173.73:5050/api/")
				.addConverterFactory(GsonConverterFactory.create())
				.build()
				.create(DashboardService.class);
	}


	public ListenerWrapper<OnRequestCompleteListener<List<ShortDashboard>>> getDashboards(
			@NonNull OnRequestCompleteListener<List<ShortDashboard>> listener) {

		final ListenerWrapper<OnRequestCompleteListener<List<ShortDashboard>>> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Call<List<ShortDashboard>> call = service.getDashboards();
				try {
					Response<List<ShortDashboard>> response = call.execute();
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(response, response.body());
					}

				} catch (IOException | RuntimeException exception) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(exception);
					}
				}
			}
		});
		return wrapper;
	}


	public ListenerWrapper<OnRequestCompleteListener<Dashboard>> getEvents(
			final String ID, @NonNull OnRequestCompleteListener<Dashboard> listener) {

		final ListenerWrapper<OnRequestCompleteListener<Dashboard>> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Call<Dashboard> call = service.getEvents(ID);
				try {
					Response<Dashboard> response = call.execute();
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(response, response.body());
					}

				} catch (IOException | RuntimeException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}


	public ListenerWrapper<OnRequestCompleteListener<Dashboard>> postDashboard(
			@NonNull final Dashboard dashboard,
			@NonNull OnRequestCompleteListener<Dashboard> listener) {

		final ListenerWrapper<OnRequestCompleteListener<Dashboard>> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Call<Dashboard> call = service.postDashboard(dashboard, FirebaseInstanceId.getInstance().getToken());
				try {
					Response<Dashboard> response = call.execute();
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(response, response.body());
					}

				} catch (IOException | RuntimeException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}



	public ListenerWrapper<OnRequestCompleteListener<Dashboard>> subscribe(
			@NonNull final String dashID,
			@NonNull OnRequestCompleteListener<Dashboard> listener) {

		final ListenerWrapper<OnRequestCompleteListener<Dashboard>> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Call<Dashboard> call = service.subscribe(dashID, FirebaseInstanceId.getInstance().getToken());
				try {
					Response<Dashboard> response = call.execute();
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(response, response.body());
					}

				} catch (IOException | RuntimeException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}

	public ListenerWrapper<OnRequestCompleteListener<Dashboard>> unsubscribe(
			@NonNull final String dashID,
			@NonNull OnRequestCompleteListener<Dashboard> listener) {

		final ListenerWrapper<OnRequestCompleteListener<Dashboard>> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Call<Dashboard> call = service.unsubscribe(dashID, FirebaseInstanceId.getInstance().getToken());
				try {
					Response<Dashboard> response = call.execute();
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(response, response.body());
					}

				} catch (IOException | RuntimeException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}




	public interface OnRequestCompleteListener<T> {

		void onSuccess(Response<T> response, T body);

		void onFailure(Exception exception);
	}
}
