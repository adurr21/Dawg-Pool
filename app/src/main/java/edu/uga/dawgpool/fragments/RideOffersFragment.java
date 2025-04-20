package edu.uga.dawgpool.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uga.dawgpool.MainActivity;
import edu.uga.dawgpool.R;

public class RideOffersFragment extends Fragment {

    public RideOffersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) requireActivity()).setToolbarTitle("Ride Offers");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ride_offers, container, false);
    }
}