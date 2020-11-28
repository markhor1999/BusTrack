package com.markhor.bustrack.Admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.markhor.bustrack.LoginActivity;
import com.markhor.bustrack.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminSettingsFragment extends Fragment {
    private TextInputLayout mAdminEmail, mAdminName;

    private Button mAdminLogoutButton;
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

    public AdminSettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminSettingsFragment newInstance(String param1, String param2) {
        AdminSettingsFragment fragment = new AdminSettingsFragment();
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
        View view =  inflater.inflate(R.layout.fragment_admin_settings, container, false);
        mAdminLogoutButton = view.findViewById(R.id.admin_settings_logout_btn);
        mAdminEmail = view.findViewById(R.id.admin_setting_email);
        mAdminName = view.findViewById(R.id.admin_setting_name);
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseFirestore.getInstance();
        mCurrentStudentID = mAuth.getCurrentUser().getUid();

        getDataOfCurrentUser();

        mAdminLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        return view;
    }

    private void getDataOfCurrentUser() {
        DocumentReference documentReference = mRootRef.collection("Admins").document(mCurrentStudentID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    mAdminEmail.getEditText().setText(task.getResult().get("emal").toString());
                    mAdminName.getEditText().setText(task.getResult().get("name").toString());
                }
            }
        });
    }
}