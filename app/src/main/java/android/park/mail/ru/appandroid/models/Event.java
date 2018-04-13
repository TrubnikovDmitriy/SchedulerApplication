package android.park.mail.ru.appandroid.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class Event implements Serializable {

	@SerializedName("eventID")
	private final Long eventID;
	@SerializedName("dashID")
	private final Long dashID;
	@SerializedName("text")
	private final String text;
	@SerializedName("timestamp")
	private final Long timestamp;
	@SerializedName("title")
	private final String title;
	@SerializedName("wholeDay")
	private final Boolean isWholeDay;
	@SerializedName("type")
	private final EventType type;
	@SerializedName("priority")
	private final Priority priority;


	public Event(String text, Long timestamp, Long eventID,
	             Long dashID, String title, Boolean isWholeDay,
	             EventType type, Priority priority) {

		this.text = text;
		this.timestamp = timestamp;
		this.eventID = eventID;
		this.dashID = dashID;
		this.title = title;
		this.isWholeDay = isWholeDay;
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

	public Boolean getWholeDay() {
		return isWholeDay;
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
		EVERY_YEAR,
		EVERY_CENTURY
	}

	public enum Priority {
		LOW,
		MEDIUM,
		HIGH,
		ULTRA_HIGH
	}

	@Override
	public String toString() {
		return "Event{" +
				"eventID=" + eventID +
				", dashID=" + dashID +
				", text='" + text + '\'' +
				", timestamp=" + timestamp +
				", title='" + title + '\'' +
				", isWholeDay=" + isWholeDay +
				", type=" + type +
				", priority=" + priority +
				'}';
	}
}
