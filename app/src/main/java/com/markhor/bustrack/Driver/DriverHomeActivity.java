package com.markhor.bustrack.Driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.markhor.bustrack.Admin.AdminBusFragment;
import com.markhor.bustrack.Admin.AdminHomeFragment;
import com.markhor.bustrack.Admin.AdminSettingsFragment;
import com.markhor.bustrack.Admin.AdminStudentFragment;
import com.markhor.bustrack.ModelClasses.BusLocations;
import com.markhor.bustrack.ModelClasses.DriverInformation;
import com.markhor.bustrack.R;
import com.markhor.bustrack.Services.LocationService;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class DriverHomeActivity extends AppCompatActivity {
    private static final String TAG = "DriverHomeActivity";

    //
    private FragmentManager mFragmentManager;
    private ChipNavigationBar mDriverNavigationMenu;

    //
    private FusedLocationProviderClient mFusedLocationClient;

    //Bus Location Class Object
    private BusLocations mBusLocations;
    private BusLocations mBusLocations2;

    //Firebase Objects
    private FirebaseFirestore mRootRef;
    private FirebaseAuth mAuth;

    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        //
        mRootRef = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getDriverDetails();

        mDriverNavigationMenu = findViewById(R.id.driver_nav);
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            mDriverNavigationMenu.setItemSelected(R.id.driver_home, true);
            Fragment homeFragment = new DriverHomeFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.driver_frame_layout, homeFragment)
                    .commit();
        }
        mDriverNavigationMenu.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i) {
                    case R.id.driver_home:
                        fragment = new DriverHomeFragment();
                        break;
                    case R.id.driver_settings:
                        fragment = new DriverSettingsFragment();
                }
                if (fragment != null) {
                    mFragmentManager.beginTransaction()
                            .replace(R.id.driver_frame_layout, fragment)
                            .commit();
                } else
                    Log.d("Fragment: ", "Fragment Selection Error");
            }
        });

    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                ContextCompat.startForegroundService(this, serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.markhor.bustrack.services.LocationService".equals(service.service.getClassName())) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.");
        return false;
    }

    //To Get The Driver Details So That we can Store them in BusLocations Object
    private void getDriverDetails() {
        if (mBusLocations == null) {
            mBusLocations = new BusLocations();
            final DocumentReference driverCollection = mRootRef.collection("Drivers")
                    .document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
            driverCollection.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DriverInformation driver = Objects.requireNonNull(task.getResult()).toObject(DriverInformation.class);
                        ((DriverClient) (getApplicationContext())).setDriver(driver);
                        mBusLocations.setDriverInformation(driver);
                        Log.d(TAG, "onComplete: " + mBusLocations.getDriverInformation().getName());
                        getLastKnownLocation();
                    } else {
                        Toast.makeText(DriverHomeActivity.this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            getLastKnownLocation();
        }
    }

    //Saving The Bus Location to FireStore
    private void saveBusLocation() {
        if (mBusLocations != null) {
            DocumentReference locationRef = mRootRef.collection("Bus Locations").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
            locationRef.set(mBusLocations).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "saveBusLocation: \nBus Location Updated"
                                + "\n latitude: " + mBusLocations.getGeoPoint().getLatitude()
                                + "\n longitude: " + mBusLocations.getGeoPoint().getLongitude());
                    }
                }
            });
        } else {
            Log.d(TAG, "saveBusLocation: No Data Found In mBusLocation");
        }
    }

    //Getting last Location Of User
    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onSuccess: lat: " + geoPoint.getLatitude());
                    Log.d(TAG, "onSuccess: lon: " + geoPoint.getLongitude());

                    mBusLocations.setGeoPoint(geoPoint);
                    mBusLocations.setTimestamp(null);
                    saveBusLocation();
                    startLocationService();
                }
            }
        });
    }
}