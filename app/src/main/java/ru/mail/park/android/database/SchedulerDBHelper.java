package ru.mail.park.android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.models.ShortDashboard;
import ru.mail.park.android.utils.ListenerWrapper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SchedulerDBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "scheduler.db";
	private static final String TABLE_DASHBOARDS_NAME = "schedulers";
	private static final String TABLE_EVENTS_NAME = "events";
	private static final int VERSION = 1;

	public enum DASH {

		PRIMARY_KEY(0, "_id"),
		DASH_ID(1, "dash_id"),
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
		DASH_ID(1, "dash_id"),
		EVENT_ID(2, "event_id"),
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
		db.execSQL(
				"CREATE TABLE " + TABLE_DASHBOARDS_NAME + "(" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"dash_id TEXT NOT NULL UNIQUE," +
						"title TEXT NOT NULL," +
						"author_id INTEGER NOT NULL," +
						"author TEXT NOT NULL" +
				");"
		);
		db.execSQL(
				"CREATE TABLE " + TABLE_EVENTS_NAME + "(" +
						"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
						"dash_id TEXT NOT NULL," +
						"event_id TEXT NOT NULL," +
						"title TEXT NOT NULL," +
						"text BLOB," +
						"timestamp NUMERIC NOT NULL," +
						"type TEXT NOT NULL," +
						"priority TEXT NOT NULL" +
				");"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }


	public ListenerWrapper<OnSelectCompleteListener<ArrayList<ShortDashboard>>> selectShortDashboards(
			@NonNull OnSelectCompleteListener<ArrayList<ShortDashboard>> listener) {

		final ListenerWrapper<OnSelectCompleteListener<ArrayList<ShortDashboard>>> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try (final Cursor cursor = getReadableDatabase().query(
						TABLE_DASHBOARDS_NAME,
						new String[] {
								DASH.PRIMARY_KEY.getName(),
								DASH.DASH_ID.getName(),
								DASH.TITLE.getName()
						},
						null, null, null, null, null)) {

					final ArrayList<ShortDashboard> dashboards = new ArrayList<>(cursor.getCount());
					while(cursor.moveToNext()) {
						ShortDashboard dashboard = new ShortDashboard(
								cursor.getString(cursor.getColumnIndex(DASH.TITLE.getName())),
								cursor.getString(cursor.getColumnIndex(DASH.DASH_ID.getName()))
						);
						dashboards.add(dashboard);
					}
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(dashboards);
					}

				} catch (SQLException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}

	public ListenerWrapper<OnSelectCompleteListener<Dashboard>> selectDashboard(
			@NonNull final String dashID,
			@NonNull OnSelectCompleteListener<Dashboard> listener) {

		final ListenerWrapper<OnSelectCompleteListener<Dashboard>> wrapper = new ListenerWrapper<>(listener);
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
						EVENT.DASH_ID.getName() + " = ?",
						new String[] { dashID },
						null, null, null)) {

					final ArrayList<Event> events = new ArrayList<>(cursor.getCount());
					while(cursor.moveToNext()) {
						Event event = new Event(
								cursor.getString(cursor.getColumnIndex(EVENT.TEXT.getName())),
								cursor.getLong(cursor.getColumnIndex(EVENT.TIMESTAMP.getName())),
								cursor.getString(cursor.getColumnIndex(EVENT.EVENT_ID.getName())),
								dashID,
								cursor.getString(cursor.getColumnIndex(EVENT.TITLE.getName())),
								Event.EventType.valueOf(
										cursor.getString(cursor.getColumnIndex(EVENT.TYPE.getName()))),
								Event.Priority.valueOf(
										cursor.getString(cursor.getColumnIndex(EVENT.PRIORITY.getName())))
						);
						events.add(event);
					}
					dashboard.setEvents(events);

				} catch (SQLException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}


				// Select dashboard
				try(final Cursor cursor = getReadableDatabase().query(
						TABLE_DASHBOARDS_NAME,
						new String[] {
								DASH.PRIMARY_KEY.getName(),
								DASH.DASH_ID.getName(),
								DASH.TITLE.getName(),
								DASH.AUTHOR_ID.getName(),
								DASH.AUTHOR.getName(),
						},
						DASH.DASH_ID.getName() + " = ?",
						new String[] { dashID },
						null, null, null)) {

					if (cursor.moveToFirst()) {
						final String author = cursor.getString(cursor.getColumnIndex(DASH.AUTHOR.getName()));
						final Long authorID = cursor.getLong(cursor.getColumnIndex(DASH.AUTHOR_ID.getName()));
						final String title = cursor.getString(cursor.getColumnIndex(DASH.TITLE.getName()));

						dashboard.setAuthor(author);
						dashboard.setAuthorID(authorID);
						dashboard.setTitle(title);

					} else {
						if (wrapper.getListener() != null) {
							wrapper.getListener().onFailure(new SQLException("This dashboards does not exist"));
						}
					}
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(dashboard);
					}

				}  catch (SQLException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}

	public ListenerWrapper<OnInsertCompleteListener> insertDashboard(
			@NonNull final Dashboard dashboard,
			@NonNull OnInsertCompleteListener listener) {

		final ListenerWrapper<OnInsertCompleteListener> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				final ContentValues values = new ContentValues();

				values.put(DASH.DASH_ID.getName(), dashboard.getDashID());
				values.put(DASH.TITLE.getName(), dashboard.getTitle());
				values.put(DASH.AUTHOR_ID.getName(), dashboard.getAuthorID());
				values.put(DASH.AUTHOR.getName(), dashboard.getAuthor());

				try {
					final Long rowID = getWritableDatabase()
							.insertOrThrow(TABLE_DASHBOARDS_NAME, null, values);
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(rowID);
					}

				} catch (SQLException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}

	public ListenerWrapper<OnInsertCompleteListener> insertEvent(
			@NonNull final Event event,
			@NonNull OnInsertCompleteListener listener) {

		final ListenerWrapper<OnInsertCompleteListener> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				final ContentValues values = getContentValueFromEvent(event);
				try {
					final Long rowID = getWritableDatabase().insertOrThrow(
							TABLE_EVENTS_NAME, null, values);
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(rowID);
					}

				} catch (SQLException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}

	public ListenerWrapper<OnUpdateCompleteListener> updateEvent(
			@NonNull final Event event,
			@NonNull OnUpdateCompleteListener listener) {

		final ListenerWrapper<OnUpdateCompleteListener> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {

				final ContentValues values = new ContentValues();
				values.put(EVENT.TITLE.getName(), event.getTitle());
				values.put(EVENT.TEXT.getName(), event.getText());
				values.put(EVENT.TIMESTAMP.getName(), event.getTimestamp());
				values.put(EVENT.TYPE.getName(), event.getType().toString());
				values.put(EVENT.PRIORITY.getName(), event.getPriority().toString());

				final String WHERE = EVENT.EVENT_ID.getName() + " = ?";
				try {
					final int rowsAffected = getWritableDatabase().update(
							TABLE_EVENTS_NAME,
							values,
							WHERE,
							new String[] { event.getEventID() }
					);
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(rowsAffected);
					}

				} catch (SQLException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}

	public ListenerWrapper<OnUpdateCompleteListener> renameDashboard(
			@NonNull final Dashboard dashboard,
			@NonNull OnUpdateCompleteListener listener) {

		final ListenerWrapper<OnUpdateCompleteListener> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {

				final ContentValues values = new ContentValues();
				values.put(DASH.TITLE.getName(), dashboard.getTitle());
				final String WHERE = DASH.DASH_ID.getName() + " = ?";

				try {
					final int rowsAffected = getWritableDatabase().update(
							TABLE_DASHBOARDS_NAME,
							values,
							WHERE,
							new String[] { dashboard.getDashID() }
					);
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(rowsAffected);
					}

				} catch (SQLException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}

	public ListenerWrapper<OnDeleteCompleteListener> deleteDashboard(
			@NonNull final String dashID,
			@NonNull OnDeleteCompleteListener listener) {

		final ListenerWrapper<OnDeleteCompleteListener> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				final String WHERE_EVENT = EVENT.DASH_ID.getName() + " = ?";
				final String WHERE_DASH = DASH.DASH_ID.getName() + " = ?";
				try {
					getWritableDatabase().delete(
							TABLE_EVENTS_NAME,
							WHERE_EVENT,
							new String[] { dashID }
					);
					final int rowsAffected = getWritableDatabase().delete(
							TABLE_DASHBOARDS_NAME,
							WHERE_DASH,
							new String[] { dashID }
					);
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(rowsAffected);
					}
				} catch (SQLException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}

	public ListenerWrapper<OnDeleteCompleteListener> deleteEvent(
			@NonNull final String eventID,
			@Nullable OnDeleteCompleteListener listener) {

		final ListenerWrapper<OnDeleteCompleteListener> wrapper = new ListenerWrapper<>(listener);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				final String WHERE = EVENT.EVENT_ID.getName() + " = ?";
				try {
					final int rowsAffected = getWritableDatabase().delete(
							TABLE_EVENTS_NAME,
							WHERE,
							new String[] { eventID }
					);
					if (wrapper.getListener() != null) {
						wrapper.getListener().onSuccess(rowsAffected);
					}
				} catch (SQLException e) {
					if (wrapper.getListener() != null) {
						wrapper.getListener().onFailure(e);
					}
				}
			}
		});
		return wrapper;
	}


	@NonNull
	private ContentValues getContentValueFromEvent(@NonNull final Event event) {
		final ContentValues values = new ContentValues();

		values.put(EVENT.DASH_ID.getName(), event.getDashID());
		values.put(EVENT.EVENT_ID.getName(), event.getEventID());
		values.put(EVENT.TITLE.getName(), event.getTitle());
		values.put(EVENT.TEXT.getName(), event.getText());
		values.put(EVENT.TIMESTAMP.getName(), event.getTimestamp());
		values.put(EVENT.TYPE.getName(), event.getType().toString());
		values.put(EVENT.PRIORITY.getName(), event.getPriority().toString());

		return values;
	}

	public interface OnSelectCompleteListener<T> {

		void onSuccess(@Nullable final T data);

		void onFailure(final Exception exception);
	}

	public interface OnInsertCompleteListener {

		void onSuccess(@NonNull final Long rowID);

		void onFailure(final Exception exception);
	}

	public interface OnUpdateCompleteListener {

		void onSuccess(final int numberOfRowsAffected);

		void onFailure(final Exception exception);
	}

	public interface OnDeleteCompleteListener {

		void onSuccess(final int numberOfRowsAffected);

		void onFailure(final Exception exception);
	}
}
