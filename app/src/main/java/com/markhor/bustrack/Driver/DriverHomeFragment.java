package com.markhor.bustrack.Driver;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.clustering.ClusterManager;
import com.markhor.bustrack.ModelClasses.BusLocations;
import com.markhor.bustrack.ModelClasses.ClusterMarker;
import com.markhor.bustrack.ModelClasses.DriverInformation;
import com.markhor.bustrack.R;
import com.markhor.bustrack.UI.MyClusterManagerRenderer;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.ContentValues.TAG;
import static com.markhor.bustrack.UI.Constants.MAPVIEW_BUNDLE_KEY;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriverHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverHomeFragment extends Fragment implements OnMapReadyCallback {
    //For Maps
    private MapView mapView;


    //Firebase Objects
    private FirebaseAuth mAuth;
    private FirebaseFirestore mRootRef;
    private String mCurrentDriverId;


    //
    private BusLocations mBusLocations;
    private GeoPoint mDriverGeoPoint;
    //Google Maps
    private GoogleMap mGooleMap;
    private LatLngBounds mLatLngBounds;
    private double mDriverLat;
    private double mDriverLog;
    //


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DriverHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DriverHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DriverHomeFragment newInstance(String param1, String param2) {
        DriverHomeFragment fragment = new DriverHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_driver_home, container, false);
        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseFirestore.getInstance();
        mCurrentDriverId = mAuth.getCurrentUser().getUid();

        mapView = view.findViewById(R.id.map_bus_map);

        initiateGoogleMap(savedInstanceState);
        return view;
    }

    //get The Bus Location And Other Driver Information of Currently Logged in User
    private void getCurrentDriverDetails()
    {
        if(mBusLocations == null)
        {
            DocumentReference busLocationRef = mRootRef.collection("Bus Locations").document(mCurrentDriverId);
            busLocationRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        mBusLocations = Objects.requireNonNull(task.getResult()).toObject(BusLocations.class);
                        Log.d(TAG, "onComplete: getCurrentDriverDetails Done");
                        Log.d(TAG, "onComplete: lat: " + mBusLocations.getGeoPoint().getLatitude());
                        Log.d(TAG, "onComplete: log: " + mBusLocations.getGeoPoint().getLongitude());
                        Log.d(TAG, "onComplete: log: " + mBusLocations.getDriverInformation().getName());
                        Log.d(TAG, "onComplete: log: " + mBusLocations.getDriverInformation().getId());
                        setCameraView(mBusLocations.getGeoPoint().getLatitude(), mBusLocations.getGeoPoint().getLongitude());
                    }
                }
            });
        }
    }

    private void initiateGoogleMap(Bundle savedInstanceState) {
        //For Maps
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        mGooleMap = googleMap;
        getCurrentDriverDetails();
    }

    private void setCameraView(double latitude, double longitude)
    {
        double bottomBoundary = latitude - .1;
        double leftBoundary = longitude - .1;
        double topBoundary = latitude + .1;
        double rightBoundary = longitude + .1;
        mLatLngBounds = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );
        mGooleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds, 500));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        getCurrentDriverDetails();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}