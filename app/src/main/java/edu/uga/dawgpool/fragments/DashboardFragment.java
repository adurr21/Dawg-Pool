package edu.uga.dawgpool.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.uga.dawgpool.MainActivity;
import edu.uga.dawgpool.R;


public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((MainActivity) requireActivity()).setToolbarTitle("Dashboard");
        ((MainActivity) requireActivity()).setShowLogout(true); // logout visible

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        TextView pointsMessageText = view.findViewById(R.id.pointsMessageText);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

            userRef.child("ridePoints").get().addOnSuccessListener(snapshot -> {
                Integer points = snapshot.getValue(Integer.class);
                if (points != null) {
                    String base = "Hello! You currently have\n" + points + " ride points.\n\n" +
                            "You can use the points to get rides around Athens, or offer to drive and earn some back!";

                    SpannableString styled = new SpannableString(base);

                    String target = points + " ride points";
                    int start = base.indexOf(target);
                    int end = start + target.length();

                    styled.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    pointsMessageText.setText(styled);
                } else {
                    pointsMessageText.setText("Could not retrieve your points.");
                }
            }).addOnFailureListener(e -> {
                pointsMessageText.setText("Error loading points.");
            });
        }

        Button viewOffersButton = view.findViewById(R.id.viewOffersBtn);
        Button viewRequestsButton = view.findViewById(R.id.viewRequestsBtn);

        Button createDriveButton = view.findViewById(R.id.createDriveButton);
        Button createRideButton = view.findViewById(R.id.createRideButton);

        Button viewAcceptedRidesButton = view.findViewById(R.id.viewAcceptedRidesButton);
        Button viewPastRidesButton = view.findViewById(R.id.viewPastRidesButton);

        viewOffersButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RideOffersFragment())
                    .addToBackStack(null)
                    .commit();
        });

        viewRequestsButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RideRequestsFragment())
                    .addToBackStack(null)
                    .commit();
        });

        createDriveButton.setOnClickListener(v -> {
            Fragment createRideFragment = new CreateRideFragment();
            Bundle args = new Bundle();
            createRideFragment.setArguments(args);
            args.putString("create_status", "driver");
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, createRideFragment)
                    .addToBackStack(null)
                    .commit();
        });

        createRideButton.setOnClickListener(v -> {
            Fragment createRideFragment = new CreateRideFragment();
            Bundle args = new Bundle();
            createRideFragment.setArguments(args);
            args.putString("create_status", "rider");
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, createRideFragment)
                    .addToBackStack(null)
                    .commit();
        });

        viewAcceptedRidesButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AcceptedRidesFragment())
                    .addToBackStack(null)
                    .commit();
        });

        viewPastRidesButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PastRidesFragment())
                    .addToBackStack(null)
                    .commit();
        });



        return view;
    }
}