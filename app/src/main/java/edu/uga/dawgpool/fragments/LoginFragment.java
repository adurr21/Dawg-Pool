package edu.uga.dawgpool.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import edu.uga.dawgpool.R;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText;
    private Button loginBtn, goToRegisterBtn;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();
        emailEditText = view.findViewById(R.id.loginEmail);
        passwordEditText = view.findViewById(R.id.loginPassword);
        loginBtn = view.findViewById(R.id.loginButton);
        goToRegisterBtn = view.findViewById(R.id.goToRegisterButton);

        loginBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Email and password must not be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Check Email Format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new DashboardFragment())
                                    .commit();
                        } else {
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getContext(), "Incorrect email or password.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        goToRegisterBtn.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new RegisterFragment())
                        .commit());

        return view;
    }
}



