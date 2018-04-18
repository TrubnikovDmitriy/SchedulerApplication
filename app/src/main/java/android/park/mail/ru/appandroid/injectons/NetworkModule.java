package android.park.mail.ru.appandroid.injectons;

import android.park.mail.ru.appandroid.network.ServerAPI;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class NetworkModule {

	@Provides
	@Singleton
	public ServerAPI getNetworkManager() {
		return new ServerAPI();
	}
}
