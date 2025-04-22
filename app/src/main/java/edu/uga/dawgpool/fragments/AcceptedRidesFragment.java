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
 * Use the {@link AcceptedRidesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AcceptedRidesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> rideList = new ArrayList<>();
    private DatabaseReference dbRef;

    private FirebaseUser user;

    public AcceptedRidesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AcceptedRidesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AcceptedRidesFragment newInstance(String param1, String param2) {
        AcceptedRidesFragment fragment = new AcceptedRidesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Accepted Rides");
        View view = inflater.inflate(R.layout.fragment_accepted_rides, container, false);

        recyclerView = view.findViewById(R.id.acceptedRidesRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rideAdapter = new RideAdapter(rideList, requireActivity());
        recyclerView.setAdapter(rideAdapter);

        dbRef = FirebaseDatabase.getInstance().getReference("rides");

        loadAcceptedRides();
        return view;
    }

    private void loadAcceptedRides() {
        Query query = dbRef.orderByChild("status").equalTo("accepted");

        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        Log.d(MainActivity.LOG_TAG, "loadAcceptedRides: Current user uid: " + uid);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);
                    String postedBy = ride.postedBy;
                    String acceptedBy = ride.acceptedBy;
                    Log.d(MainActivity.LOG_TAG, "loadAcceptedRides: postedBy: " + postedBy);
                    Log.d(MainActivity.LOG_TAG, "loadAcceptedRides: acceptedBy: " + acceptedBy);


                    if (uid.equals(postedBy) || uid.equals(acceptedBy)) {
                        Log.d(MainActivity.LOG_TAG, "loadAcceptedRides: Adding this ride to the rideList");
                        rideList.add(ride);
                    }
                }

                Collections.sort(rideList, Comparator.comparingLong(r -> r.datetime));
                rideAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(MainActivity.LOG_TAG, "AcceptedRidesFragment - loadAcceptedRides Error: " + error.getDetails());
            }
        });
    }
}