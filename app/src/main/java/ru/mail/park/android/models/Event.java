package ru.mail.park.android.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@SuppressWarnings("unused")
public class Event implements Serializable {

	@SerializedName("eventID")
	private String eventID;
	@SerializedName("dashID")
	private String dashID;
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


	public Event() { }

	public Event(String text, Long timestamp, String eventID,
	             String dashID, String title, EventType type,
	             Priority priority) {

		this.text = text;
		this.timestamp = timestamp;
		this.eventID = eventID;
		this.dashID = dashID;
		this.title = title;
		this.type = type;
		this.priority = priority;
	}


	public String getEventID() {
		return eventID;
	}

	public String getDashID() {
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

	public void setEventID(String eventID) {
		this.eventID = eventID;
	}

	public void setDashID(String dashID) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Event event = (Event) o;
		return Objects.equals(eventID, event.eventID) &&
				Objects.equals(dashID, event.dashID) &&
				Objects.equals(text, event.text) &&
				Objects.equals(timestamp, event.timestamp) &&
				Objects.equals(title, event.title) &&
				type == event.type &&
				priority == event.priority;
	}

	public Map<String, Object> toMap() {
		final HashMap<String, Object> firebaseModel = new HashMap<>();
		firebaseModel.put("text", text);
		firebaseModel.put("title", title);
		firebaseModel.put("timestamp", timestamp);
		firebaseModel.put("type", type);
		firebaseModel.put("priority", priority);
		return firebaseModel;
	}
}
