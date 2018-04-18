package android.park.mail.ru.appandroid.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.park.mail.ru.appandroid.models.Dashboard;
import android.park.mail.ru.appandroid.models.Event;
import android.park.mail.ru.appandroid.models.ShortDashboard;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

		ID(0, "_id"),
		TITLE(1, "title"),
		AUTHOR_ID(2, "author_id"),
		AUTHOR(3, "author");

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
		PRIORITY(7, "priority");


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
				"type TEXT NOT NULL," +
				"priority TEXT NOT NULL" +
				");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }


	public void selectShortDashboards(
			@NonNull final OnSelectCompleteListener<ArrayList<ShortDashboard>> listener) {

		executor.execute(new Runnable() {
			@Override
			public void run() {
				try (final Cursor cursor = getReadableDatabase().query(
						TABLE_SCHEDULER_NAME,
						new String[] {
								DASH.ID.getName(),
								DASH.TITLE.getName()
						},
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

	public void selectDashboard(
			@NonNull final Long dashID,
			@NonNull final OnSelectCompleteListener<Dashboard> listener) {

		final Dashboard dashboard = new Dashboard();
		dashboard.setDashID(dashID);

		executor.execute(new Runnable() {
			@Override
			public void run() {

				// Select events
				try(final Cursor cursor = getReadableDatabase().query(
						TABLE_EVENTS_NAME,
						new String[] {
								EVENT.PRIMARY_KEY.getName(),
								EVENT.EVENT_ID.getName(),
								EVENT.TITLE.getName(),
								EVENT.TEXT.getName(),
								EVENT.TIMESTAMP.getName(),
								EVENT.PRIORITY.getName(),
								EVENT.TYPE.getName()
						},
						DASH.ID.getName() + " = ?",
						new String[] {String.valueOf(dashID)},
						null, null, null)) {

					final ArrayList<Event> events = new ArrayList<>(cursor.getCount());
					while(cursor.moveToNext()) {
						Event event = new Event(
								cursor.getString(EVENT.TEXT.getNumber()),
								cursor.getLong(EVENT.TIMESTAMP.getNumber()),
								cursor.getLong(EVENT.EVENT_ID.getNumber()),
								dashID,
								cursor.getString(EVENT.TITLE.getNumber()),
								Event.EventType.valueOf(
										cursor.getString(EVENT.TYPE.getNumber())),
								Event.Priority.valueOf(
										cursor.getString(EVENT.PRIORITY.getNumber()))
						);
						events.add(event);
					}
					dashboard.setEvents(events);

				} catch (Exception e) {
					listener.onFailure(e);
				}


				// Select dashboard
				try(final Cursor cursor = getReadableDatabase().query(
						TABLE_SCHEDULER_NAME,
						new String[] {
								DASH.ID.getName(),
								DASH.TITLE.getName(),
								DASH.AUTHOR_ID.getName(),
								DASH.AUTHOR.getName(),
						},
						DASH.ID.getName() + " = ?",
						new String[] {String.valueOf(dashID)},
						null, null, null)) {

					if (cursor.moveToFirst()) {
						dashboard.setAuthor(cursor.getString(DASH.AUTHOR.getNumber()));
						dashboard.setAuthorID(cursor.getLong(DASH.AUTHOR_ID.getNumber()));
						dashboard.setTitle(cursor.getString(DASH.TITLE.getNumber()));
					} else {
						listener.onFailure(new SQLException("This dashboards does not exist"));
					}

					listener.onSuccess(dashboard);

				}  catch (Exception e) {
					listener.onFailure(e);
				}
			}
		});
	}

	public void selectEvents(
			@NonNull final Long dashID,
			@NonNull final OnSelectCompleteListener<ArrayList<Event>> listener) {

		executor.execute(new Runnable() {
			@Override
			public void run() {
				try(final Cursor cursor = getReadableDatabase().query(
						TABLE_EVENTS_NAME,
						new String[] {
								EVENT.PRIMARY_KEY.getName(),
								EVENT.EVENT_ID.getName(),
								EVENT.TITLE.getName(),
								EVENT.TEXT.getName(),
								EVENT.TIMESTAMP.getName(),
								EVENT.PRIORITY.getName(),
								EVENT.TYPE.getName()
						},
						DASH.ID.getName() + " = ?",
						new String[] {String.valueOf(dashID)},
						null, null, null)) {


					final ArrayList<Event> events = new ArrayList<>(cursor.getCount());
					while(cursor.moveToNext()) {
						Event event = new Event(
								cursor.getString(EVENT.TEXT.getNumber()),
								cursor.getLong(EVENT.TIMESTAMP.getNumber()),
								cursor.getLong(EVENT.EVENT_ID.getNumber()),
								dashID,
								cursor.getString(EVENT.TITLE.getNumber()),
								Event.EventType.valueOf(
										cursor.getString(EVENT.TYPE.getNumber())),
								Event.Priority.valueOf(
										cursor.getString(EVENT.PRIORITY.getNumber()))
						);
						events.add(event);
					}
					listener.onSuccess(events);

				} catch (Exception e) {
					listener.onFailure(e);
				}
			}
		});
	}


	public void insertDashboard(
			@NonNull final Dashboard dashboard,
			@NonNull final OnInsertCompleteListener listener) {

		executor.execute(new Runnable() {
			@Override
			public void run() {
				final ContentValues values = new ContentValues();

				values.put(DASH.TITLE.getName(), dashboard.getTitle());
				values.put(DASH.AUTHOR_ID.getName(), dashboard.getAuthorID());
				values.put(DASH.AUTHOR.getName(), dashboard.getAuthor());

				try {
					final Long rowID = getWritableDatabase()
							.insertOrThrow(TABLE_SCHEDULER_NAME, null, values);
					listener.onSuccess(rowID);

				} catch (SQLException e) {
					listener.onFailure(e);
				}
			}
		});
	}

	public void insertEvent(
			@NonNull final Event event,
			@NonNull final OnInsertCompleteListener listener) {

		executor.execute(new Runnable() {
			@Override
			public void run() {
				final ContentValues values = new ContentValues();

				values.put(EVENT.EVENT_ID.getName(), event.getEventID());
				values.put(EVENT.DASH_ID.getName(), event.getDashID());
				values.put(EVENT.TITLE.getName(), event.getTitle());
				values.put(EVENT.TEXT.getName(), event.getText());
				values.put(EVENT.TIMESTAMP.getName(), event.getTimestamp());
				values.put(EVENT.TYPE.getName(), event.getType().toString());
				values.put(EVENT.PRIORITY.getName(), event.getPriority().toString());

				try {
					final Long rowID = getWritableDatabase()
							.insertOrThrow(TABLE_EVENTS_NAME, null, values);
					listener.onSuccess(rowID);

				} catch (SQLException e) {
					listener.onFailure(e);
				}
			}
		});
	}


	public interface OnSelectCompleteListener<T> {

		void onSuccess(@Nullable final T data);

		void onFailure(final Exception exception);
	}

	public interface OnInsertCompleteListener {

		void onSuccess(@NonNull final Long rowID);

		void onFailure(final Exception exception);
	}
}
