package android.park.mail.ru.appandroid.network;

import okhttp3.Request;


public class NetworkManager {

	private static final String URL = "http://138.68.173.73:5050";
	private final static HttpClient httpClient = HttpClient.getInstance();

	public static void getSchedulers(HttpClient.OnRequestCompleteListener listener) {
		final Request request = new Request.Builder()
				.get()
				.url(URL + "/api/v1/dashboard")
				.build();
		httpClient.executeGet(request, listener);
	}
}
