package android.park.mail.ru.appandroid.fragments;

import android.os.Bundle;
import android.park.mail.ru.appandroid.R;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class LocalDashboardsFragment extends Fragment {

	public LocalDashboardsFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_local_dashboards, container, false);
	}

}
