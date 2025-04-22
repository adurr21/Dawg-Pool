package edu.uga.dawgpool.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.uga.dawgpool.MainActivity;
import edu.uga.dawgpool.R;
import edu.uga.dawgpool.adapters.RideAdapter;
import edu.uga.dawgpool.models.Ride;

public class RideOffersFragment extends Fragment {

    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> rideList = new ArrayList<>();
    private DatabaseReference ridesRef;

    public RideOffersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Ride Offers");

        View view = inflater.inflate(R.layout.fragment_ride_offers, container, false);

        // setup RecyclerView
        recyclerView = view.findViewById(R.id.rideOffersRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rideAdapter = new RideAdapter(rideList, requireActivity());
        recyclerView.setAdapter(rideAdapter);

        // load ride offers from Realtime Database
        ridesRef = FirebaseDatabase.getInstance().getReference("rides");
        loadRideOffers();

        return view;
    }

    private void loadRideOffers() {
        ridesRef.orderByChild("datetime").addListenerForSingleValueEvent(new ValueEventListener() {

            // runs when data is successfully retrieved
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                rideList.clear();

                for (DataSnapshot rideSnap : snapshot.getChildren()) {
                    Ride ride = rideSnap.getValue(Ride.class);
                    // load open offers only (not accepted or completed)
                    if (ride != null && "offer".equals(ride.type) && "open".equals(ride.status)) {
                        rideList.add(ride);
                    }
                }

                // Sort by datetime (soonest first)
                Collections.sort(rideList, Comparator.comparingLong(r -> r.datetime));
                rideAdapter.notifyDataSetChanged();
            }

            // runs when there is an error getting data
            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load rides: " + error.getMessage(), Toast.LENGTH_SHORT).show();            }
        });
    }
}