package edu.uga.dawgpool.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import edu.uga.dawgpool.R;

public class RegisterFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private Button registerBtn, goToLoginBtn;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        mAuth = FirebaseAuth.getInstance();
        emailEditText = view.findViewById(R.id.registerEmail);
        passwordEditText = view.findViewById(R.id.registerPassword);
        registerBtn = view.findViewById(R.id.registerButton);
        goToLoginBtn = view.findViewById(R.id.goToLoginButton);

        registerBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Navigate to login or dashboard
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new LoginFragment())
                                    .commit();
                        } else {
                            Toast.makeText(getContext(), "Registration failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        goToLoginBtn.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit());

        return view;
    }
}