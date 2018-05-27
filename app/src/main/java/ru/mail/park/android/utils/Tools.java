package ru.mail.park.android.utils;

import park.mail.ru.android.R;
import ru.mail.park.android.models.Event;

import android.content.res.Resources;
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
	@Deprecated
	public static Date getDate(@NonNull final DateTime dateTime) {
		return new Date(dateTime.getMilliseconds(TIME_ZONE));
	}

	@NonNull
	public static Date getDate(final long timestamp) {
		return new Date(timestamp * 1000);
	}

	@NonNull
	@Deprecated
	public static DateTime getDateTime(@NonNull final Date date) {
		return DateTime.forInstant(date.getTime(), TIME_ZONE);
	}

	@NonNull
	public static DateTime getDateTime(@NonNull final Event event) {
		// TODO remove *1000 for milliseconds
		return DateTime.forInstant(event.getTimestamp() * 1000, TIME_ZONE);
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
