package android.park.mail.ru.appandroid.network;


import android.park.mail.ru.appandroid.pojo.ShortDashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

	public void getDashboards(final OnRequestCompleteListener<List<ShortDashboard>> listener) {

		executor.execute(new Runnable() {
			@Override
			public void run() {
				Call<List<ShortDashboard>> call = service.getDashboards();
				try {
					Response<List<ShortDashboard>> response = call.execute();
					listener.onSuccess(response, response.body());

				} catch (IOException | RuntimeException exception) {
					listener.onFailure(exception);
				}
			}
		});
	}

	public interface OnRequestCompleteListener<T> {

		void onSuccess(Response<T> response, T body);

		void onFailure(Exception exception);
	}
}
