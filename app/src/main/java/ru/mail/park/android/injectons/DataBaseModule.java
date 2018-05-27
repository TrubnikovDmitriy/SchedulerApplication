package ru.mail.park.android.injectons;

import android.content.Context;
import ru.mail.park.android.database.SchedulerDBHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class DataBaseModule {

	@Provides
	@Singleton
	SchedulerDBHelper getDataBaseManager(Context context) {
		return new SchedulerDBHelper(context);
	}
}
