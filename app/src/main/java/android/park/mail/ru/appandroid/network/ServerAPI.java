package android.park.mail.ru.appandroid.network;


import android.park.mail.ru.appandroid.models.Dashboard;
import android.park.mail.ru.appandroid.models.ShortDashboard;
import android.park.mail.ru.appandroid.utils.ListenerWrapper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerAPI {

	private static final ServerAPI instance = new ServerAPI();
	private final DashboardService service;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();


	private ServerAPI() {
		service = new Retrofit.Builder()
				.baseUrl("http://138.68.173.73:5050/api/")
				.addConverterFactory(GsonConverterFactory.create())
				.build()
				.create(DashboardService.class);
	}

	public static ServerAPI getInstance() {
		return instance;
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
			final long ID, @NonNull OnRequestCompleteListener<Dashboard> listener) {

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


	public interface OnRequestCompleteListener<T> {

		void onSuccess(Response<T> response, T body);

		void onFailure(Exception exception);
	}
}
