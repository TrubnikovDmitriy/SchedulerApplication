package ru.mail.park.android.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import park.mail.ru.android.R;



public class DialogConfirm extends DialogFragment {

	private String title;
	private Dialog.OnClickListener listener;

	public DialogConfirm() { }

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setIcon(R.mipmap.ic_alert_black)
				.setTitle(title)
				.setPositiveButton(R.string.delete_button, listener)
				.setNegativeButton(R.string.cancel_button, null)
				.create();
	}

	public void setTitle(@Nullable String title) {
		this.title = title;
	}

	public void setListener(@Nullable Dialog.OnClickListener listener) {
		this.listener = listener;
	}

	@Override
	public void onPause() {
		super.onPause();
		// Close dialog after rotate
		dismiss();
	}
}
