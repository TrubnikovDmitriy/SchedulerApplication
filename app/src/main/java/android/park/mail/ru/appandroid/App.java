package android.park.mail.ru.appandroid;

import android.app.Application;
import android.park.mail.ru.appandroid.injectons.AppComponent;
import android.park.mail.ru.appandroid.injectons.AppModule;
import android.park.mail.ru.appandroid.injectons.DaggerAppComponent;
import android.park.mail.ru.appandroid.injectons.DataBaseModule;


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
				.dataBaseModule(new DataBaseModule())
				.build();
	}
}
