package ru.mail.park.android;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

import ru.mail.park.android.injectons.AppComponent;
import ru.mail.park.android.injectons.AppModule;
import ru.mail.park.android.injectons.DaggerAppComponent;


public class App extends Application {

	public static boolean isFirstTime = true;
	private static AppComponent component;

	public static AppComponent getComponent() {
		return component;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		component = DaggerAppComponent.builder()
				.appModule(new AppModule(this))
				.build();
		FirebaseDatabase.getInstance().setPersistenceEnabled(true);
	}
}
