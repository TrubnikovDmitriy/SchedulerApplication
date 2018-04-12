package android.park.mail.ru.appandroid.network;

import android.park.mail.ru.appandroid.pojo.ShortDashboard;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;


public interface DashboardService {

	@GET("v1/dashboard")
	Call<List<ShortDashboard>> getDashboards();

}
