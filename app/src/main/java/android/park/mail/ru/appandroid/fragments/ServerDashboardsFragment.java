package android.park.mail.ru.appandroid.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.park.mail.ru.appandroid.R;

public class ServerDashboardsFragment extends Fragment {


	public ServerDashboardsFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_server_dashboards, container, false);
	}

}
