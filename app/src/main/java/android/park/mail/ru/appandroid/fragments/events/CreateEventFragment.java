package android.park.mail.ru.appandroid.fragments.events;


import android.os.Bundle;
import android.park.mail.ru.appandroid.App;
import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.database.SchedulerDBHelper;
import android.park.mail.ru.appandroid.dialogs.DialogDashboardCreator;
import android.park.mail.ru.appandroid.dialogs.DialogDateTimePicker;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import javax.inject.Inject;

public class CreateEventFragment extends Fragment{

	@Inject
	SchedulerDBHelper dbHelper;

	private Button datePicker;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.getComponent().inject(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_creating_event, container, false);

		datePicker = view.findViewById(R.id.datetime_picker_button);
		datePicker.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DialogDateTimePicker()
						.show(getFragmentManager(), DialogDateTimePicker.CREATE_DIALOG_TAG);
			}
		});

		return view;
	}
}
