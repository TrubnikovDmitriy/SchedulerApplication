package ru.mail.park.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Objects;


public class ShortDashboard implements Serializable {

	@SerializedName("title")
	private String title;
	@SerializedName("dashID")
	private String dashID;

	public ShortDashboard() { }

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

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDashID(String dashID) {
		this.dashID = dashID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ShortDashboard that = (ShortDashboard) o;
		return Objects.equals(title, that.title) &&
				Objects.equals(dashID, that.dashID);
	}
}
