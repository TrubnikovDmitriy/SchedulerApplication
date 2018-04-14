package android.park.mail.ru.appandroid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.park.mail.ru.appandroid.models.Event;
import android.park.mail.ru.appandroid.models.ShortDashboard;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SchedulerDBHelper extends SQLiteOpenHelper {

	// TODO Singleton

	private static final String DB_NAME = "scheduler.db";
	private static final String TABLE_SCHEDULER_NAME = "schedulers";
	private static final String TABLE_EVENTS_NAME = "events";
	private static final int VERSION = 1;

	public enum DASH {

		PRIMARY_KEY(0, "_id"),
		ID(1, "dash_id"),
		TITLE(2, "title"),
		AUTHOR_ID(3, "author_id"),
		AUTHOR(4, "author");


		DASH(int column_number, String column_name) {
			this.column_number = column_number;
			this.column_name = column_name;
		}

		private final int column_number;
		private final String column_name;

		public int getNumber() {
			return column_number;
		}

		public String getName() {
			return column_name;
		}
	}
	public enum EVENT {

		PRIMARY_KEY(0, "_id"),
		EVENT_ID(1, "event_id"),
		DASH_ID(2, "dash_id"),
		TITLE(3, "title"),
		TEXT(4, "text"),
		TIMESTAMP(5, "timestamp"),
		TYPE(6, "type"),
		PRIORITY(7, "priority"),
		WHOLE_DAY(8, "is_whole_day");


		EVENT(int column_number, String column_name) {
			this.column_number = column_number;
			this.column_name = column_name;
		}

		private final int column_number;
		private final String column_name;

		public int getNumber() {
			return column_number;
		}

		public String getName() {
			return column_name;
		}
	}

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public SchedulerDBHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_SCHEDULER_NAME + "(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"dash_id INTEGER NOT NULL," +
				"title TEXT NOT NULL," +
				"author_id INTEGER NOT NULL," +
				"author TEXT NOT NULL" +
				");");
		db.execSQL("CREATE TABLE " + TABLE_EVENTS_NAME + "(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"event_id INTEGER NOT NULL," +
				"dash_id INTEGER NOT NULL," +
				"title TEXT NOT NULL," +
				"text BLOB," +
				"timestamp NUMERIC NOT NULL," +
				"type NUMERIC NOT NULL CHECK (type IN (0,1,2,3,4))," +
				"priority NUMERIC NOT NULL CHECK (priority IN (0,1,2,3))," +
				"is_whole_day INTEGER CHECK (is_whole_day IN (0,1))" +
				");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

	public void getDashboards(final OnQueryCompleteListener<ShortDashboard> listener) {

		executor.execute(new Runnable() {
			@Override
			public void run() {
				try (final Cursor cursor = getReadableDatabase().query(
						TABLE_SCHEDULER_NAME,
						new String[]{ DASH.PRIMARY_KEY.getName(), DASH.ID.getName(), DASH.TITLE.getName() },
						null, null, null, null, null)) {

					final ArrayList<ShortDashboard> dashboards = new ArrayList<>(cursor.getCount());
					while(cursor.moveToNext()) {
						ShortDashboard dashboard = new ShortDashboard(
								cursor.getString(DASH.TITLE.getNumber()),
								cursor.getLong(DASH.ID.getNumber())
						);
						dashboards.add(dashboard);
					}

					listener.onSuccess(dashboards);

				} catch (Exception e) {
					listener.onFailure(e);
				}
			}
		});
	}

	public void insertDashboard(final ShortDashboard dashboard) {

		executor.execute(new Runnable() {
			@Override
			public void run() {
				final ContentValues values = new ContentValues();

				values.put(DASH.ID.getName(), dashboard.getDashID());
				values.put(DASH.TITLE.getName(), dashboard.getTitle());
				// TODO delete stubs
				values.put(DASH.AUTHOR_ID.getName(), 1L);
				values.put(DASH.AUTHOR.getName(), "Dmitriy");

				getWritableDatabase().insertOrThrow(TABLE_SCHEDULER_NAME, null, values);
			}
		});

	}

	public void insertEvent(final Event event) {

		executor.execute(new Runnable() {
			@Override
			public void run() {
				final ContentValues values = new ContentValues();

				values.put(EVENT.EVENT_ID.getName(), event.getEventID());
				values.put(EVENT.DASH_ID.getName(), event.getDashID());
				values.put(EVENT.TITLE.getName(), event.getTitle());
				values.put(EVENT.TEXT.getName(), event.getText());
				values.put(EVENT.TIMESTAMP.getName(), event.getTimestamp());
				values.put(EVENT.TYPE.getName(), event.getType().ordinal());
				values.put(EVENT.PRIORITY.getName(), event.getPriority().ordinal());
				values.put(EVENT.WHOLE_DAY.getName(), event.getWholeDay());

				getWritableDatabase().insertOrThrow(TABLE_EVENTS_NAME, null, values);
			}
		});
	}

	public interface OnQueryCompleteListener<T> {

		void onSuccess(final ArrayList<T> data);

		void onFailure(final Exception exception);
	}
}
