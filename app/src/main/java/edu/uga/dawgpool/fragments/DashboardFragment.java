package edu.uga.dawgpool.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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

        Button viewOffersButton = view.findViewById(R.id.viewOffersBtn);
        Button viewRequestsButton = view.findViewById(R.id.viewRequestsBtn);

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

        return view;
    }
}