package ru.mail.park.android.fragments.calendar;

import android.content.Context;
import android.os.Bundle;

import ru.mail.park.android.R;
import ru.mail.park.android.database.RealtimeDatabase;
import ru.mail.park.android.dialogs.DialogDateTimePicker;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.utils.Tools;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;


public final class EventFragmentEditCreate extends EventFragmentEdit {

	public EventFragmentEditCreate() { }

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(DATE_BUNDLE, date);
		outState.putString(DASH_ID_BUNDLE, dashID);
		outState.putString(PATH_TO_NODE, pathToNode);
	}

	@Override
	protected void initialize(@Nullable Bundle savedInstanceState) {

		// Take arguments from bundle after transaction OR after rotate
		Bundle bundle = null;
		if (savedInstanceState != null) {
			bundle = savedInstanceState;
		}
		if (getArguments() != null) {
			bundle = getArguments();
			setArguments(null);
		}
		if (bundle != null) {
			date = (Date) bundle.getSerializable(DATE_BUNDLE);
			dashID = bundle.getString(DASH_ID_BUNDLE);
			pathToNode = bundle.getString(PATH_TO_NODE);
		}

		// Set correct title of action bar
		final ActionBar bar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(getResources().getString(R.string.event_create_title));
		}
	}

	@Override
	protected void updateDialog() {

		dialogPicker = (DialogDateTimePicker)
				requireFragmentManager().findFragmentByTag(DialogDateTimePicker.DIALOG_TAG);

		if (dialogPicker == null) {
			dialogPicker = new DialogDateTimePicker();
		}
		dialogPicker.setOnApplyListener(new DialogDateTimePicker.OnApplyListener() {
			@Override
			public void onApply(@NonNull Date newDate) {
				date = newDate;
				buttonDateTimePicker.setText(Tools.formatDate(date));
			}
		});

		// Listener for create event_high in database
		buttonDone.setOnClickListener(new OnDoneClickListener());
	}

	private class OnDoneClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			final String title = editTitle.getText().toString().trim();
			// Validation
			if (title.length() < Tools.TITLE_MIN_LENGTH) {
				Toast.makeText(requireContext(), R.string.too_short_title, Toast.LENGTH_SHORT).show();
				return;
			}

			final String description = editDescription.getText().toString().trim();
			final Event.EventType eventType = Event.EventType
					.values()[(int) spinnerType.getSelectedItemId()];
			final Event.Priority eventPriority = Event.Priority
					.values()[(int) spinnerPriority.getSelectedItemId()];

			event = new Event(
					description,
					date.getTime(),
					null,
					dashID,
					title,
					eventType,
					eventPriority
			);

			// Change current month in calendar
			SchedulerCaldroidFragment.currentDateTime = Tools.getDate(date);

			// Close keyboard
			final InputMethodManager imm = (InputMethodManager)
					view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}

			// Check auth
			final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
			if (user == null) {
				Toast.makeText(requireContext(), R.string.auth_access_db, Toast.LENGTH_SHORT).show();
				return;
			}

			// Create event in Firebase
			final DatabaseReference events = FirebaseDatabase.getInstance().getReference(pathToNode);
			final String newEventID = events.push().getKey();
			if (newEventID == null) {
				throw new NullPointerException("Failed to create new event");
			}
			events.addChildEventListener(new InsertEvent(events, newEventID));
			events.child(newEventID).setValue(event.toMap());
		}
	}

	private class InsertEvent extends RealtimeDatabase.FirebaseEventListener {

		final DatabaseReference reference;
		final String eventID;

		InsertEvent(@NonNull final DatabaseReference reference,
		            @NonNull final String eventID) {
			this.reference = reference;
			this.eventID = eventID;
		}

		@Override
		public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			if (eventID.equals(dataSnapshot.getKey())) {
				reference.removeEventListener(this);
				requireFragmentManager().popBackStack();
			}
		}

		@Override
		public void onCancelled(@NonNull DatabaseError databaseError) {
			final Context context = getContext();
			if (context != null) {
				Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
			}
			reference.removeEventListener(this);
		}
	}
}
