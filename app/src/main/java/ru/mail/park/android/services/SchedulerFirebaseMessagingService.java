package ru.mail.park.android.services;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.UUID;

import ru.mail.park.android.MainActivity;
import ru.mail.park.android.R;
import ru.mail.park.android.database.RealtimeDatabase;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.models.SubscriberMessage;

import static android.content.ContentValues.TAG;


public class SchedulerFirebaseMessagingService extends FirebaseMessagingService {

	@Override
	public void onNewToken(String token) {
		Log.e(TAG, "Token from arguments: " + token);
		RealtimeDatabase.sendToken();
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {

		// Check if message contains a data payload.
		if (remoteMessage.getData().size() > 0) {
			Log.e("MESSAGE", "Message data payload: " + remoteMessage.getData());

//			SubscriberMessage message = new Gson().fromJson(
//					remoteMessage.getData().get("payload"), SubscriberMessage.class);
//
//
//
//			if (message.getInsertedEvents() != null && !message.getInsertedEvents().isEmpty()) {
//				final StringBuilder builder = new StringBuilder();
//				for (final Event event : message.getInsertedEvents()) {
//					builder.append(event.getTitle());
//					builder.append(" ");
//				}
//				showMessageNotification(getString(R.string.fcm_inserted_events), builder.toString());
//			}
//
//			if (message.getUpdatedEvents() != null && !message.getUpdatedEvents().isEmpty()) {
//				final StringBuilder builder = new StringBuilder();
//				for (final Event event : message.getUpdatedEvents()) {
//					builder.append(event.getTitle());
//					builder.append(" ");
//				}
//				showMessageNotification(getString(R.string.fcm_updated_events), builder.toString());
//			}
//
//			if (message.getDeletedEvents() != null && !message.getDeletedEvents().isEmpty()) {
//				final StringBuilder builder = new StringBuilder();
//				for (final Event event : message.getDeletedEvents()) {
//					builder.append(event.getTitle());
//					builder.append(" ");
//				}
//				showMessageNotification(getString(R.string.fcm_deleted_events), builder.toString());
//			}

		}
	}



	private static final long[] VIBRATION_PATTERN = { 0, 1000, 1000, 1000, 1000, 700, 700 };
	private static final int LIGHT_COLOR_ARGB = Color.GREEN;

	public void showMessageNotification(String title, String message) {
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		if (manager == null)
			return;

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.SUBSCRIBE_MESSAGE);
		Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.vector_assset_alert);

		builder
				.setLargeIcon(largeIcon)
				.setSmallIcon(R.mipmap.icon_update)
				.setColor(getResources().getColor(R.color.primary))
				.setContentTitle(title)
				.setContentText(message)
				.setVibrate(VIBRATION_PATTERN)
				.setLights(LIGHT_COLOR_ARGB, 100, 100)
				.setAutoCancel(false);


		manager.notify(MainActivity.SUBSCRIBE_MESSAGE_ID, builder.build());

		FirebaseMessaging fm = FirebaseMessaging.getInstance();
		fm.send(new RemoteMessage.Builder("Asd")
				.setMessageId(UUID.randomUUID().toString())
				.addData("asd", "asd")
				.build());
	}
}
