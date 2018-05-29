package ru.mail.park.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;


public class Dashboard implements Serializable {

	@SerializedName("title")
	private String title;
	@SerializedName("dashID")
	private String dashID;
	@SerializedName("author")
	private String author;
	@SerializedName("authorID")
	private Long authorID;
	@SerializedName("events")
	private ArrayList<Event> events;


	public Dashboard() { }

	public Dashboard(String author, Long authorID, String title,
	                 String dashID, ArrayList<Event> events) {
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

	public String getDashID() {
		return dashID;
	}

	public ArrayList<Event> getEvents() {
		return events;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDashID(String dashID) {
		this.dashID = dashID;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setAuthorID(Long authorID) {
		this.authorID = authorID;
	}

	public void setEvents(ArrayList<Event> events) {
		this.events = events;
	}
}
