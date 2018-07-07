package ru.mail.park.android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.mail.park.android.fragments.dashboards.PrivateDashboardsFragment;
import ru.mail.park.android.fragments.dashboards.PublicDashboardsFragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	public static final String SUBSCRIBE_MESSAGE = "SUBSCRIBE_MESSAGE";
	public static final int SUBSCRIBE_MESSAGE_ID = 0;
	public static final int RC_SIGN_IN = 1;

	private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

	@BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
	@BindView(R.id.main_navigation) NavigationView mainNavigation;
	@BindView(R.id.toolbar) Toolbar toolbar;
	private TextView signEmail;
	private TextView signName;
	private ImageView signPhoto;

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


		mainNavigation.inflateHeaderView(R.layout.main_nav_header);
		mainNavigation.setNavigationItemSelectedListener(this);
		final View navigationHeader = mainNavigation.getHeaderView(0);
		signEmail = navigationHeader.findViewById(R.id.sign_email);
		signName = navigationHeader.findViewById(R.id.sign_name);
		signPhoto = navigationHeader.findViewById(R.id.sign_photo);


		// Channels
		if (Build.VERSION.SDK_INT >= 26) {
			final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			if (manager != null) {
				NotificationChannel defaultChannel = new NotificationChannel(
						SUBSCRIBE_MESSAGE,
						getString(R.string.channel_subscribe_message),
						NotificationManager.IMPORTANCE_DEFAULT
				);
				manager.createNotificationChannel(defaultChannel);
			}
		}

		// Authentication
		firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
		if (firebaseUser != null) {
			updateSignUI(firebaseUser);
		} else if (App.isFirstTime) {
			authentication();
		}

		if (App.isFirstTime) {
			onNavigationItemSelected(mainNavigation.getMenu().findItem(R.id.local_dashboards));
		}

		App.isFirstTime = false;
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {

		final Fragment fragment;
		switch (item.getItemId()) {
			case R.id.local_dashboards:
				fragment = new PrivateDashboardsFragment();
				break;

			case R.id.server_dashboards:
				fragment = new PublicDashboardsFragment();
				break;

			case R.id.sign_out:
				FirebaseAuth.getInstance().signOut();
				updateSignUI(null);
				firebaseUser = null;
				return true;

			case R.id.sign_in:
				authentication();
				return true;

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
			if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
				App.isFirstTime = true;
			}
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case RC_SIGN_IN:
				final IdpResponse response = IdpResponse.fromResultIntent(data);
				if (resultCode == RESULT_OK) {
					firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
					this.updateSignUI(firebaseUser);
				} else {
					if (response != null && response.getError() != null) {
						Toast.makeText(
								this,
								response.getError().getLocalizedMessage(),
								Toast.LENGTH_LONG
						).show();
					} else {
						updateSignUI(firebaseUser);
					}
				}
		}
	}


	private void authentication() {
		List<AuthUI.IdpConfig> providers = Arrays.asList(
				new AuthUI.IdpConfig.EmailBuilder().build(),
				new AuthUI.IdpConfig.PhoneBuilder().build(),
				new AuthUI.IdpConfig.GoogleBuilder().build()
		);
		startActivityForResult(
				AuthUI.getInstance()
						.createSignInIntentBuilder()
						.setAvailableProviders(providers)
						.setLogo(R.drawable.sign_up_logo)
						.setTheme(R.style.AppTheme)
						.build(),
				RC_SIGN_IN
		);
	}

	private void updateSignUI(@Nullable final FirebaseUser firebaseUser) {

		if (firebaseUser != null) {
			Picasso.get()
					.load(firebaseUser.getPhotoUrl() == null ? Uri.EMPTY : firebaseUser.getPhotoUrl())
					.error(R.drawable.nav_default_avatar)
					.resizeDimen(R.dimen.avatar_width, R.dimen.avatar_height)
					.into(signPhoto);

			signName.setText(firebaseUser.getDisplayName());
			signEmail.setText(firebaseUser.getEmail());
			signName.setVisibility(View.VISIBLE);

			mainNavigation.getMenu().findItem(R.id.sign_in).setVisible(false);
			mainNavigation.getMenu().findItem(R.id.sign_out).setVisible(true);

		} else {
			signPhoto.setImageDrawable(
					AppCompatResources.getDrawable(this, R.drawable.nav_logo));

			signEmail.setText(R.string.app_name);
			signName.setText(null);
			signName.setVisibility(View.INVISIBLE);

			mainNavigation.getMenu().findItem(R.id.sign_in).setVisible(true);
			mainNavigation.getMenu().findItem(R.id.sign_out).setVisible(false);
		}
	}
}
