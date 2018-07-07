package ru.mail.park.android.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import ru.mail.park.android.R;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class DialogDashboardCreator extends DialogFragment {

	public static final String DIALOG_TAG = "DialogDashboardCreator";

	private DialogInterface.OnClickListener onPositiveClick;
	private EditText editText;


	public DialogDashboardCreator() { }

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
		final LayoutInflater inflater = requireActivity().getLayoutInflater();

		@SuppressLint("InflateParams")
		final View viewDialog = inflater.inflate(R.layout.dialog_edit_name_dashboard, null);
		editText = viewDialog.findViewById(R.id.edit_name_dashboard);
		setCancelable(false);

		builder
				.setIcon(R.mipmap.ic_create_black)
				.setTitle(R.string.title_of_dashboard)
				.setView(viewDialog)
				.setPositiveButton(R.string.create_button, onPositiveClick)
				.setNegativeButton(R.string.cancel_button, null);

		return builder.create();
	}

	public String getInputText() {
		return editText.getText().toString();
	}

	public void setOnPositiveClick(DialogInterface.OnClickListener onPositiveClick) {
		this.onPositiveClick = onPositiveClick;
	}
}
