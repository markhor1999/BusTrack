package com.markhor.bustrack.Student;

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
import com.markhor.bustrack.ModelClasses.StudentInformation;
import com.markhor.bustrack.R;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudentSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudentSettingsFragment extends Fragment {
    private TextInputLayout mStudentName, mStudentEmail, mStudentPhoneNumber, mStudentBusNumber;
    private Button mStudentLogoutButton, mStudentUpdateInformationButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mRootRef;
    private String mCurrentStudentID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StudentSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StudentSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StudentSettingsFragment newInstance(String param1, String param2) {
        StudentSettingsFragment fragment = new StudentSettingsFragment();
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
        View view =  inflater.inflate(R.layout.fragment_student_settings, container, false);
        mStudentLogoutButton = view.findViewById(R.id.student_settings_logout_btn);
        mStudentName = view.findViewById(R.id.student_setting_name);
        mStudentEmail = view.findViewById(R.id.student_setting_email);
        mStudentPhoneNumber = view.findViewById(R.id.student_setting_phonenumber);
        mStudentBusNumber = view.findViewById(R.id.student_setting_busnumber);
        mStudentUpdateInformationButton = view.findViewById(R.id.student_update_account_settings);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseFirestore.getInstance();
        mCurrentStudentID = mAuth.getCurrentUser().getUid();
        getDataOfCurrentUser();

        mStudentLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        mStudentUpdateInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStudentInformation();
            }
        });

        return view;
    }

    private void getDataOfCurrentUser() {
        DocumentReference documentReference = mRootRef.collection("Students").document(mCurrentStudentID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    StudentInformation studentInformation = task.getResult().toObject(StudentInformation.class);
                    mStudentName.getEditText().setText(studentInformation.getName());
                    mStudentEmail.getEditText().setText(studentInformation.getEmail());
                    mStudentBusNumber.getEditText().setText(studentInformation.getBusnumber());
                    mStudentPhoneNumber.getEditText().setText(studentInformation.getPhonenumber());
                }
            }
        });
    }

    private void updateStudentInformation() {
        String name = mStudentName.getEditText().getText().toString();
        String phone = mStudentPhoneNumber.getEditText().getText().toString();

        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(phone))
            Toast.makeText(getActivity(), "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
        else {
            DocumentReference documentReference = mRootRef.collection("Students").document(mCurrentStudentID);
            documentReference.update("name", name, "phonenumber", phone).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getActivity(), "Updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: Student Updated");
                }
            });
        }

    }
}