package ru.mail.park.android.fragments.calendar;

import android.os.Bundle;
import ru.mail.park.android.utils.Tools;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
	@Nullable private OnDateClickListener onDateClickListener;
	@Nullable private OnLongDateClickListener onLongDateClickListener;

	private String dashID;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			dashID = savedInstanceState.getString(DASH_ID_BUNDLE);
		}
		if (getArguments() != null) {
			dashID = getArguments().getString(DASH_ID_BUNDLE);
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (currentDateTime != null) {
			moveToDateTime(currentDateTime);
		}
		this.setCaldroidListener(new SchedulerCaldroidListener());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(DASH_ID_BUNDLE, dashID);
	}


	public void setOnDateClickListener(@Nullable OnDateClickListener listener) {
		this.onDateClickListener = listener;
	}

	public void setOnLongDateClickListener(@Nullable OnLongDateClickListener listener) {
		this.onLongDateClickListener = listener;
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
			if (onLongDateClickListener != null) {
				// Remember the date to return at the same page of calendar
				currentDateTime = Tools.getDate(date);
				onLongDateClickListener.onLongClickDate(date);
			}
		}
	}

	public interface OnDateClickListener {
		void onSelectDate(@NonNull final Date date);
	}

	public interface OnLongDateClickListener {
		void onLongClickDate(@NonNull final Date date);
	}
}
