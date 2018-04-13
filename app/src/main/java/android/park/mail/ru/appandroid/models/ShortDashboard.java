package android.park.mail.ru.appandroid.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class ShortDashboard implements Serializable {

	@SerializedName("title")
	private final String title;
	@SerializedName("dashID")
	private final Long dashID;

	public ShortDashboard(String title, Long dashID) {
		this.title = title;
		this.dashID = dashID;
	}

	public String getTitle() {
		return title;
	}

	public Long getDashID() {
		return dashID;
	}
}
