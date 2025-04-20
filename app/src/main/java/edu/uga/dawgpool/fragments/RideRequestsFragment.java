package edu.uga.dawgpool.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uga.dawgpool.MainActivity;
import edu.uga.dawgpool.R;

public class RideRequestsFragment extends Fragment {


    public RideRequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((MainActivity) requireActivity()).setToolbarTitle("Ride Requests");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ride_requests, container, false);
    }
}