package ru.mail.park.android.network;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.ShortDashboard;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface DashboardService {

	@GET("v1/dashboard")
	Call<List<ShortDashboard>> getDashboards();

	@GET("v1/dashboard/{dash_id}")
	Call<Dashboard> getEvents(@Path("dash_id") String ID);

	@POST("v1/dashboard/{token}")
	Call<Dashboard> postDashboard(@Body Dashboard dashboard, @Path("token") String token);

	@GET("v1/dashboard/{dash_id}/unsubscribe/{token}")
	Call<Dashboard> unsubscribe(@Path("dash_id") String ID, @Path("token") String token);

	@GET("v1/dashboard/{dash_id}/subscribe/{token}")
	Call<Dashboard> subscribe(@Path("dash_id") String ID, @Path("token") String token);

	@DELETE("v1/dashboard/{dash_id}/{token}")
	Call<Dashboard> deleteDashboard(@Path("dash_id") String ID, @Path("token") String token);
}
