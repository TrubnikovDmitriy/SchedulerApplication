package ru.mail.park.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class ShortDashboard implements Serializable {

	@SerializedName("title")
	private final String title;
	@SerializedName("dashID")
	private final String dashID;

	public ShortDashboard(String title, String dashID) {
		this.title = title;
		this.dashID = dashID;
	}

	public ShortDashboard(Dashboard dashboard) {
		this.title = dashboard.getTitle();
		this.dashID = dashboard.getDashID();
	}

	public String getTitle() {
		return title;
	}

	public String getDashID() {
		return dashID;
	}
}
