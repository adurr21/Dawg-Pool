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
import com.google.firebase.database.DatabaseReference;
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

    private void finalizeRide(Ride ride) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Fetch rider and driver points first
        usersRef.child(ride.acceptedBy).child("ridePoints").get().addOnSuccessListener(riderSnap -> {
            usersRef.child(ride.postedBy).child("ridePoints").get().addOnSuccessListener(driverSnap -> {
                int riderPoints = riderSnap.getValue(Integer.class);
                int driverPoints = driverSnap.getValue(Integer.class);

                int cost = ride.points;

                usersRef.child(ride.acceptedBy).child("ridePoints").setValue(riderPoints - cost);
                usersRef.child(ride.postedBy).child("ridePoints").setValue(driverPoints + cost);

                // Set ride to completed
                FirebaseDatabase.getInstance().getReference("rides")
                        .child(ride.getRid())
                        .child("status")
                        .setValue("completed");

                Toast.makeText(activity, "Ride completed!", Toast.LENGTH_SHORT).show();
            });
        });
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

        if (mode.equals("accepted") || mode.equals("completed")) {
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

            if (mode.equals("completed")) {
                holder.manageOtherRideButtons.setVisibility(View.GONE);
                holder.confirmButton.setVisibility(View.GONE);
                return;
            }

            if (mode.equals("accepted")) {
                holder.manageOtherRideButtons.setVisibility(View.GONE);
                holder.confirmButton.setVisibility(View.VISIBLE);

                boolean isDriver = ride.postedBy.equals(currentUser.getUid());
                boolean isRider = ride.acceptedBy != null && ride.acceptedBy.equals(currentUser.getUid());
                boolean hasConfirmed = (isDriver && ride.confirmedByDriver) || (isRider && ride.confirmedByRider);

                holder.confirmButton.setText(hasConfirmed ? "Confirmed" : "Confirm Ride Completed");
                holder.confirmButton.setEnabled(!hasConfirmed);

                holder.confirmButton.setOnClickListener(v -> {
                    String fieldToUpdate = isDriver ? "confirmedByDriver" : "confirmedByRider";
                    FirebaseDatabase.getInstance().getReference("rides")
                            .child(ride.getRid())
                            .child(fieldToUpdate)
                            .setValue(true)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(v.getContext(), "Confirmation recorded", Toast.LENGTH_SHORT).show();
                                if (isDriver) ride.confirmedByDriver = true;
                                else ride.confirmedByRider = true;
                                notifyItemChanged(position);

                                // If both confirmed, finalize ride
                                if (ride.confirmedByDriver && ride.confirmedByRider) {
                                    finalizeRide(ride);

                                    // remove from adapter - may not need we will see
                                    rides.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, rides.size());
                                }
                            });
                });
            }

            // Offer/Request: show accept button
            else {
                holder.manageOtherRideButtons.setVisibility(View.VISIBLE);
                holder.confirmButton.setVisibility(View.GONE);

                holder.acceptButton.setOnClickListener(v -> {
                    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    String currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    String postedByUid = ride.postedBy;

                    if (ride.type.equals("offer")) {
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(postedByUid)
                                .child("email")
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    String driverEmail = snapshot.getValue(String.class);
                                    FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("status").setValue("accepted");
                                    FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("acceptedBy").setValue(currentUid);
                                    FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("riderEmail").setValue(currentEmail);
                                    FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("driverEmail").setValue(driverEmail);

                                    Toast.makeText(v.getContext(), "Ride accepted", Toast.LENGTH_SHORT).show();
                                    rides.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, rides.size());
                                });
                    } else {
                        FirebaseDatabase.getInstance().getReference("users")
                                .child(postedByUid)
                                .child("email")
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    String riderEmail = snapshot.getValue(String.class);
                                    FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("status").setValue("accepted");
                                    FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("acceptedBy").setValue(currentUid);
                                    FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("driverEmail").setValue(currentEmail);
                                    FirebaseDatabase.getInstance().getReference("rides").child(ride.getRid()).child("riderEmail").setValue(riderEmail);

                                    Toast.makeText(v.getContext(), "Ride accepted", Toast.LENGTH_SHORT).show();
                                    rides.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, rides.size());
                                });
                    }
                });
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
        Button editButton, deleteButton, acceptButton, confirmButton;

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
            confirmButton = itemView.findViewById(R.id.confirmButton);
        }
    }
}