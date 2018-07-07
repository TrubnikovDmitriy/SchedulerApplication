package ru.mail.park.android.database;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import javax.inject.Inject;

import ru.mail.park.android.App;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.Event;


public class RealtimeDatabase {

	private static final String PRIVATE = "private";
	private static final String PUBLIC = "public";
	private static final String DASHBOARDS = "dashboards";
	private static final String EVENTS = "events";
	private static final String INFO = "info";
	private static final String WATCHERS = "watchers";

	public static final String TITLE = "title";


	@NonNull
	public static DatabaseReference getPublicInfoList() {
		return FirebaseDatabase.getInstance().getReference()
				.child(PUBLIC)
				.child(DASHBOARDS)
				.child(INFO);
	}
	@NonNull
	public static DatabaseReference getPublicInfo(@NonNull final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PUBLIC)
				.child(DASHBOARDS)
				.child(INFO)
				.child(dashID);
	}
	@NonNull
	public static DatabaseReference getPublicWatchers(@NonNull final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PUBLIC)
				.child(DASHBOARDS)
				.child(WATCHERS)
				.child(dashID);
	}
	@NonNull
	public static DatabaseReference getPublicEvents(@NonNull final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PUBLIC)
				.child(DASHBOARDS)
				.child(EVENTS)
				.child(dashID);
	}


	@NonNull
	public static DatabaseReference getPrivateInfoList(@NonNull final String userUID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PRIVATE)
				.child(userUID)
				.child(DASHBOARDS)
				.child(INFO);
	}

	@NonNull
	public static DatabaseReference getPrivateInfo(@NonNull final String userUID,
	                                               @NonNull final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PRIVATE)
				.child(userUID)
				.child(DASHBOARDS)
				.child(INFO)
				.child(dashID);

	}
	@NonNull
	public static DatabaseReference getPrivateEvents(@NonNull final String userUID,
	                                                 @NonNull final String dashID) {
		return FirebaseDatabase.getInstance().getReference()
				.child(PRIVATE)
				.child(userUID)
				.child(DASHBOARDS)
				.child(EVENTS)
				.child(dashID);
	}


	@NonNull
	public static String getPathToPrivateEvents(@NonNull final String userUID,
	                                            @NonNull final String dashID) {
		return PRIVATE
				.concat("/").concat(userUID)
				.concat("/").concat(DASHBOARDS)
				.concat("/").concat(EVENTS)
				.concat("/").concat(dashID);
	}
	@NonNull
	public static String getPathToPublicEvents(@NonNull final String dashID) {
		return PUBLIC
				.concat("/").concat(DASHBOARDS)
				.concat("/").concat(EVENTS)
				.concat("/").concat(dashID);
	}

	@NonNull
	public static String createDashboard(@NonNull final DatabaseReference referenceToDashboardsList,
	                                     @NonNull final Map<String, Object> values,
	                                     @NonNull final ValueEventListener listener) {

		final String dashID = referenceToDashboardsList.push().getKey();
		if (dashID == null) {
			throw new DatabaseException("Failed to create new node");
		}

		referenceToDashboardsList.child(dashID).setValue(values);
		referenceToDashboardsList.child(dashID).addListenerForSingleValueEvent(listener);

		return dashID;
	}


	@NonNull
	public static Event parseEvent(@NonNull final DataSnapshot snapshot) {

		final Event event = snapshot.getValue(Event.class);
		if (event == null) {
			throw new NullPointerException("Failed to parse event");
		}

		event.setEventID(snapshot.getKey());

		final DatabaseReference parentNode = snapshot.getRef().getParent();
		if (parentNode == null) {
			throw new NullPointerException("Failed to parse event");
		}
		event.setDashID(parentNode.getKey());

		return event;
	}
	@NonNull
	public static Dashboard parseDashboard(@NonNull final DataSnapshot dataSnapshot) {

		final Dashboard dashboard = dataSnapshot.getValue(Dashboard.class);
		if (dashboard == null) {
			throw new NullPointerException("Failed to parse dashboard");
		}
		dashboard.setDashID(dataSnapshot.getKey());
		return dashboard;
	}



	public static class FailEventListener implements ValueEventListener {

		@Inject
		public Context context;

		public FailEventListener() {
			App.getComponent().inject(this);
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError) {
			Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onDataChange(@NonNull DataSnapshot ignore) { }

	}

	public static abstract class FirebaseEventListener implements ChildEventListener {
		@Override
		public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

		@Override
		public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

		@Override
		public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

		@Override
		public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
	}
}
