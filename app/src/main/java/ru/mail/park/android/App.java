package ru.mail.park.android;

import android.app.Application;
import ru.mail.park.android.injectons.AppComponent;
import ru.mail.park.android.injectons.AppModule;
import ru.mail.park.android.injectons.DaggerAppComponent;


public class App extends Application {

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
	}
}
