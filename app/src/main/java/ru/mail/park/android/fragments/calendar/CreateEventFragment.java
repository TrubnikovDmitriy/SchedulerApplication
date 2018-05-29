package ru.mail.park.android.fragments.calendar;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import ru.mail.park.android.App;
import ru.mail.park.android.R;
import ru.mail.park.android.database.SchedulerDBHelper;
import ru.mail.park.android.dialogs.DialogConfirm;
import ru.mail.park.android.dialogs.DialogDateTimePicker;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.utils.Tools;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Date;

import javax.inject.Inject;


public class CreateEventFragment extends Fragment {

	public static final String DATE_BUNDLE = "CURRENT_DATE_BUNDLE";
	public static final String IS_NEW_BUNDLE = "IS_NEW_BUNDLE";
	public static final String EVENT_BUNDLE = "EVENT_BUNDLE";
	public static final String DASH_ID_BUNDLE = "DASH_ID_BUNDLE";

	private TextInputEditText editDescription;
	private TextInputEditText editTitle;
	private Button buttonDateTimePicker;
	private Button buttonDone;
	private Spinner spinnerPriority;
	private Spinner spinnerType;
	private View view;

	@Inject
	SchedulerDBHelper dbHelper;
	@Nullable
	private Event event;
	private Long dashID;
	private Boolean isNew;
	private Date date;
	private DialogDateTimePicker dialogPicker;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.getComponent().inject(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_creating_event, container, false);

		// Bind views
		editDescription = view.findViewById(R.id.edit_event_description);
		editTitle = view.findViewById(R.id.edit_event_title);
		buttonDateTimePicker = view.findViewById(R.id.datetime_picker_button);
		buttonDone = view.findViewById(R.id.create_event_button);
		spinnerPriority = view.findViewById(R.id.event_priority);
		spinnerType = view.findViewById(R.id.event_type);

		// Initialize fields of form
		initialize(savedInstanceState);

		// Update listener in dialog after rotate
		updateDialog();

		// Listener for date/time picker
		buttonDateTimePicker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!dialogPicker.isAdded()) {
					final Bundle bundle = new Bundle();
					bundle.putSerializable(DialogDateTimePicker.OLD_DATE_BUNDLE, date);
					dialogPicker.setArguments(bundle);

					dialogPicker.show(getFragmentManager(), DialogDateTimePicker.CREATE_DIALOG_TAG);
				}
			}
		});
		buttonDateTimePicker.setText(Tools.formatDate(date));

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(DATE_BUNDLE, date);
		outState.putSerializable(EVENT_BUNDLE, event);
		outState.putBoolean(IS_NEW_BUNDLE, isNew);
		outState.putLong(DASH_ID_BUNDLE, dashID);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.create_event, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_delete_event) {
			if (event != null && event.getEventID() != null) {

				final DialogConfirm dialogConfirm = new DialogConfirm();
				dialogConfirm.setTitle(getResources().getString(R.string.delete_event_confirm));
				dialogConfirm.setListener(new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dbHelper.deleteEvent(event.getEventID(), new OnEventChange());
					}
				});
				dialogConfirm.show(getFragmentManager(), null);
				return true;
			}
		}
		return false;
	}


	private void initialize(@Nullable Bundle savedInstanceState) {

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
			isNew = bundle.getBoolean(IS_NEW_BUNDLE);
			dashID = bundle.getLong(DASH_ID_BUNDLE);
			if (!isNew) {
				// If clicked at already existing event_high
				event = (Event) bundle.getSerializable(EVENT_BUNDLE);
				buttonDone.setText(R.string.event_edit_button);
				if (event != null) {
					// Restore data in fields from event
					editTitle.setText(event.getTitle());
					editDescription.setText(event.getText());
					date = Tools.getDate(event.getTimestamp());
					spinnerPriority.setSelection(event.getPriority().ordinal());
					spinnerType.setSelection(event.getType().ordinal());
				}
			}
			setHasOptionsMenu(!isNew);
		}

		// Set correct title of action bar
		final ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(isNew ? getResources().getString(R.string.event_create_title) : getResources().getString(R.string.event_edit_title));
		}
	}

	private void updateDialog() {

		dialogPicker = (DialogDateTimePicker) getFragmentManager()
				.findFragmentByTag(DialogDateTimePicker.CREATE_DIALOG_TAG);

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
			// Validationgit
			if (title.length() < Tools.TITLE_MIN_LENGTH) {
				Toast.makeText(getContext(), R.string.too_short_title, Toast.LENGTH_SHORT).show();
				return;
			}
			final String description = editDescription.getText().toString().trim();
			final Event.EventType eventType = Event.EventType
					.values()[(int) spinnerType.getSelectedItemId()];
			final Event.Priority eventPriority = Event.Priority
					.values()[(int) spinnerPriority.getSelectedItemId()];

			final Long eventID = (event == null) ? 0 : event.getEventID();

			event = new Event(
					description,
					date.getTime() / 1000,
					eventID,
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

			if (isNew) {
				dbHelper.insertEvent(event, new OnEventInsert());
			} else {
				dbHelper.updateEvent(event, new OnEventChange());
			}
		}
	}

	private class OnEventInsert implements SchedulerDBHelper.OnInsertCompleteListener {

		private final Handler handler = new Handler(Looper.getMainLooper());

		@Override
		public void onSuccess(@NonNull Long rowID) {
			if (event != null) {
				event.setEventID(rowID);
				// Return to calendar
				getFragmentManager().popBackStack();
			}
		}

		@Override
		public void onFailure(Exception exception) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getContext(), R.string.db_failure, Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	private class OnEventChange implements
			SchedulerDBHelper.OnUpdateCompleteListener,
			SchedulerDBHelper.OnDeleteCompleteListener {

		private final Handler handler = new Handler(Looper.getMainLooper());

		@Override
		public void onSuccess(int numberOfRowsAffected) {
			if (numberOfRowsAffected != 1) {
				onFailure(null);
			}
			getFragmentManager().popBackStack();
		}

		@Override
		public void onFailure(@Nullable Exception exception) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getContext(), R.string.db_failure, Toast.LENGTH_LONG).show();
				}
			});
		}
	}
}
