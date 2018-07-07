package ru.mail.park.android.recycler;

import ru.mail.park.android.R;
import ru.mail.park.android.models.Dashboard;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardHolder> {

	static public class DashboardHolder extends RecyclerView.ViewHolder {

		private CardView cardItemView;
		private TextView textView;
		private Dashboard dashboard;

		DashboardHolder(CardView cardItemView) {
			super(cardItemView);
			this.cardItemView = cardItemView;
			this.textView = cardItemView.findViewById(R.id.dash_title);
		}

		private void updateContent(@NonNull final Dashboard dashboard) {
			this.dashboard = dashboard;
			textView.setText(dashboard.getTitle());
		}

		public Dashboard getDashboard() {
			return dashboard;
		}
	}

	@Nullable private OnDashboardClickListener onItemClickListener;
	@NonNull private ArrayList<Dashboard> dashSet;

	public DashboardAdapter(@NonNull final ArrayList<Dashboard> dataset,
	                        @Nullable final OnDashboardClickListener listener) {
		this.onItemClickListener = listener;
		this.dashSet = dataset;
	}

	@NonNull
	@Override
	public DashboardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		// Inflate item
		final CardView itemView = (CardView) LayoutInflater.from(parent.getContext())
				.inflate(R.layout.holder_dashboard, parent, false);
		// Create holder and set listener
		final DashboardHolder holder = new DashboardHolder(itemView);
		holder.cardItemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onItemClickListener != null) {
					onItemClickListener.onClick(dashSet.get(holder.getAdapterPosition()));
				}
			}
		});
		return holder;
	}

	@Override
	public void onBindViewHolder(@NonNull final DashboardHolder holder, final int position) {
		holder.updateContent(dashSet.get(position));
	}

	@Override
	public int getItemCount() {
		return dashSet.size();
	}


	public interface OnDashboardClickListener {

		void onClick(@NonNull final Dashboard dashboard);
	}
}
