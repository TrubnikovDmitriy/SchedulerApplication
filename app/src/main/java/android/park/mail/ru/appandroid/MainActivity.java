package android.park.mail.ru.appandroid;

import android.park.mail.ru.appandroid.fragments.dashboards.LocalDashboardsFragment;
import android.park.mail.ru.appandroid.fragments.dashboards.ServerDashboardsFragment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

	private DrawerLayout drawerLayout;
	private ListView navigationMenuList;
	private ArrayList<String> navigationMenu;

	public static final int LOCAL = 0;
	public static final int SERVER = 1;
	public static final String TITLE = "bundle_title";



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		navigationMenuList = findViewById(R.id.main_navigation);
		navigationMenu = new ArrayList<>(Arrays.asList(
				getResources().getStringArray(R.array.navigation_menu)));

		navigationMenuList.setAdapter(new ArrayAdapter<>(
				this, android.R.layout.simple_list_item_1, navigationMenu));
		navigationMenuList.setOnItemClickListener(new NavigationMenuListener());


		final ActionBar bar = getSupportActionBar();
		if (bar != null) {
			if (savedInstanceState != null) {
				final String title = savedInstanceState.getString(TITLE);
				if (title != null) {
					bar.setTitle(title);
				} else {
					bar.setTitle(R.string.app_name);
				}
			}
//			bar.setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (getSupportActionBar() != null && getSupportActionBar().getTitle() != null) {
			outState.putString(TITLE, getSupportActionBar().getTitle().toString());
		}
		super.onSaveInstanceState(outState);
	}

	private class NavigationMenuListener implements AdapterView.OnItemClickListener  {

		@Override
		public void onItemClick(AdapterView<?> adapterView,
		                        View view, int position, long id) {

			selectMenuItem(position);

			if (drawerLayout == null) {
				drawerLayout = findViewById(R.id.drawer_layout);
			}
			drawerLayout.closeDrawer(navigationMenuList);
		}

		private void selectMenuItem(int position) {
			final Fragment fragment;
			switch (position) {
				case LOCAL:
					fragment = new LocalDashboardsFragment();
					break;

				case SERVER:
					fragment = new ServerDashboardsFragment();
					break;

				default:
					Log.w(this.getClass().getSimpleName(), "Fall to default case");
					return;
			}

			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.container, fragment)
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
					.commit();
		}
	}
}
