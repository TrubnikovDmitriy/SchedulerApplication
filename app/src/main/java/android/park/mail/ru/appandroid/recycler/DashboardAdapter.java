package android.park.mail.ru.appandroid.recycler;

import android.park.mail.ru.appandroid.R;
import android.park.mail.ru.appandroid.pojo.ShortDashboard;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardHolder> {

	static class DashboardHolder extends RecyclerView.ViewHolder {

		private CardView cardItemView;
		private TextView textView;

		DashboardHolder(CardView cardItemView) {
			super(cardItemView);
			this.cardItemView = cardItemView;
			this.textView = cardItemView.findViewById(R.id.dash_title);
		}
	}

	@NonNull
	private ArrayList<ShortDashboard> dashSet;

	public DashboardAdapter(@Nullable ArrayList<ShortDashboard> dataset) {
		this.dashSet = (dataset != null) ?
				dataset : new ArrayList<ShortDashboard>();
	}

	@Override
	public DashboardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		final CardView itemView = (CardView) LayoutInflater.from(parent.getContext())
				.inflate(R.layout.dashboard_holder, parent, false);
		return new DashboardHolder(itemView);
	}

	@Override
	public void onBindViewHolder(DashboardHolder holder, int position) {
		holder.textView.setText(dashSet.get(position).getTitle());
	}

	@Override
	public int getItemCount() {
		return dashSet.size();
	}

	public void setNewDataset(@Nullable ArrayList<ShortDashboard> newDataset) {
		this.dashSet = (newDataset != null) ?
				newDataset : new ArrayList<ShortDashboard>();
	}
}
