package com.markhor.bustrack.Admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.markhor.bustrack.ModelClasses.DriverInformation;
import com.markhor.bustrack.R;
import com.markhor.bustrack.UI.ProgressButton;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminBusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminBusFragment extends Fragment {
    //Layout Attributes
    private TextInputLayout mDriverName, mDriverEmail, mDriverPhoneNumber, mDriverBusNumber;
    private View mDriverSignupBtn;
    //Progress Button Class Object
    private ProgressButton progressButton;

    //Firebase Object
    private FirebaseAuth mAuth;
    private FirebaseFirestore RootRef;
    private String mDriverId;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdminBusFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminBusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminBusFragment newInstance(String param1, String param2) {
        AdminBusFragment fragment = new AdminBusFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin_bus, container, false);
        //
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseFirestore.getInstance();
        ///Hoks
        mDriverName = view.findViewById(R.id.admin_bus_full_name);
        mDriverEmail = view.findViewById(R.id.admin_bus_email);
        mDriverPhoneNumber = view.findViewById(R.id.admin_bus_phone);
        mDriverBusNumber = view.findViewById(R.id.admin_bus_bus_number);

        mDriverSignupBtn = view.findViewById(R.id.admin_bus_signup_btn);
        progressButton = new ProgressButton(getActivity(), mDriverSignupBtn, "Signup");

        mDriverSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressButton.buttonActivated();
                createDriver();
            }
        });

        return view;
    }

    private void createDriver() {
        final String name = mDriverName.getEditText().getText().toString();
        final String email = mDriverEmail.getEditText().getText().toString();
        final String phoneNumber = mDriverPhoneNumber.getEditText().getText().toString();
        final String busNumber = mDriverBusNumber.getEditText().getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(busNumber)) {
            Toast.makeText(getActivity(), "Please Fill All the Fields", Toast.LENGTH_SHORT).show();
            progressButton.buttonStop();
        } else {
            mAuth.createUserWithEmailAndPassword(email, phoneNumber).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mDriverId = task.getResult().getUser().getUid();
                        uploadUserInfoToFirestore(name, email, phoneNumber, busNumber);
                    } else {
                        progressButton.buttonStop();
                        Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("createUser", "User Creating Failed");
                    }
                }
            });
        }
    }

    private void uploadUserInfoToFirestore(String name, String email, String phoneNumber, String busNumber) {

        if (mDriverId != null) {
            DocumentReference driverRef = RootRef.collection("Drivers").document(mDriverId);
            DriverInformation driverInformation = new DriverInformation(mDriverId, name, email, phoneNumber, phoneNumber, busNumber);
            driverRef.set(driverInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Driver Created Successfully", Toast.LENGTH_SHORT).show();
                        mDriverEmail.getEditText().setText(null);
                        mDriverName.getEditText().setText(null);
                        mDriverPhoneNumber.getEditText().setText(null);
                        mDriverBusNumber.getEditText().setText(null);
                        mDriverId = null;
                        progressButton.buttonDone();
                    } else {
                        progressButton.buttonStop();
                        Toast.makeText(getActivity(), "User Data Not Uploaded To FireStore", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            Map<String, Object> type = new HashMap<>();
            type.put("userid", mDriverId);
            type.put("type", "driver");

            DocumentReference typeRef = RootRef.collection("User Type").document(mDriverId);
            typeRef.set(type);
        } else {
            Toast.makeText(getActivity(), "Driver ID is Empty.", Toast.LENGTH_SHORT).show();
            progressButton.buttonStop();
        }
    }
}