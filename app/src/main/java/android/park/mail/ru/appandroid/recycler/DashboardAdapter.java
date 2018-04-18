package android.park.mail.ru.appandroid.recycler;

import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.models.ShortDashboard;
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
		private TextView textButton;
		private ShortDashboard dashboard;

		DashboardHolder(CardView cardItemView) {
			super(cardItemView);
			this.cardItemView = cardItemView;
			this.textButton = cardItemView.findViewById(R.id.dash_title);
		}

		private void updateContent(@NonNull final ShortDashboard dashboard) {
			this.dashboard = dashboard;
			textButton.setText(dashboard.getTitle());
		}

		public ShortDashboard getDashboard() {
			return dashboard;
		}
	}

	private OnDashboardClickListener onItemClickListener;

	@NonNull
	private ArrayList<ShortDashboard> dashSet;

	public DashboardAdapter(@Nullable final ArrayList<ShortDashboard> dataset,
	                        @Nullable final OnDashboardClickListener listener) {
		this.onItemClickListener = listener;
		this.dashSet = (dataset != null) ? dataset : new ArrayList<ShortDashboard>();
	}

	@Override
	public DashboardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final CardView itemView = (CardView) LayoutInflater.from(parent.getContext())
				.inflate(R.layout.dashboard_holder, parent, false);
		return new DashboardHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final DashboardHolder holder, final int position) {
		holder.updateContent(dashSet.get(position));
		holder.cardItemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onItemClickListener != null) {
					onItemClickListener.onClick(dashSet.get(holder.getAdapterPosition()));
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return dashSet.size();
	}

	public void setNewDataset(@Nullable ArrayList<ShortDashboard> newDataset) {
		this.dashSet = (newDataset != null) ?
				newDataset : new ArrayList<ShortDashboard>();
	}

	public interface OnDashboardClickListener {

		void onClick(@NonNull final ShortDashboard dashboard);
	}
}
