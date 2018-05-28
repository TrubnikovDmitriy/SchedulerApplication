package ru.mail.park.android;

import android.os.StrictMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.park.android.fragments.dashboards.LocalDashboardsFragment;
import ru.mail.park.android.fragments.dashboards.ServerDashboardsFragment;

import park.mail.ru.android.R;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	@BindView(R.id.drawer_layout)
	DrawerLayout drawerLayout;

	@BindView(R.id.main_navigation)
	NavigationView mainNavigation;

	@BindView(R.id.toolbar)
	Toolbar toolbar;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO remove
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectActivityLeaks()
				.detectLeakedSqlLiteObjects()
				.build());
		setContentView(R.layout.main_activity);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawerLayout, toolbar,
				R.string.navigation_drawer_open,
				R.string.navigation_drawer_close
		);
		drawerLayout.addDrawerListener(toggle);
		toggle.syncState();

		mainNavigation.setNavigationItemSelectedListener(this);
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {

		final Fragment fragment;
		switch (item.getItemId()) {
			case R.id.local_dashboards:
				fragment = new LocalDashboardsFragment();
				break;

			case R.id.server_dashboards:
				fragment = new ServerDashboardsFragment();
				break;

			default:
				Log.w(this.getClass().getSimpleName(), "Fall to default navigation case");
				return false;
		}

		final FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager
				.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		fragmentManager
				.beginTransaction()
				.replace(R.id.container, fragment)
				.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.commit();

		drawerLayout.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
			drawerLayout.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}
}
