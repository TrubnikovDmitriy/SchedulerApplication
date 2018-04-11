package android.park.mail.ru.appandroid.pojo;

import java.io.Serializable;

public class ShortDashboard implements Serializable {

	private final String title;
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
