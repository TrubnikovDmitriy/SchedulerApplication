package android.park.mail.ru.appandroid.injectons;

import android.park.mail.ru.appandroid.network.ServerAPI;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
class NetworkModule {

	@Provides
	@Singleton
	ServerAPI getNetworkManager() {
		return new ServerAPI();
	}
}
