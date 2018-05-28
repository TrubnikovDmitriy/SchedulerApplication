package ru.mail.park.android.network;

import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.ShortDashboard;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface DashboardService {

	@GET("v1/dashboard")
	Call<List<ShortDashboard>> getDashboards();

	@GET("v1/dashboard/{DASH_ID}")
	Call<Dashboard> getEvents(@Path("DASH_ID") Long ID);

}
