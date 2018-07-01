package ru.mail.park.android.utils;

import ru.mail.park.android.R;
import ru.mail.park.android.models.Event;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;


public class Tools {

	public static final int TITLE_MIN_LENGTH = 3;
	public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT+3");

	private static final SimpleDateFormat DATE_FORMATTER =
			new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.UK);
	private static final SimpleDateFormat TIME_FORMATTER =
			new SimpleDateFormat("HH:mm", Locale.UK);

	public static String formatDate(final Date date) {
		return DATE_FORMATTER.format(date);
	}

	public static String formatTime(final Date date) {
		return TIME_FORMATTER.format(date);
	}

	@NonNull
	public static Date getDate(@NonNull final DateTime dateTime) {
		return new Date(dateTime.getMilliseconds(TIME_ZONE));
	}

	@NonNull
	public static Date getDate(final long timestamp) {
		return new Date(timestamp);
	}

	@NonNull
	public static DateTime getDate(@NonNull final Date date) {
		return DateTime.forInstant(date.getTime(), TIME_ZONE);
	}

	@NonNull
	public static DateTime getDateTime(@NonNull final Event event) {
		return DateTime.forInstant(event.getTimestamp(), TIME_ZONE);
	}

	public static float getCalendarCellWidthInPixels(@NonNull Resources resources) {
		float cellSize = resources.getDisplayMetrics().widthPixels;
		// Subtract the padding at the edges of calendar
		cellSize -= 2 * resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin);
		// Seven days in week on one row
		cellSize /= 7;

		return cellSize;
	}
}
