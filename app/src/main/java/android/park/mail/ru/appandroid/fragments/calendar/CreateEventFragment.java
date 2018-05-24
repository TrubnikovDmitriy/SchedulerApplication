package android.park.mail.ru.appandroid.fragments.calendar;


import android.content.DialogInterface;
import android.os.Bundle;
import android.park.mail.ru.appandroid.App;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.calendar.SchedulerCaldroidGridAdapter;
import android.park.mail.ru.appandroid.database.SchedulerDBHelper;
import android.park.mail.ru.appandroid.dialogs.DialogDateTimePicker;
import android.park.mail.ru.appandroid.models.Event;
import android.park.mail.ru.appandroid.utils.Tools;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.DragAndDropPermissions;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Date;

import javax.inject.Inject;

import hirondelle.date4j.DateTime;

public class CreateEventFragment extends Fragment {

	public static final String DATE_BUNDLE = "CURRENT_DATE_BUNDLE";
	public static final String IS_NEW_BUNDLE = "IS_NEW_BUNDLE";
	public static final String EVENT_BUNDLE = "EVENT_BUNDLE";

	private Button buttonDateTimePicker;
	private Button buttonDone;

	@Inject
	SchedulerDBHelper dbHelper;
	@Nullable
	private Event event;
	private Boolean isNew;
	private Date date;
	private DialogDateTimePicker dialogPicker;
	private Boolean isDialogLaunched;


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
		initialize(savedInstanceState);

		// Update listener in dialog after rotate
		updateDialog();

		// Initialize button with date
		buttonDateTimePicker = view.findViewById(R.id.datetime_picker_button);
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


		buttonDone = view.findViewById(R.id.create_event_button);


		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(DATE_BUNDLE, date);
		outState.putSerializable(EVENT_BUNDLE, event);
		outState.putBoolean(IS_NEW_BUNDLE, isNew);
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
			if (!isNew) {
				event = (Event) bundle.getSerializable(EVENT_BUNDLE);
			}
		}

		// Set correct title of action bar
		final ActionBar bar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (bar != null) {
			bar.setTitle(isNew ?
					getResources().getString(R.string.event_create_title)
					:
					getResources().getString(R.string.event_edit_title)
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
	}
}
