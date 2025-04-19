package edu.uga.dawgpool.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.uga.dawgpool.R;
import edu.uga.dawgpool.models.User;

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

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Email and password must not be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Check Email Format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            // Add user to database
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            User newUser = new User(userId, email, 150); // user starts with 150 ride points
                            usersRef.child(userId).setValue(newUser);

                            Toast.makeText(getContext(), "Account created successfully.", Toast.LENGTH_SHORT).show();

                            // Navigate to login
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new LoginFragment())
                                    .commit();
                        } else {
                            Exception e = task.getException();
                            Log.e("RegisterFragment", "Registration error", e);
                            String errorMsg = "Registration failed.";
                            if (e != null && e.getMessage() != null) {
                                String msg = e.getMessage();
                                if (msg.contains("The email address is already in use")) {
                                    errorMsg = "That email is already registered.";
                                }
                            }
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
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