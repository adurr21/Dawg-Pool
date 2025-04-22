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

    private String mode; // "offers", "requests", "accepted"

    public RideAdapter(List<Ride> rides, FragmentActivity activity, String mode) {
        this.rides = rides;
        this.activity = activity;
        this.mode = mode;
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

        if (mode.equals("accepted")) {
            holder.roleText.setVisibility(View.VISIBLE);
            if (ride.postedBy.equals(currentUser.getUid())) {
                holder.roleText.setText("Your Role: Driver");
            } else if (ride.acceptedBy != null && ride.acceptedBy.equals(currentUser.getUid())) {
                holder.roleText.setText("Your Role: Rider");
            } else {
                // should not happen
                holder.roleText.setText("Your Role: Unknown");
            }
            holder.ridePointsText.setVisibility(View.VISIBLE);
            holder.ridePointsText.setText("Ride Points: " + ride.points);
        } else {
            holder.roleText.setVisibility(View.GONE);
            holder.ridePointsText.setVisibility(View.GONE);
        }

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
            // Only show accept button if not in "accepted" fragment
            if (!mode.equals("accepted")) {
                holder.manageOtherRideButtons.setVisibility(View.VISIBLE);

                holder.acceptButton.setOnClickListener(v -> {

                    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    String postedByUid = ride.postedBy;

                    if (ride.type.equals("offer")) {
                        // user is a rider
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(postedByUid)
                                .child("email")
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    String driverEmail = snapshot.getValue(String.class);

                                    FirebaseDatabase.getInstance().getReference("rides")
                                            .child(ride.getRid())
                                            .child("status")
                                            .setValue("accepted")
                                            .addOnSuccessListener(aVoid -> {
                                                FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("acceptedBy").setValue(currentUid);
                                                FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("riderEmail").setValue(currentEmail);
                                                FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("driverEmail").setValue(driverEmail);

                                                Toast.makeText(v.getContext(), "Ride accepted", Toast.LENGTH_SHORT).show();
                                                rides.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, rides.size());
                                            });
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(v.getContext(), "Failed to fetch driver email", Toast.LENGTH_SHORT).show());
                    } else {
                        // user is accepting as a driver
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(postedByUid)
                                .child("email")
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    String riderEmail = snapshot.getValue(String.class);

                                    FirebaseDatabase.getInstance().getReference("rides")
                                            .child(ride.getRid())
                                            .child("status")
                                            .setValue("accepted")
                                            .addOnSuccessListener(aVoid -> {
                                                FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("acceptedBy").setValue(currentUid);
                                                FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("driverEmail").setValue(currentEmail);
                                                FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("riderEmail").setValue(riderEmail);

                                                Toast.makeText(v.getContext(), "Ride accepted", Toast.LENGTH_SHORT).show();
                                                rides.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, rides.size());
                                            });
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(v.getContext(), "Failed to fetch rider email", Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                holder.manageOtherRideButtons.setVisibility(View.GONE);
            }
        }
    }

    // how many items to display
    @Override
    public int getItemCount() {
        return rides.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView fromText, toText, dateText, roleText, ridePointsText;
        LinearLayout manageMyRideButtons;
        LinearLayout manageOtherRideButtons;
        Button editButton, deleteButton, acceptButton;

        public RideViewHolder(View itemView) {
            super(itemView);
            fromText = itemView.findViewById(R.id.fromText);
            toText = itemView.findViewById(R.id.toText);
            dateText = itemView.findViewById(R.id.dateText);
            roleText = itemView.findViewById(R.id.roleText);
            ridePointsText = itemView.findViewById(R.id.ridePointsText);
            manageMyRideButtons = itemView.findViewById(R.id.manageMyRideButtons);
            manageOtherRideButtons = itemView.findViewById(R.id.manageOtherRideButtons);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            acceptButton = itemView.findViewById(R.id.acceptButton);
        }
    }
}