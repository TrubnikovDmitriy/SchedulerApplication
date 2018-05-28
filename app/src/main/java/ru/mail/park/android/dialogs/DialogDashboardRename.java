package ru.mail.park.android.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import park.mail.ru.android.R;


public class DialogDashboardRename extends DialogFragment {

	public static final String DIALOG_TAG = "DialogDashboardRename";

	private OnRenameListener listener;
	private EditText editText;


	public DialogDashboardRename() { }

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final LayoutInflater inflater = getActivity().getLayoutInflater();

		@SuppressLint("InflateParams")
		final View viewDialog = inflater.inflate(R.layout.dialog_edit_name_dashboard, null);
		editText = viewDialog.findViewById(R.id.edit_name_dashboard);

		builder
				.setIcon(R.mipmap.ic_edit_black)
				.setTitle(R.string.rename_dashboard)
				.setView(viewDialog)
				.setPositiveButton(R.string.create_button, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final String newName = editText.getText().toString();
						if (listener != null) {
							listener.onRename(newName);
						}
					}
				})
				.setNegativeButton(R.string.cancel_button, null);

		return builder.create();
	}

	public void setOnRenameListener(OnRenameListener listener) {
		this.listener = listener;
	}

	public interface OnRenameListener {
		void onRename(@NonNull final String newName);
	}
}