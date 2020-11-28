package com.markhor.bustrack.Student;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.model.DirectionsResult;
import com.markhor.bustrack.ModelClasses.BusLocations;
import com.markhor.bustrack.ModelClasses.ClusterMarker;
import com.markhor.bustrack.ModelClasses.DriverInformation;
import com.markhor.bustrack.ModelClasses.StudentInformation;
import com.markhor.bustrack.R;
import com.markhor.bustrack.UI.MyClusterManagerRenderer;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.markhor.bustrack.UI.Constants.LOCATION_UPDATE_INTERVAL;
import static com.markhor.bustrack.UI.Constants.MAPVIEW_BUNDLE_KEY;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentHomeFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {
    //For Maps
    private MapView mapView;
    private FusedLocationProviderClient mFusedLocationClient;
    //Firebase Objects
    private FirebaseAuth mAuth;
    private FirebaseFirestore mRootRef;
    //
    private BusLocations mBusLocations;
    private ArrayList<BusLocations> mBusLocationArrayList = new ArrayList<>();
    //Google Maps
    private GoogleMap mGoogleMap;
    //Handler
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    //Array List Of Markers
    private ArrayList<Marker> mMarkersList = new ArrayList<Marker>();
    private int mMarkerIndex;
    //Cluster Item
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    //Bus Number From Activity
    private String mBusNumber;
    private GeoApiContext mGeoApiContext = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StudentHomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StudentHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentHomeFragment newInstance(String param1, String param2) {
        StudentHomeFragment fragment = new StudentHomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_student_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseFirestore.getInstance();

        mapView = view.findViewById(R.id.map_bus_map);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());


        initiateGoogleMap(savedInstanceState);
        getCurrentStudentData();

        return view;
    }

    private void getCurrentStudentData() {
        FirebaseFirestore.getInstance().collection("Students").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    StudentInformation studentInformation = task.getResult().toObject(StudentInformation.class);
                    mBusNumber = studentInformation.getBusnumber();
                    Log.d(TAG, "onComplete: " + mBusNumber);
                    getTheDriver(mBusNumber);
                }
            }
        });
    }

    private void startLocationRunnable() {
        mHandler.postDelayed(mRunnable = new Runnable() {
            @Override
            public void run() {
                int position = 0;
                getLocationUpdatesAgain(position);
                mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
            }
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopBusLocationUpdates() {
        mHandler.removeCallbacks(mRunnable);
    }

    //Get The Location Updates From The Database Every 3 Sec
    private void getLocationUpdatesAgain(final int position) {
        Log.d(TAG, "getLocationUpdatesAgain: Getting Location Updates Again");
        try {
            for (final ClusterMarker clusterMarker : mClusterMarkers) {
                DocumentReference documentReference = FirebaseFirestore.getInstance()
                        .collection("Bus Locations")
                        .document(clusterMarker.getDriverInformation().getId());
                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            final BusLocations updatedBusLocations = task.getResult().toObject(BusLocations.class);

                            //Update the Location
                            for (int i = 0; i < mClusterMarkers.size(); i++) {
                                try {
                                    if (mClusterMarkers.get(i).getDriverInformation().getId().equals(updatedBusLocations.getDriverInformation().getId())) {
                                        LatLng updatedLatLng = new LatLng(
                                                updatedBusLocations.getGeoPoint().getLatitude(),
                                                updatedBusLocations.getGeoPoint().getLongitude()
                                        );
                                        mClusterMarkers.get(i).setPosition(updatedLatLng);
                                        mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));
                                    }
                                } catch (NullPointerException e) {
                                    Log.d(TAG, "onComplete: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }
        } catch (IllegalStateException e) {
            Log.d(TAG, "getLocationUpdated: " + e.getMessage());
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

        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }
    }

    private void calculateDirections(Marker marker) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        32.0736519, 72.6781505
                        /*mUserPosition.getGeo_point().getLatitude(),
                        mUserPosition.getGeo_point().getLongitude()*/
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });
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
        mGoogleMap = googleMap;
        googleMap.setOnInfoWindowClickListener(this);
    }

    private void getTheDriver(String a) {
        Log.d(TAG, "onMapReady: " + a);
        Query driverRef = mRootRef.collection("Drivers").whereEqualTo("busnumber", a);
        driverRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    DriverInformation driverInformation = documentSnapshot.toObject(DriverInformation.class);
                    getTheBusLocationOfThisDriver(driverInformation);
                }
            }
        });
    }

    private void getTheBusLocationOfThisDriver(DriverInformation driverInformation) {
        DocumentReference busLocationsRef = mRootRef.collection("Bus Locations").document(driverInformation.getId());
        busLocationsRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                if (value != null && value.exists()) {
                    Log.d(TAG, "getAllTheBusLocations: ID = " + value.getGeoPoint("geoPoint"));
                    BusLocations busLocations = value.toObject(BusLocations.class);
                    Double lat = busLocations.getGeoPoint().getLatitude();
                    Double lng = busLocations.getGeoPoint().getLongitude();
                    mBusLocationArrayList.add(busLocations);
                    //addAllMarkers(lat, lng);
                    addMapMarkers(busLocations);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    private void addMapMarkers(BusLocations busLocations) {
        if (mGoogleMap != null) {
            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(getActivity().getApplicationContext(), mGoogleMap);
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        getActivity(),
                        mGoogleMap,
                        mClusterManager
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            try {
                String snippet = "Determine route to " + busLocations.getDriverInformation().getName() + "?";
                ClusterMarker newClusterMarker = new ClusterMarker(
                        new LatLng(busLocations.getGeoPoint().getLatitude(), busLocations.getGeoPoint().getLongitude()),
                        busLocations.getDriverInformation().getName(),
                        snippet,
                        busLocations.getDriverInformation()
                );
                mClusterManager.addItem(newClusterMarker);
                mClusterMarkers.add(newClusterMarker);

            } catch (NullPointerException e) {
                Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
            }
            mClusterManager.cluster();
            animateTheCameraToUni(busLocations.getGeoPoint().getLatitude(), busLocations.getGeoPoint().getLongitude());
        }
    }

    private void animateTheCameraToUni(double latitude, double longitude) {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(14)
                .tilt(20)
                .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
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
        startLocationRunnable();
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
        stopBusLocationUpdates();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "onInfoWindowClick: Clicked");
        if (marker.getSnippet().equals("This is you")) {
            marker.hideInfoWindow();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(marker.getSnippet())
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            calculateDirections(marker);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }
    }
}