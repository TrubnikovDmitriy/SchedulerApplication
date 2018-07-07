package ru.mail.park.android.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Dashboard implements Serializable {

	@SerializedName("title")
	private String title;
	@SerializedName("dashID")
	private String dashID;
	@SerializedName("author")
	private String author;
	@SerializedName("authorUID")
	private String authorUID;
	@SerializedName("events")
	private ArrayList<Event> events;


	public Dashboard() { }

	public Dashboard(String author, String authorUID, String title,
	                 String dashID, ArrayList<Event> events) {
		this.author = author;
		this.authorUID = authorUID;
		this.title = title;
		this.dashID = dashID;
		this.events = events;
	}


	public String getAuthor() {
		return author;
	}

	public String getAuthorUID() {
		return authorUID;
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

	public void setAuthorUID(String authorUID) {
		this.authorUID = authorUID;
	}

	public void setEvents(ArrayList<Event> events) {
		this.events = events;
	}


	@Exclude
	public Map<String, Object> toPrivateMap() {
		final HashMap<String, Object> firebaseModel = new HashMap<>();
		firebaseModel.put("title", title);
		return firebaseModel;
	}

	@Exclude
	public  Map<String, Object> toPublicMap() {
		final HashMap<String, Object> firebaseModel = new HashMap<>();
		firebaseModel.put("title", title);
		firebaseModel.put("author", author);
		firebaseModel.put("authorUID", authorUID);
		return firebaseModel;
	}
}
