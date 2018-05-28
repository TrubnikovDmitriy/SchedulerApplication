package ru.mail.park.android.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import park.mail.ru.android.R;
import ru.mail.park.android.utils.Tools;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Date;

import hirondelle.date4j.DateTime;


public class DialogDateTimePicker extends DialogFragment {

	public static final String CREATE_DIALOG_TAG = "DialogDateTimePicker";

	public static final String CURRENT_DATE_BUNDLE = "CURRENT_DATE";
	public static final String OLD_DATE_BUNDLE = "OLD_DATE";

	private OnApplyListener onApplyListener;
	private Date oldDate;

	private TimePicker timePicker;
	private DatePicker datePicker;


	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams") final View viewDialog =
				inflater.inflate(R.layout.dialog_datetime_picker, null);

		datePicker = viewDialog.findViewById(R.id.date_picker);
		timePicker = viewDialog.findViewById(R.id.time_picker);
		timePicker.setIs24HourView(true);
		initialize(savedInstanceState);

		builder
				.setTitle(R.string.pick_date_time)
				.setView(viewDialog)
				.setPositiveButton(R.string.apply_button, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						onApplyListener.onApply(Tools.getDate(getCurrentDate()));
					}
				})
				.setNegativeButton(R.string.cancel_button, null);

		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(CURRENT_DATE_BUNDLE, getCurrentDate());
		outState.putSerializable(OLD_DATE_BUNDLE, oldDate);
	}

	private DateTime getCurrentDate() {
		return new DateTime(
				datePicker.getYear(),
				datePicker.getMonth() + 1, // [0;11] => [1;12]
				datePicker.getDayOfMonth(),
				timePicker.getCurrentHour(),
				timePicker.getCurrentMinute(),
				0,0
		);
	}

	private void initialize(@Nullable final Bundle savedInstanceState) {

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
			oldDate = (Date) bundle.getSerializable(OLD_DATE_BUNDLE);
			DateTime currentDate = (DateTime) bundle.getSerializable(CURRENT_DATE_BUNDLE);
			if (currentDate == null) {
				currentDate = Tools.getDate(oldDate);
			}

			// Update Date and Time at the user screen
			datePicker.init(
					currentDate.getYear(),
					currentDate.getMonth() - 1,
					currentDate.getDay(),
					null
			);
			timePicker.setCurrentHour(currentDate.getHour());
			timePicker.setCurrentMinute(currentDate.getMinute());
		}
	}

	public void setOnApplyListener(OnApplyListener onApplyListener) {
		this.onApplyListener = onApplyListener;
	}

	public interface OnApplyListener {
		void onApply(@NonNull final Date date);
	}
}
