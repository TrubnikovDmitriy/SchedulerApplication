package ru.mail.park.android.injectons;

import ru.mail.park.android.network.ServerAPI;

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
