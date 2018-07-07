package ru.mail.park.android.injectons;

import ru.mail.park.android.database.RealtimeDatabase;
import ru.mail.park.android.fragments.dashboards.PublicDashboardsFragment;
import ru.mail.park.android.fragments.events.PublicEventsFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = { AppModule.class })
@Singleton
public interface AppComponent {

	void inject(RealtimeDatabase.FailEventListener failValueListener);
}
