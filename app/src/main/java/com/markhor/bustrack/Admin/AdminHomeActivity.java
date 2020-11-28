package com.markhor.bustrack.Admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.markhor.bustrack.ModelClasses.BusLocations;
import com.markhor.bustrack.ModelClasses.DriverInformation;
import com.markhor.bustrack.R;

import java.util.ArrayList;

public class AdminHomeActivity extends AppCompatActivity {
    private static final String TAG = "AdminHomeActivity";
    private FragmentManager mFragmentManager;
    private ChipNavigationBar mAdminNavigationMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        mAdminNavigationMenu = findViewById(R.id.admin_nav);
        mFragmentManager = getSupportFragmentManager();

        if(savedInstanceState == null)
        {
            mAdminNavigationMenu.setItemSelected(R.id.admin_home, true);
            Fragment homeFragment = new AdminHomeFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.admin_frame_layout, homeFragment)
                    .commit();
        }
        mAdminNavigationMenu.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i)
                {
                    case R.id.admin_home:
                        fragment = new AdminHomeFragment();
                        break;
                    case R.id.admin_student_registration:
                        fragment = new AdminStudentFragment();
                        break;
                    case R.id.admin_bus_registration:
                        fragment = new AdminBusFragment();
                        break;
                    case R.id.admin_settings:
                        fragment = new AdminSettingsFragment();
                        break;
                }
                if(fragment != null)
                {
                    mFragmentManager.beginTransaction()
                            .replace(R.id.admin_frame_layout, fragment)
                            .commit();
                }
                else
                    Log.d("Fragment: ", "Fragment Selection Error");
            }
        });

    }

}