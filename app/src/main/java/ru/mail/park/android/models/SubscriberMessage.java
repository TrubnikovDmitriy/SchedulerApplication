package ru.mail.park.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


public class SubscriberMessage {

	@SerializedName("title")
	private String title;
	@SerializedName("dashID")
	private String dashID;
	@SerializedName("inserted")
	private ArrayList<Event> insertedEvents;
	@SerializedName("updated")
	private ArrayList<Event> updatedEvents;
	@SerializedName("deleted")
	private ArrayList<Event> deletedEvents;

	public SubscriberMessage() { }


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDashID() {
		return dashID;
	}

	public void setDashID(String dashID) {
		this.dashID = dashID;
	}

	public ArrayList<Event> getInsertedEvents() {
		return insertedEvents;
	}

	public void setInsertedEvents(ArrayList<Event> insertedEvents) {
		this.insertedEvents = insertedEvents;
	}

	public ArrayList<Event> getUpdatedEvents() {
		return updatedEvents;
	}

	public void setUpdatedEvents(ArrayList<Event> updatedEvents) {
		this.updatedEvents = updatedEvents;
	}

	public ArrayList<Event> getDeletedEvents() {
		return deletedEvents;
	}

	public void setDeletedEvents(ArrayList<Event> deletedEvents) {
		this.deletedEvents = deletedEvents;
	}
}
