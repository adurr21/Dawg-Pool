package edu.uga.dawgpool.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.uga.dawgpool.R;
import edu.uga.dawgpool.models.Ride;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rides;

    public RideAdapter(List<Ride> rides) {
        this.rides = rides;
    }

    //  called when RecyclerView creates a new row
    @Override
    public RideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_item, parent, false);
        return new RideViewHolder(view);
    }

    // to fill in the content for each row
    @Override
    public void onBindViewHolder(RideViewHolder holder, int position) {
        Ride ride = rides.get(position);

        holder.fromText.setText("From: " + ride.from);
        holder.toText.setText("To: " + ride.to);

        Date date = new Date(ride.datetime);
        String formattedDate = new SimpleDateFormat("EEE, MMM d, h:mm a", Locale.getDefault()).format(date);
        holder.dateText.setText("When: " + formattedDate);
    }

    // how many items to display
    @Override
    public int getItemCount() {
        return rides.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView fromText, toText, dateText;

        public RideViewHolder(View itemView) {
            super(itemView);
            fromText = itemView.findViewById(R.id.fromText);
            toText = itemView.findViewById(R.id.toText);
            dateText = itemView.findViewById(R.id.dateText);
        }
    }
}