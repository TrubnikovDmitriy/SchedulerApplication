package ru.mail.park.android.fragments.calendar;

import android.os.Bundle;
import park.mail.ru.android.R;
import ru.mail.park.android.utils.Tools;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.Date;
import hirondelle.date4j.DateTime;


public class SchedulerCaldroidFragment extends CaldroidFragment {

	public static final String DASH_ID_BUNDLE = "DASH_ID_BUNDLE";
	@Nullable static DateTime currentDateTime = null;

	private OnDateClickListener onDateClickListener;
	private Long dashID;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			dashID = savedInstanceState.getLong(DASH_ID_BUNDLE);
		}
		if (getArguments() != null) {
			dashID = getArguments().getLong(DASH_ID_BUNDLE);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (currentDateTime != null) {
			moveToDateTime(currentDateTime);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(DASH_ID_BUNDLE, dashID);
	}


	public void setOnDateClickListener(OnDateClickListener onDateClickListener) {
		this.onDateClickListener = onDateClickListener;
	}

	public void enableClicks() {
		this.setCaldroidListener(new SchedulerCaldroidListener());
	}

	final class SchedulerCaldroidListener extends CaldroidListener {

		@Override
		public void onSelectDate(Date date, View view) {
			if (onDateClickListener != null) {
				onDateClickListener.onSelectDate(date);
			}
		}

		@Override
		public void onLongClickDate(Date date, @Nullable View view) {

			// Create fragment and set arguments
			final Fragment fragment = new CreateEventFragment();
			final Bundle bundle = new Bundle();

			bundle.putBoolean(CreateEventFragment.IS_NEW_BUNDLE, true);
			bundle.putSerializable(CreateEventFragment.DATE_BUNDLE, date);
			bundle.putLong(CreateEventFragment.DASH_ID_BUNDLE, dashID);
			fragment.setArguments(bundle);

			// Replace content in FrameLayout-container
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();

			// Remember the date to return at the same page of calendar
			currentDateTime = Tools.getDate(date);
		}
	}

	public interface OnDateClickListener {
		void onSelectDate(@NonNull final Date date);
	}
}
