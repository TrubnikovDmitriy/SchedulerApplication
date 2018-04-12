package android.park.mail.ru.appandroid.pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class ShortDashboard implements Serializable {

	@SerializedName("title")
	private final String title;
	@SerializedName("dashID")
	private final Integer dashID;

	public ShortDashboard(String title, Integer dashID) {
		this.title = title;
		this.dashID = dashID;
	}

	public String getTitle() {
		return title;
	}

	public Integer getDashID() {
		return dashID;
	}
}
