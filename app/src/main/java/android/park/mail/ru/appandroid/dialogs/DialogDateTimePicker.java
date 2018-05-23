package android.park.mail.ru.appandroid.dialogs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.park.mail.ru.appandroid.R;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;


public class DialogDateTimePicker extends DialogFragment {

	public static final String CREATE_DIALOG_TAG = "DATE_TIME_PICKER_DIALOG";


	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		final LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams") final View viewDialog =
				inflater.inflate(R.layout.dialog_datetime_picker, null);

		final TimePicker timePicker = viewDialog.findViewById(R.id.time_picker);
		timePicker.setIs24HourView(true);

		builder
				.setTitle(R.string.pick_date_time)
				.setView(viewDialog)
				.setPositiveButton(R.string.create_button, null)
				.setNegativeButton(R.string.cancel_button, null);

		return builder.create();
	}
}
