package com.markhor.bustrack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.markhor.bustrack.Admin.AdminHomeActivity;
import com.markhor.bustrack.Driver.DriverHomeActivity;
import com.markhor.bustrack.Student.StudentHomeActivity;
import com.markhor.bustrack.UI.ProgressButton;

import java.util.Objects;

import static com.markhor.bustrack.UI.Constants.ERROR_DIALOG_REQUEST;
import static com.markhor.bustrack.UI.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.markhor.bustrack.UI.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class LoginActivity extends AppCompatActivity {

    //Firebase Authentication and FireStore
    private FirebaseAuth mAuth;
    private FirebaseFirestore mRootRef;
    private String mCurrentUserId;

    //For Checking Permissions
    private boolean mLocationPermissionGranted = false;

    //layout Views
    private TextInputLayout mEmailEditText, mPasswordEditText;
    private View mLoginButton;
    private LinearLayout mMainLinearLayout;
    private ProgressBar mMainProgressBar;
    private Button mResetButton;

    //progress button class
    private ProgressButton mProgressButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        initializeVariables();
        addTextFieldListeners();
    }

    private void addTextFieldListeners() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(mEmailEditText.getEditText()).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String val = mEmailEditText.getEditText().getText().toString();

                    if (val.isEmpty()) {
                        mEmailEditText.setError("Field cannot be empty");
                    } else {
                        mEmailEditText.setError(null);
                        mEmailEditText.setErrorEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(mPasswordEditText.getEditText()).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String val = mPasswordEditText.getEditText().getText().toString();
                    if (val.isEmpty()) {
                        mPasswordEditText.setError("Field cannot be empty");
                    } else {
                        mPasswordEditText.setError(null);
                        mPasswordEditText.setErrorEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    private void initializeVariables() {
        //For Firebase
        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseFirestore.getInstance();

        //Layout
        mEmailEditText = findViewById(R.id.main_email_edit_text);
        mLoginButton = findViewById(R.id.main_login_button);
        mPasswordEditText = findViewById(R.id.main_password_edit_text);
        mMainLinearLayout = findViewById(R.id.main_linear_layout);
        mMainProgressBar = findViewById(R.id.main_progress_bar);
        mResetButton = findViewById(R.id.main_reset_button);
        mProgressButton = new ProgressButton(this, mLoginButton, getString(R.string.login_text));

        //
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToForgotPasswordActivity();
            }
        });

        //OnClick Listener For Login Button
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMapServices()) {
                    if (!mLocationPermissionGranted)
                        getLocationPermission();
                    else {
                        if (validateFields()) {
                            mProgressButton.buttonActivated();
                            String email = mEmailEditText.getEditText().getText().toString();
                            String password = mPasswordEditText.getEditText().getText().toString();
                            loginWithEmailPassword(email, password);
                        }
                    }
                } else
                    Toast.makeText(LoginActivity.this, "Device Not Supported", Toast.LENGTH_LONG).show();

            }
        });
    }

    private void sendUserToForgotPasswordActivity() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void loginWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mCurrentUserId = mAuth.getCurrentUser().getUid();
                    sendUserToAppropriateActivity();
                } else {
                    mProgressButton.buttonStop();
                    Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean checkMapServices() {
        if (isServicesOK()) {
            return isMapsEnabled();
        }
        return false;
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work, do you want to Enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
                if (!mLocationPermissionGranted)
                    getLocationPermission();
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mLocationPermissionGranted = true;
        }
    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else
            Toast.makeText(this, "You can't make map request", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Checking If User is Already Logged In
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (!mLocationPermissionGranted) {
            getLocationPermission();
        }
        if (mLocationPermissionGranted) {
            if (firebaseUser != null) {
                mMainLinearLayout.setVisibility(View.INVISIBLE);
                mMainProgressBar.setVisibility(View.VISIBLE);
                mCurrentUserId = firebaseUser.getUid();
                Log.d("onStart", mCurrentUserId);
                sendUserToAppropriateActivity();
            }
        }
    }

    //Validating Email and Password
    private boolean validateFields() {
        return !(!validatePassword() | !validateEmail());
    }

    private Boolean validateEmail() {
        String val = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            val = Objects.requireNonNull(mEmailEditText.getEditText()).getText().toString();
        }

        if (val.isEmpty()) {
            mEmailEditText.setError("Field cannot be empty");
            return false;
        } else {
            mEmailEditText.setError(null);
            mEmailEditText.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            val = Objects.requireNonNull(mPasswordEditText.getEditText()).getText().toString();
        }
        if (val.isEmpty()) {
            mPasswordEditText.setError("Field cannot be empty");
            return false;
        } else {
            mPasswordEditText.setError(null);
            mPasswordEditText.setErrorEnabled(false);
            return true;
        }
    }

    private void sendUserToAppropriateActivity() {
        if (mCurrentUserId != null) {
            DocumentReference typeRef = mRootRef.collection("User Type").document(mCurrentUserId);
            typeRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        mProgressButton.buttonDone();
                        String userType = task.getResult().getString("type");
                        if (userType.equals("admin"))
                            sendUserToAdminHomeActivity();
                        else if (userType.equals("student"))
                            sendUserToStudentHomeActivity();
                        else if (userType.equals("driver"))
                            sendUserToDriverHomeActivity();
                    }
                    else
                    {
                        mMainProgressBar.setVisibility(View.INVISIBLE);
                        mMainLinearLayout.setVisibility(View.VISIBLE);
                        mProgressButton.buttonStop();
                        mAuth.signOut();
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            mProgressButton.buttonStop();
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }
    }

    private void sendUserToDriverHomeActivity() {
        Intent intent = new Intent(this, DriverHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void sendUserToStudentHomeActivity() {
        Intent intent = new Intent(this, StudentHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void sendUserToAdminHomeActivity() {
        Intent intent = new Intent(this, AdminHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}