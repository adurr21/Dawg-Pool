package edu.uga.dawgpool;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.dawgpool.fragments.DashboardFragment;
import edu.uga.dawgpool.fragments.LoginFragment;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "DawgPool";
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Toolbar toolbar;
    private boolean showLogout = true;

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Main Activity onCreate - Current FirebaseUser UID: " + user.getUid());
            Log.d(LOG_TAG, "Main Activity onCreate - Current FirebaseUser email: " + user.getEmail());
        } else {
            Log.d(LOG_TAG, "Main Activity onCreate - Current User was NOT found");
        }

        // set toolbar visible by default
        toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.VISIBLE);

        if (user != null) {
            if (savedInstanceState == null) {
                // Load Dashboard Fragment since user is already logged in.
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DashboardFragment())
                        .commit();
            }
        } else {
            if (savedInstanceState == null) {
                // Load Login Fragment if a user was not found by FireBase
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit();
            }
        }


        }

    /**
     *
     * @param menu The options menu in which you place your items.
     *
     * @return boolean true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem logoutItem = menu.findItem(R.id.action_logout);
        if (logoutItem != null) {
            logoutItem.setVisible(showLogout);
        }

        return true;
    }

    /**
     * If the user chooses the logout item, sign them out and clear the back stack
     *
     * @param item The menu item that was selected.
     *
     * @return boolean true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();

            // Clear back stack so the user cannot go back
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            // Load LoginFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setToolbarVisibility(boolean visible) {
        if (toolbar != null) {
            toolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    public void setShowLogout(boolean show) {
        showLogout = show;
        invalidateOptionsMenu();
    }
}