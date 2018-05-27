package ru.mail.park.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


@SuppressWarnings("unused")
public class Event implements Serializable {

	@SerializedName("eventID")
	private Long eventID;
	@SerializedName("dashID")
	private Long dashID;
	@SerializedName("text")
	private String text;
	@SerializedName("timestamp")
	private Long timestamp;
	@SerializedName("title")
	private String title;
	@SerializedName("type")
	private EventType type;
	@SerializedName("priority")
	private Priority priority;

	public Event(String text, Long timestamp, Long eventID,
	             Long dashID, String title, EventType type,
	             Priority priority) {

		this.text = text;
		this.timestamp = timestamp;
		this.eventID = eventID;
		this.dashID = dashID;
		this.title = title;
		this.type = type;
		this.priority = priority;
	}


	public Long getEventID() {
		return eventID;
	}

	public Long getDashID() {
		return dashID;
	}

	public String getText() {
		return text;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public String getTitle() {
		return title;
	}

	public EventType getType() {
		return type;
	}

	public Priority getPriority() {
		return priority;
	}

	public enum EventType {
		ONE_TIME,
		EVERY_DAY,
		EVERY_WEEK,
		EVERY_MONTH,
		EVERY_YEAR,
		EVERY_CENTURY
	}

	public enum Priority {
		LOW,
		MEDIUM,
		HIGH,
		ULTRA_HIGH
	}

	public void setEventID(Long eventID) {
		this.eventID = eventID;
	}

	public void setDashID(Long dashID) {
		this.dashID = dashID;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}
}
