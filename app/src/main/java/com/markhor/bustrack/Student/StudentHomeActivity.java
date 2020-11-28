package com.markhor.bustrack.Student;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.markhor.bustrack.Admin.AdminBusFragment;
import com.markhor.bustrack.Admin.AdminHomeFragment;
import com.markhor.bustrack.Admin.AdminSettingsFragment;
import com.markhor.bustrack.Admin.AdminStudentFragment;
import com.markhor.bustrack.Driver.DriverHomeFragment;
import com.markhor.bustrack.ModelClasses.BusLocations;
import com.markhor.bustrack.ModelClasses.StudentInformation;
import com.markhor.bustrack.R;

import java.util.ArrayList;

public class StudentHomeActivity extends AppCompatActivity {

    private static final String TAG = "StudentHomeActivity";
    private FragmentManager mFragmentManager;
    private ChipNavigationBar mStudentNavigationMenu;
    private String mBusNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        mStudentNavigationMenu = findViewById(R.id.student_nav);
        mFragmentManager = getSupportFragmentManager();

        if(savedInstanceState == null)
        {
            mStudentNavigationMenu.setItemSelected(R.id.student_home, true);
            Fragment homeFragment = new StudentHomeFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.student_frame_layout, homeFragment)
                    .commit();
        }
        mStudentNavigationMenu.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i)
                {
                    case R.id.student_home:
                        fragment = new StudentHomeFragment();
                        break;
                    case R.id.student_settings:
                        fragment = new StudentSettingsFragment();
                        break;
                }
                if(fragment != null)
                {
                    mFragmentManager.beginTransaction()
                            .replace(R.id.student_frame_layout, fragment)
                            .commit();
                }
                else
                    Log.d("Fragment: ", "Fragment Selection Error");
            }
        });
    }
}