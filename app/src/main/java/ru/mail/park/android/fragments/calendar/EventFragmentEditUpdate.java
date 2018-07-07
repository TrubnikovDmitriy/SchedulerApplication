package ru.mail.park.android.fragments.calendar;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import ru.mail.park.android.R;
import ru.mail.park.android.database.RealtimeDatabase;
import ru.mail.park.android.dialogs.DialogConfirm;
import ru.mail.park.android.dialogs.DialogDateTimePicker;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.utils.Tools;


public final class EventFragmentEditUpdate extends EventFragmentEdit {

	public EventFragmentEditUpdate() { }

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		view = super.onCreateView(inflater, container, savedInstanceState);
		buttonDone.setText(R.string.event_edit_button);
		return view;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putSerializable(DATE_BUNDLE, date);
		outState.putSerializable(EVENT_BUNDLE, event);
		outState.putString(DASH_ID_BUNDLE, dashID);
		outState.putString(PATH_TO_NODE, pathToNode);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.create_event, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_delete_event) {

			final DialogConfirm dialogConfirm = new DialogConfirm();
			dialogConfirm.setTitle(getResources().getString(R.string.delete_event_confirm));
			dialogConfirm.setListener(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
					if (user == null) {
						Toast.makeText(requireContext(), R.string.auth_access_db, Toast.LENGTH_SHORT).show();
						return;
					}
					final DatabaseReference events = RealtimeDatabase.getPrivateEvents(user.getUid(), dashID);
					events.addChildEventListener(new ChangeEvents(events, event.getEventID()));
					events.child(event.getEventID()).removeValue();
				}
			});
			dialogConfirm.show(requireFragmentManager(), null);
			return true;
		}
		return false;
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

			// Restore data in fields from Event
			event = (Event) bundle.getSerializable(EVENT_BUNDLE);
			if (event != null) {
				editTitle.setText(event.getTitle());
				editDescription.setText(event.getText());
				date = Tools.getDate(event.getTimestamp());
				spinnerPriority.setSelection(event.getPriority().ordinal());
				spinnerType.setSelection(event.getType().ordinal());
			}
			setHasOptionsMenu(true);
		}

		// Set correct title of action bar
		final ActionBar bar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(getResources().getString(R.string.event_edit_title));
		}

	}

	@Override
	protected  void updateDialog() {
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
		// Listener for update event in firebase
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
			final String eventID = event.getEventID();

			final Event newEvent = new Event(
					description,
					date.getTime(),
					eventID,
					dashID,
					title,
					eventType,
					eventPriority
			);

			// Change current month in calendar
			SchedulerCaldroidFragment.currentDateTime = Tools.getDate(date);

			// Hide keyboard
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

			// If there are no changes, just go back
			if (newEvent.equals(event)) {
				requireFragmentManager().popBackStack();
				return;
			}
			event = newEvent;

			// Update event in Firebase
			final DatabaseReference events = FirebaseDatabase.getInstance().getReference(pathToNode);
			events.addChildEventListener(new ChangeEvents(events, event.getEventID()));
			events.child(event.getEventID()).setValue(event.toMap());
		}
	}

	private class ChangeEvents extends RealtimeDatabase.FirebaseEventListener {

		final DatabaseReference reference;
		final String eventID;

		ChangeEvents(@NonNull final DatabaseReference reference,
		             @NonNull final String eventID) {
			this.reference = reference;
			this.eventID = eventID;
		}

		@Override
		public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
			if (eventID.equals(dataSnapshot.getKey())) {
				reference.removeEventListener(this);
				requireFragmentManager().popBackStack();
			}
		}

		@Override
		public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
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
