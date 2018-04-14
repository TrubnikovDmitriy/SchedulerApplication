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

	static class DashboardHolder extends RecyclerView.ViewHolder {

		private CardView cardItemView;
		private TextView textButton;

		DashboardHolder(CardView cardItemView) {
			super(cardItemView);
			this.cardItemView = cardItemView;
			this.textButton = cardItemView.findViewById(R.id.dash_title);
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
		holder.textButton.setText(dashSet.get(position).getTitle());
		if (onItemClickListener != null) {
			onItemClickListener.setDashboard(dashSet.get(position));
			holder.cardItemView.setOnClickListener(onItemClickListener);
		}
	}

	@Override
	public int getItemCount() {
		return dashSet.size();
	}

	public void setNewDataset(@Nullable ArrayList<ShortDashboard> newDataset) {
		this.dashSet = (newDataset != null) ?
				newDataset : new ArrayList<ShortDashboard>();
	}

	public static abstract class OnDashboardClickListener implements View.OnClickListener {

		private ShortDashboard dashboard;

		private void setDashboard(ShortDashboard dashboard) {
			this.dashboard = dashboard;
		}

		protected ShortDashboard getDashboard() {
			return dashboard;
		}
	}
}
