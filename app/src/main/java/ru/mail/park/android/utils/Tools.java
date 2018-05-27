package ru.mail.park.android.utils;

import ru.mail.park.android.models.Event;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class Tools {

	public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT+3");
	private static final SimpleDateFormat DATE_FORMATTER =
			new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.UK);

	public static String formatDate(final Date date) {
		return DATE_FORMATTER.format(date);
	}

	@NonNull
	public static Date getDate(@NonNull final DateTime dateTime) {
		return new Date(dateTime.getMilliseconds(TIME_ZONE));
	}

	@NonNull
	public static Date getDate(final long timestamp) {
		return new Date(timestamp * 1000);
	}

	@NonNull
	public static DateTime getDate(@NonNull final Date date) {
		return DateTime.forInstant(date.getTime(), TIME_ZONE);
	}

	@NonNull
	public static DateTime getDate(@NonNull final Event event) {
		// TODO remove *1000 for milliseconds
		return DateTime.forInstant(event.getTimestamp() * 1000, TIME_ZONE);
	}
}
