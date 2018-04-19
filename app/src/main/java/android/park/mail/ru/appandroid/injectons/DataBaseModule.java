package android.park.mail.ru.appandroid.injectons;

import android.content.Context;
import android.park.mail.ru.appandroid.database.SchedulerDBHelper;

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
