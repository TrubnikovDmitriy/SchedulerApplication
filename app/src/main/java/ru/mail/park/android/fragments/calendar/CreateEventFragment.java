package ru.mail.park.android.fragments.calendar;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import ru.mail.park.android.App;
import park.mail.ru.android.R;
import ru.mail.park.android.database.SchedulerDBHelper;
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
import android.view.View;
import android.view.ViewGroup;
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

		final View view = inflater.inflate(R.layout.fragment_creating_event, container, false);

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
					editTitle.setText(event.getTitle());
					editDescription.setText(event.getText());
					date = Tools.getDate(event.getTimestamp());
					// TODO spinners restore
				}
			}
		}

		// Set correct title of action bar
		final ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(isNew ?
					getResources().getString(R.string.event_create_title)
					: getResources().getString(R.string.event_edit_title)
			);
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
			final String title = editTitle.getText().toString();
			final String description = editDescription.getText().toString();
			final Event.EventType eventType = Event.EventType
					.values()[(int) spinnerType.getSelectedItemId()];
			final Event.Priority eventPriority = Event.Priority
					.values()[(int) spinnerPriority.getSelectedItemId()];

			event = new Event(
					description,
					date.getTime() / 1000,
					0L, dashID,
					title,
					eventType,
					eventPriority
			);

			dbHelper.insertEvent(event, new OnEventInsert());
		}
	}

	private class OnEventInsert implements SchedulerDBHelper.OnInsertCompleteListener {

		private final Handler handler = new Handler(Looper.getMainLooper());

		@Override
		public void onSuccess(@NonNull Long rowID) {
			if (event != null) {
				event.setEventID(rowID);
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

}
