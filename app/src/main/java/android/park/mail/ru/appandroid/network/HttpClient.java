package android.park.mail.ru.appandroid.network;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpClient {

	private final static HttpClient instance = new HttpClient();

	private final OkHttpClient client = new OkHttpClient();
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private HttpClient() { }

	static HttpClient getInstance() {
		return instance;
	}

	void executeGet(final Request request,
	                       final OnRequestCompleteListener listener) {

		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					final Response response = client.newCall(request).execute();
					listener.onSuccess(response);
				} catch (IOException e) {
					Log.e(getClass().getSimpleName(), "Error while execute GET request", e);
					listener.onFailure();
				}
			}
		});
	}

	public interface OnRequestCompleteListener {

		void onSuccess(Response response);

		void onFailure();
	}

}
