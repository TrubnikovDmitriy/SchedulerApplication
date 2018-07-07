package ru.mail.park.android.fragments.calendar;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.park.android.R;
import ru.mail.park.android.dialogs.DialogDateTimePicker;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.utils.Tools;


public abstract class EventFragmentEdit extends Fragment {

	public static final String DATE_BUNDLE = "CURRENT_DATE_BUNDLE";
	public static final String EVENT_BUNDLE = "EVENT_BUNDLE";
	public static final String DASH_ID_BUNDLE = "DASH_ID_BUNDLE";
	public static final String PATH_TO_NODE = "PATH_TO_NODE";

	@BindView(R.id.edit_event_description) TextInputEditText editDescription;
	@BindView(R.id.edit_event_title) TextInputEditText editTitle;
	@BindView(R.id.datetime_picker_button) Button buttonDateTimePicker;
	@BindView(R.id.create_event_button) Button buttonDone;
	@BindView(R.id.event_priority) Spinner spinnerPriority;
	@BindView(R.id.event_type) Spinner spinnerType;
	protected View view;

	protected Event event;
	protected String dashID;
	protected String pathToNode;
	protected Date date;
	protected DialogDateTimePicker dialogPicker;


	@Nullable
	@Override
	@CallSuper
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.fragment_creating_event, container, false);
		ButterKnife.bind(this, view);

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

					dialogPicker.show(requireFragmentManager(), DialogDateTimePicker.DIALOG_TAG);
				}
			}
		});
		buttonDateTimePicker.setText(Tools.formatDate(date));

		return view;
	}

	protected abstract void initialize(@Nullable Bundle savedInstanceState);

	protected abstract void updateDialog();
}
