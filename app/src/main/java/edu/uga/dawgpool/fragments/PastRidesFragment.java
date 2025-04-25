package edu.uga.dawgpool.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.uga.dawgpool.MainActivity;
import edu.uga.dawgpool.R;
import edu.uga.dawgpool.adapters.RideAdapter;
import edu.uga.dawgpool.models.Ride;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PastRidesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PastRidesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> rideList = new ArrayList<>();
    private DatabaseReference dbRef;
    private FirebaseUser user;

    public PastRidesFragment() {
        // Required empty public constructor
    }

    public static PastRidesFragment newInstance() {
        return new PastRidesFragment();
    }

    /**
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return The View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Past Rides");
        View view = inflater.inflate(R.layout.fragment_past_rides, container, false);

        recyclerView = view.findViewById(R.id.pastRidesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rideAdapter = new RideAdapter(rideList, requireActivity(), "completed");
        recyclerView.setAdapter(rideAdapter);
        dbRef = FirebaseDatabase.getInstance().getReference("rides");
        loadPastRides();

        return view;
    }

    /**
     * Loads all past rides from the Firebase Database for the signed in user.
     */
    private void loadPastRides() {
        Query query = dbRef.orderByChild("status").equalTo("completed");

        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        Log.d(MainActivity.LOG_TAG, "loadPastRides: Current user uid: " + uid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Fresh list so we aren't sharing the list with the other fragments
                List<Ride> freshList = new ArrayList<>();
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    if (ride != null) {
                        if (uid.equals(ride.postedBy) || uid.equals(ride.acceptedBy)) {
                            Log.d(MainActivity.LOG_TAG, "loadPastRides: Adding this ride to the rideList");
                            freshList.add(ride);
                        }
                    }
                    String postedBy = ride.postedBy;
                    String acceptedBy = ride.acceptedBy;
                    Log.d(MainActivity.LOG_TAG, "loadPastRides: postedBy: " + postedBy);
                    Log.d(MainActivity.LOG_TAG, "loadPastRides: acceptedBy: " + acceptedBy);

                }

                Collections.sort(freshList, Comparator.comparingLong(r -> r.datetime));
                rideList.clear();
                rideList.addAll(freshList);
                rideAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(MainActivity.LOG_TAG, "PastRidesFragment - loadPastRides Error: " + error.getDetails());
                Toast.makeText(getContext(), "Failed to load past rides.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}