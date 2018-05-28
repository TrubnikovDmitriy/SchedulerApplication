package ru.mail.park.android.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import park.mail.ru.android.R;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class DialogDashboardCreator extends DialogFragment {

	public DialogDashboardCreator() { }

	private EditText editText;
	private DialogInterface.OnClickListener onPositiveClick;
	private DialogInterface.OnClickListener onNegativeClick;
	public static final String CREATE_DIALOG_TAG = "CREATE_DIALOG_TAG";


	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		@SuppressLint("InflateParams") final View viewDialog =
				inflater.inflate(R.layout.dialog_creating_dashboard, null);
		editText = viewDialog.findViewById(R.id.edit_creating_dashboard);
		setCancelable(false);

		builder
				.setIcon(R.mipmap.icon_create)
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

	@SuppressWarnings("unused")
	public void setOnNegativeClick(DialogInterface.OnClickListener onNegativeClick) {
		this.onNegativeClick = onNegativeClick;
	}
}
