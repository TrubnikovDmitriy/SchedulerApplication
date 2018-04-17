package android.park.mail.ru.appandroid.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.database.SchedulerDBHelper;
import android.park.mail.ru.appandroid.models.Dashboard;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class DashboardCreate extends DialogFragment {

	public DashboardCreate() { }

	private EditText editText;
	private DialogInterface.OnClickListener onPositiveClick;
	private DialogInterface.OnClickListener onNegativeClick;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		final View viewDialog = inflater.inflate(R.layout.creating_dashboard, null);
		editText = viewDialog.findViewById(R.id.edit_creating_dashboard);

		builder
				.setTitle(R.string.title_of_dashboard)
				.setView(viewDialog)
				.setPositiveButton(R.string.create_button, onPositiveClick)
				.setNegativeButton(R.string.cancel_button, onNegativeClick);

		return builder.create();
	}

	public String getInputText() {
		return editText.getText().toString();
	}

	public void setOnPositiveClick(DialogInterface.OnClickListener onPositiveClick) {
		this.onPositiveClick = onPositiveClick;
	}

	public void setOnNegativeClick(DialogInterface.OnClickListener onNegativeClick) {
		this.onNegativeClick = onNegativeClick;
	}
}
