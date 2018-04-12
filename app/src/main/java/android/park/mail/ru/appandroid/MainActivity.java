package android.park.mail.ru.appandroid;

import android.park.mail.ru.appandroid.fragments.LocalDashboardsFragment;
import android.park.mail.ru.appandroid.fragments.ServerDashboardsFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

	private DrawerLayout drawerLayout;
	private ListView navigationMenuList;
	private ArrayList<String> navigationMenu;

	public static final int LOCAL = 0;
	public static final int SERVER = 1;

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
	}

	private class NavigationMenuListener implements AdapterView.OnItemClickListener  {

		@Override
		public void onItemClick(AdapterView<?> adapterView,
		                        View view, int position, long id) {

			selectMenuItem(position);
			setActionBarTitle(position);

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
					Log.i(this.getClass().getSimpleName(), LocalDashboardsFragment.class.getSimpleName());
					break;

				case SERVER:
					fragment = new ServerDashboardsFragment();
					Log.i(this.getClass().getSimpleName(), ServerDashboardsFragment.class.getSimpleName());
					break;

				default:
					Log.w(this.getClass().getSimpleName(), "Fall to default case");
					return;
			}

			final FragmentManager manager = getSupportFragmentManager();
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.replace(R.id.container, fragment);
			transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			transaction.commit();
		}

		private void setActionBarTitle(int position) {
			final ActionBar bar = getSupportActionBar();
			if (bar == null) {
				return;
			}

			try {
				bar.setTitle(navigationMenu.get(position));
			} catch (IndexOutOfBoundsException e) {
				Log.w(getClass().getSimpleName(), e);
				bar.setTitle(R.string.app_name);
			}
		}
	}
}
