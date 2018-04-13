package android.park.mail.ru.appandroid.network;

import android.park.mail.ru.appandroid.models.Dashboard;
import android.park.mail.ru.appandroid.models.ShortDashboard;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface DashboardService {

	@GET("v1/dashboard")
	Call<List<ShortDashboard>> getDashboards();

	@GET("v1/dashboard/{ID}")
	Call<Dashboard> getEvents(@Path("ID") Long ID);

}
