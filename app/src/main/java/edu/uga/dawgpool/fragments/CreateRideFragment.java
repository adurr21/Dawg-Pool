package edu.uga.dawgpool.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.uga.dawgpool.MainActivity;
import edu.uga.dawgpool.R;
import edu.uga.dawgpool.models.Ride;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateRideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateRideFragment extends Fragment {
    private static final String CREATE_STATUS = "create_status";
    private DatabaseReference dbReference;
    FirebaseUser user;
    private EditText pickupEditText;
    private EditText dropOffEditText;
    private Button submitButton;

    private String mCreateStatus;

    public CreateRideFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Either "driver" or "rider" depending on if the driver is creating a ride or a rider
     * @return A new instance of fragment CreateRideFragment.
     */
    public static CreateRideFragment newInstance(String param1) {
        CreateRideFragment fragment = new CreateRideFragment();
        Bundle args = new Bundle();
        args.putString(CREATE_STATUS, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCreateStatus = getArguments().getString(CREATE_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_create_ride, container, false);

        pickupEditText = view.findViewById(R.id.pickupEditText);
        dropOffEditText = view.findViewById(R.id.dropoffEditText);
        submitButton = view.findViewById(R.id.submitButton);

        user = FirebaseAuth.getInstance().getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        Log.d(MainActivity.LOG_TAG, "Arguments for CreateRideFragment: " + mCreateStatus);

        if (mCreateStatus.equals("rider")) {
            ((MainActivity) requireActivity()).setToolbarTitle("Create Ride Request");
            submitButton.setText("Submit new Ride Request");
        } else {
            ((MainActivity) requireActivity()).setToolbarTitle("Create Ride Offer");
            submitButton.setText("Submit new Ride Offer");
        }

        submitButton.setOnClickListener(v -> {
            // get ride ID and rideType
            String rid = dbReference.child("rides").push().getKey();
            String rideType = mCreateStatus.equals("rider") ? "request" : "offer";
            // create ride object
            Ride ride = new Ride(
                    rid,
                    rideType,
                    dropOffEditText.getText().toString(),
                    pickupEditText.getText().toString(),
                    System.currentTimeMillis(),
                    user.getUid(),
                    null,
                    new String("open"),
                    50
            );

            // Save it under "rides" node
            dbReference.child("rides").child(rid).setValue(ride)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Successfully written to database
                            Toast.makeText(getContext(), "Ride created successfully!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to write to database
                            Toast.makeText(getContext(), "Failed to create ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        return view;
    }
}