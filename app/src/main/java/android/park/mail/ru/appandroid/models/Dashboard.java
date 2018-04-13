package android.park.mail.ru.appandroid.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;


public class Dashboard implements Serializable {

	@SerializedName("title")
	private final String title;
	@SerializedName("dashID")
	private final Long dashID;
	@SerializedName("author")
	private final String author;
	@SerializedName("authorID")
	private final Long authorID;
	@SerializedName("events")
	private final ArrayList<Event> events;

	public Dashboard(String author, Long authorID, String title, Long dashID, ArrayList<Event> events) {
		this.author = author;
		this.authorID = authorID;
		this.title = title;
		this.dashID = dashID;
		this.events = events;
	}


	public String getAuthor() {
		return author;
	}

	public Long getAuthorID() {
		return authorID;
	}

	public String getTitle() {
		return title;
	}

	public Long getDashID() {
		return dashID;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}


	@Override
	public String toString() {
		return "Dashboard{" +
				"title='" + title + '\'' +
				", dashID=" + dashID +
				", author='" + author + '\'' +
				", authorID=" + authorID +
				", events=" + events +
				'}';
	}
}
