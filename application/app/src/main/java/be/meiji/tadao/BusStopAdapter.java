package be.meiji.tadao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BusStopAdapter extends RecyclerView.Adapter<BusStopAdapter.BusStopViewHolder> {

  private List<BusStop> busStops;
  private OnBusStopClickListener listener;

  // Constructor takes the list of bus stops and a click listener
  public BusStopAdapter(List<BusStop> busStops, OnBusStopClickListener listener) {
    this.busStops = busStops;
    this.listener = listener;
  }

  @NonNull
  @Override
  public BusStopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_bus_stop, parent, false);
    return new BusStopViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull BusStopViewHolder holder, int position) {
    BusStop busStop = busStops.get(position);
    holder.nameTextView.setText(busStop.getName());
    holder.postcodeCityTextView.setText(busStop.getPostalCode() + ", " + busStop.getCityName());

    // Set click listener for each bus stop item
    holder.itemView.setOnClickListener(
        v -> listener.onBusStopClick(busStop.getId(), busStop.getName(), busStop.getCityName()));
  }

  @Override
  public int getItemCount() {
    return busStops.size();
  }

  // Define an interface for the click event
  public interface OnBusStopClickListener {

    void onBusStopClick(int stopId, String stopName, String cityName);
  }

  public static class BusStopViewHolder extends RecyclerView.ViewHolder {

    TextView nameTextView;
    TextView postcodeCityTextView;

    public BusStopViewHolder(@NonNull View itemView) {
      super(itemView);
      nameTextView = itemView.findViewById(R.id.text_name);
      postcodeCityTextView = itemView.findViewById(R.id.text_postcode_city);
    }
  }
}
