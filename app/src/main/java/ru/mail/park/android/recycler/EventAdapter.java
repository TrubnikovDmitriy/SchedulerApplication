package ru.mail.park.android.recycler;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import park.mail.ru.android.R;
import ru.mail.park.android.models.Event;
import ru.mail.park.android.models.ShortDashboard;
import ru.mail.park.android.utils.Tools;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventHolder> {

	static class EventHolder extends RecyclerView.ViewHolder {

		private CardView cardItemView;
		private TextView title;
		private TextView time;
		private TextView description;
		private TextView priority;
		private TextView type;

		EventHolder(CardView cardItemView) {
			super(cardItemView);
			this.cardItemView = cardItemView;
			this.title = cardItemView.findViewById(R.id.event_title);
			this.time = cardItemView.findViewById(R.id.event_time);
			this.description = cardItemView.findViewById(R.id.event_description);
			this.priority = cardItemView.findViewById(R.id.event_priority);
			this.type = cardItemView.findViewById(R.id.event_type);
		}

		private void updateContent(@NonNull final Event event) {
			title.setText(event.getTitle());
			description.setText(event.getText());

			final Date date = Tools.getDate(event.getTimestamp());
			time.setText(Tools.formatTime(date));
		}
	}

	@NonNull private ArrayList<Event> eventSet;

	public EventAdapter(@Nullable final ArrayList<Event> dataset) {
		this.eventSet = (dataset != null) ? dataset : new ArrayList<Event>();
	}

	@Override
	public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		// Inflate item
		final CardView itemView = (CardView) LayoutInflater.from(parent.getContext())
				.inflate(R.layout.holder_event, parent, false);
		return new EventHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final EventHolder holder, final int position) {
		holder.updateContent(eventSet.get(position));
	}

	@Override
	public int getItemCount() {
		return eventSet.size();
	}

	public void setNewDataset(@Nullable ArrayList<Event> newDataset) {
		this.eventSet = (newDataset != null) ? newDataset : new ArrayList<Event>();
	}
}
