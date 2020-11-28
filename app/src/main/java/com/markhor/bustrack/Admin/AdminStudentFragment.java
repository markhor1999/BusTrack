package com.markhor.bustrack.Admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.markhor.bustrack.ModelClasses.BusLocations;
import com.markhor.bustrack.ModelClasses.DriverInformation;
import com.markhor.bustrack.ModelClasses.StudentInformation;
import com.markhor.bustrack.R;
import com.markhor.bustrack.UI.ProgressButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminStudentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminStudentFragment extends Fragment {
    //Layout Attributes
    private TextInputLayout mStudentName, mStudentEmail, mStudentPhoneNumber, mRollNumber;
    private View mStudentSignUpBtn;
    //Progress Button Class Object
    private ProgressButton progressButton;

    //Spinner
    private Spinner spinner;
    private List<String> mBusNumbers = new ArrayList<>();

    //Firebase
    FirebaseFirestore mRootRef;
    FirebaseAuth mAuth;
    private String mStudentId, mBusNumber;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdminStudentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminStudentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminStudentFragment newInstance(String param1, String param2) {
        AdminStudentFragment fragment = new AdminStudentFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin_student, container, false);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseFirestore.getInstance();

        mStudentName = view.findViewById(R.id.student_name_text);
        mStudentEmail = view.findViewById(R.id.student_email_text);
        mStudentPhoneNumber = view.findViewById(R.id.student_phone_number);
        mRollNumber = view.findViewById(R.id.student_rollnumber_text);
        spinner = view.findViewById(R.id.bus_numbers_spinner);

        mStudentSignUpBtn = view.findViewById(R.id.student_signup_button);
        progressButton = new ProgressButton(getActivity(), mStudentSignUpBtn, "Signup");

        getAllBusNumbers();
        mStudentSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressButton.buttonActivated();
                createStudent();
            }
        });
        return view;
    }

    private void createStudent() {
        final String name = mStudentName.getEditText().getText().toString();
        final String email = mStudentEmail.getEditText().getText().toString();
        final String phonenumber = mStudentPhoneNumber.getEditText().getText().toString();
        final String rollnumber = mRollNumber.getEditText().getText().toString();
        mBusNumber = spinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phonenumber) || TextUtils.isEmpty(rollnumber) || TextUtils.isEmpty(mBusNumber)) {
            Toast.makeText(getActivity(), "Please Fill All the Fields", Toast.LENGTH_SHORT).show();
            progressButton.buttonStop();
        } else {
            mAuth.createUserWithEmailAndPassword(email, phonenumber).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mStudentId = task.getResult().getUser().getUid();
                        Log.d(TAG, "onComplete: DriverId" + mStudentId);
                        uploadUserInfoToFirestore(name, email, phonenumber, mBusNumber, rollnumber);
                    } else {
                        progressButton.buttonStop();
                        Toast.makeText(getActivity(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("createUser", "User Creating Failed");
                    }
                }
            });
        }
    }

    private void uploadUserInfoToFirestore(String name, String email, String phonenumber, String mBusNumber, String rollnumber) {
        if (mStudentId != null) {
            Log.d(TAG, "uploadUserInfoToFirestore: Uploading Started");
            DocumentReference driverRef = mRootRef.collection("Students").document(mStudentId);
            StudentInformation studentInformation = new StudentInformation(mStudentId, name, email, phonenumber, phonenumber, rollnumber, mBusNumber);
            driverRef.set(studentInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mStudentEmail.getEditText().setText(null);
                        mStudentName.getEditText().setText(null);
                        mStudentPhoneNumber.getEditText().setText(null);
                        mRollNumber.getEditText().setText(null);
                        mStudentId = null;
                    } else {
                        progressButton.buttonStop();
                        Toast.makeText(getActivity(), "User Data Not Uploaded To FireStore", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Map<String, Object> type = new HashMap<>();
            type.put("userid", mStudentId);
            type.put("type", "student");

            DocumentReference typeRef = mRootRef.collection("User Type").document(mStudentId);
            typeRef.set(type).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Student Created Successfully", Toast.LENGTH_SHORT).show();
                        progressButton.buttonStop();
                    }
                }
            });

        } else {
            Toast.makeText(getActivity(), "Student ID is Empty.", Toast.LENGTH_SHORT).show();
            progressButton.buttonStop();
        }
    }

    private void getAllBusNumbers() {
        mRootRef.collection("Drivers").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot snapshot:queryDocumentSnapshots) {
                            mBusNumbers.add(snapshot.get("busnumber").toString());
                        }
                        ArrayAdapter<String> langAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mBusNumbers );
                        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(langAdapter);
                    }
                });
    }
}