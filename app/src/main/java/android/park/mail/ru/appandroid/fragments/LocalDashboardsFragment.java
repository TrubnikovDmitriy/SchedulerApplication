package android.park.mail.ru.appandroid.fragments;

import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.park.mail.ru.appandroid.R;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LocalDashboardsFragment extends Fragment {

	CalendarView calendar;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public LocalDashboardsFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		final View view = inflater.inflate(
				R.layout.fragment_local_dashboards, container, false);


//		executor.execute(new Run());

		final CaldroidFragment caldroid = new CaldroidFragment();
		final CaldroidFragment caldroid2 = new CaldroidFragment();

		ColorDrawable blue = new ColorDrawable(getResources().getColor(R.color.caldroid_sky_blue));
//		int oneDay = 1000 /*ms*/ * 60 * 60 * 24 * 3;
		caldroid.setTextColorForDate(R.color.caldroid_darker_gray, new Date());
		caldroid.setBackgroundDrawableForDate(blue, new Date(System.currentTimeMillis()));



		Bundle args = new Bundle();
		args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS, false);
//		args.putBoolean(CaldroidFragment.ENABLE_SWIPE, false);
		caldroid.setArguments(args);
		caldroid2.setArguments(args);

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.caldroid_container, caldroid)
				.replace(R.id.caldroid_container_2, caldroid2)
				.commit();

		return view;
	}
}
