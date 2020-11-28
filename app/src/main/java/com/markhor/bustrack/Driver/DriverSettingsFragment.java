package com.markhor.bustrack.Driver;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.markhor.bustrack.LoginActivity;
import com.markhor.bustrack.ModelClasses.DriverInformation;
import com.markhor.bustrack.R;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DriverSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverSettingsFragment extends Fragment {
    private TextInputLayout mDriverName, mDriverEmail, mDriverPhoneNumber, mDriverBusNumber;
    private Button mDriverLogoutButton, mDriverUpdateInformationButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mRootRef;
    private String mCurrentDriverID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DriverSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DriverSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DriverSettingsFragment newInstance(String param1, String param2) {
        DriverSettingsFragment fragment = new DriverSettingsFragment();
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
        View view =  inflater.inflate(R.layout.fragment_driver_settings, container, false);
        mDriverLogoutButton = view.findViewById(R.id.driver_settings_logout_btn);
        mDriverName = view.findViewById(R.id.driver_setting_name);
        mDriverEmail = view.findViewById(R.id.driver_setting_email);
        mDriverPhoneNumber = view.findViewById(R.id.driver_setting_phonenumber);
        mDriverBusNumber = view.findViewById(R.id.driver_setting_busnumber);
        mDriverUpdateInformationButton = view.findViewById(R.id.driver_update_account_settings);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseFirestore.getInstance();
        mCurrentDriverID = mAuth.getCurrentUser().getUid();
        getDataOfCurrentUser();

        mDriverLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        mDriverUpdateInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDriverInformation();
            }
        });

        return view;
    }

    private void getDataOfCurrentUser() {
        DocumentReference documentReference = mRootRef.collection("Drivers").document(mCurrentDriverID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DriverInformation driverInformation = task.getResult().toObject(DriverInformation.class);
                    mDriverName.getEditText().setText(driverInformation.getName());
                    mDriverEmail.getEditText().setText(driverInformation.getEmail());
                    mDriverBusNumber.getEditText().setText(driverInformation.getBusnumber());
                    mDriverPhoneNumber.getEditText().setText(driverInformation.getPhonenumber());
                }
            }
        });
    }

    private void updateDriverInformation() {
        String name = mDriverName.getEditText().getText().toString();
        String phone = mDriverPhoneNumber.getEditText().getText().toString();

        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(phone))
            Toast.makeText(getActivity(), "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
        else {
            DocumentReference documentReference = mRootRef.collection("Drivers").document(mCurrentDriverID);
            documentReference.update("name", name, "phonenumber", phone).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: Driver Updated");
                }
            });
            DocumentReference documentReference1 = mRootRef.collection("Bus Locations").document(mCurrentDriverID);
            documentReference1.update("name", name, "phonenumber", phone).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: Bus Location Updated");
                }
            });
        }

    }
}