package android.park.mail.ru.appandroid;

import android.os.StrictMode;
import android.park.mail.ru.appandroid.fragments.dashboards.LocalDashboardsFragment;
import android.park.mail.ru.appandroid.fragments.dashboards.ServerDashboardsFragment;
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

	private DrawerLayout drawerLayout;
	private NavigationView mainNavigation;
	private Toolbar toolbar;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO remove
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectActivityLeaks()
				.detectLeakedSqlLiteObjects()
				.build());

		setContentView(R.layout.activity_main);


		toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		drawerLayout = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
				this, drawerLayout, toolbar,
				R.string.navigation_drawer_open,
				R.string.navigation_drawer_close
		);
		drawerLayout.addDrawerListener(toggle);
		toggle.syncState();

		mainNavigation = findViewById(R.id.main_navigation);
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
}
