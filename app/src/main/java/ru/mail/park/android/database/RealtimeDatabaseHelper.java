package ru.mail.park.android.database;

import android.support.v4.app.FragmentManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import ru.mail.park.android.R;
import ru.mail.park.android.models.Dashboard;
import ru.mail.park.android.models.Event;


public class RealtimeDatabaseHelper {

	private static final String PRIVATE = "private";
	private static final String PUBLIC = "public";
	private static final String DASHBOARDS = "dashboards";
	private static final String EVENTS = "events";

	public static final String INFO = "info";
	public static final String TITLE = "title";

	private static final FirebaseDatabase database = FirebaseDatabase.getInstance();


	@NonNull
	public DatabaseReference getPrivateDashboards(@NonNull final String userUID) {
		return database.getReference()
				.child(PRIVATE)
				.child(userUID)
				.child(DASHBOARDS);
	}

	@NonNull
	public DatabaseReference getPrivateDashInfo(@NonNull final String userUID,
	                                            @NonNull final String dashID) {
		return database.getReference()
				.child(PRIVATE)
				.child(userUID)
				.child(DASHBOARDS)
				.child(INFO)
				.child(dashID);

	}

	@NonNull
	public DatabaseReference getPrivateDashEvents(@NonNull final String userUID,
	                                              @NonNull final String dashID) {
		return database.getReference()
				.child(PRIVATE)
				.child(userUID)
				.child(DASHBOARDS)
				.child(EVENTS)
				.child(dashID);
	}

	@NonNull
	public Event parseEvent(@NonNull final DataSnapshot snapshot) {
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

	public void getShortDashboards(@NonNull final String userUID,
	                               @NonNull final ValueEventListener listener) {
		this.getPrivateDashboards(userUID)
				.child(INFO)
//				.addListenerForSingleValueEvent(listener);
				.addValueEventListener(listener);
	}

	public void getDashboard(@NonNull final String userUID,
	                         @NonNull final String dashID,
	                         @NonNull final ValueEventListener listener) {
		this.getPrivateDashboards(userUID)
				.child(EVENTS)
				.child(dashID)
				.addListenerForSingleValueEvent(listener);
	}


	public void createDashboard(@NonNull final Dashboard dashboard,
	                            @Nullable final OnFailureListener onFailure) {

		final DatabaseReference reference =
				getPrivateDashboards(dashboard.getAuthorID()).child(INFO);

		final String dashID = reference.push().getKey();
		if (dashID == null) {
			throw new DatabaseException("Failed to create new node");
		}
		dashboard.setDashID(dashID);


		final Task<Void> task = reference.child(dashID).setValue(dashboard.toMap());
		if (onFailure != null) {
			task.addOnFailureListener(onFailure);
		}
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
