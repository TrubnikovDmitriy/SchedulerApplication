package ru.mail.park.android.injectons;

import ru.mail.park.android.fragments.dashboards.LocalDashboardsFragment;
import ru.mail.park.android.fragments.dashboards.ServerDashboardsFragment;
import ru.mail.park.android.fragments.calendar.CreateEventFragment;
import ru.mail.park.android.fragments.events.LocalEventsFragment;
import ru.mail.park.android.fragments.events.ServerEventsFragment;

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
