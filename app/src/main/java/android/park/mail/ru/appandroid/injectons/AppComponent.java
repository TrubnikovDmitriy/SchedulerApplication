package android.park.mail.ru.appandroid.injectons;

import android.park.mail.ru.appandroid.fragments.dashboards.LocalDashboardsFragment;
import android.park.mail.ru.appandroid.fragments.dashboards.ServerDashboardsFragment;
import android.park.mail.ru.appandroid.fragments.events.CreateEventFragment;
import android.park.mail.ru.appandroid.fragments.events.LocalEventsFragment;
import android.park.mail.ru.appandroid.fragments.events.ServerEventsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {AppModule.class, DataBaseModule.class, NetworkModule.class})
@Singleton
public interface AppComponent {

	void inject(LocalDashboardsFragment localDashboards);

	void inject(LocalEventsFragment localEvents);

	void inject(ServerDashboardsFragment localDashboards);

	void inject(ServerEventsFragment localEvents);

	void inject(CreateEventFragment createEventFragment);
}
