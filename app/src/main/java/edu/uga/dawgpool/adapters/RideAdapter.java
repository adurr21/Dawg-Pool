package edu.uga.dawgpool.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.uga.dawgpool.R;
import edu.uga.dawgpool.fragments.CreateRideFragment;
import edu.uga.dawgpool.fragments.RideOffersFragment;
import edu.uga.dawgpool.fragments.RideRequestsFragment;
import edu.uga.dawgpool.models.Ride;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private List<Ride> rides;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FragmentActivity activity;

    public RideAdapter(List<Ride> rides, FragmentActivity activity) {
        this.rides = rides;
        this.activity = activity;
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

        // Only show edit/delete buttons if user is the one who posted it
        if (ride.postedBy.equals(currentUser.getUid()) && "open".equals(ride.status)) {
            holder.manageMyRideButtons.setVisibility(View.VISIBLE);

            holder.editButton.setOnClickListener(v -> {
                String mode = ride.type.equals("request") ? "rider" : "driver";
                CreateRideFragment editFragment = CreateRideFragment.newInstance(mode, ride);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            });

            holder.deleteButton.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference("rides")
                        .child(ride.getRid())
                        .removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(v.getContext(), "Ride deleted", Toast.LENGTH_SHORT).show();
                            rides.remove(position); // remove from list
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, rides.size());
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(), "Failed to delete ride", Toast.LENGTH_SHORT).show());
            });
        } else {
            holder.manageMyRideButtons.setVisibility(View.GONE);
            holder.manageOtherRideButtons.setVisibility(View.VISIBLE);

            holder.acceptButton.setOnClickListener(v -> {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("rides")
                        .child(ride.getRid())
                        .child("status")
                        .setValue("accepted")
                        .addOnSuccessListener(aVoid -> {
                            // set acceptedBy after setting status
                            FirebaseDatabase.getInstance().getReference("rides")
                                    .child(ride.getRid())
                                    .child("acceptedBy")
                                    .setValue(uid)
                                    .addOnSuccessListener(aVoid2 -> {
                                        Toast.makeText(v.getContext(), "Ride accepted", Toast.LENGTH_SHORT).show();
                                        // remove from list
                                        rides.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, rides.size());

                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(v.getContext(), "Failed to update acceptedBy", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(v.getContext(), "Failed to accept ride", Toast.LENGTH_SHORT).show());
            });
        }
    }

    // how many items to display
    @Override
    public int getItemCount() {
        return rides.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView fromText, toText, dateText;
        LinearLayout manageMyRideButtons;
        LinearLayout manageOtherRideButtons;
        Button editButton, deleteButton, acceptButton;

        public RideViewHolder(View itemView) {
            super(itemView);
            fromText = itemView.findViewById(R.id.fromText);
            toText = itemView.findViewById(R.id.toText);
            dateText = itemView.findViewById(R.id.dateText);
            manageMyRideButtons = itemView.findViewById(R.id.manageMyRideButtons);
            manageOtherRideButtons = itemView.findViewById(R.id.manageOtherRideButtons);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            acceptButton = itemView.findViewById(R.id.acceptButton);
        }
    }
}