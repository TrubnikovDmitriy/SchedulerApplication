package ru.mail.park.android.injectons;


import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

	private final Context context;

	public AppModule(@NonNull Context context) {
		this.context = context.getApplicationContext();
	}

	@Provides
	@Singleton
	public Context getContext() {
		return context;
	}
}
