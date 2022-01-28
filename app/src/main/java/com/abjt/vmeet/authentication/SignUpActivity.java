package com.abjt.vmeet.authentication;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.abjt.vmeet.R;
import com.abjt.vmeet.activities.MainActivity;
import com.abjt.vmeet.utils.Constants;
import com.abjt.vmeet.utils.SharedPreferenceManager;
import com.abjt.vmeet.utils.Toaster;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText inputFirstName, inputLastName, inputEmail, inputPassword, inputConfirmPassword;
    private MaterialButton signUpButton;
    private ProgressBar loading;

    //Toaster
    private Toaster toaster;
    private SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();
        setListeners();
    }


    private void init() {
        inputFirstName = findViewById(R.id.inputFirstName);
        inputLastName = findViewById(R.id.inputLastName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        signUpButton = findViewById(R.id.buttonSignUp);
        loading = findViewById(R.id.loading);

        //Custom
        if (getApplicationContext() != null) {
            sharedPreferenceManager = new SharedPreferenceManager(getApplicationContext());
            toaster = new Toaster(getApplicationContext());
        }
    }

    private void setListeners() {
        findViewById(R.id.imageBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.textSignIn).setOnClickListener(v -> onBackPressed());

        signUpButton.setOnClickListener(v -> validate());
    }

    private void validate() {
        if (inputFirstName.getText().toString().trim().isEmpty()) {
            showToast("Enter First Name");
        } else if (inputLastName.getText().toString().trim().isEmpty()) {
            showToast("Enter Last Name");
        } else if (inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter Email");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()) {
            showToast("Enter valid Email");
        } else if (inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter Password");
        } else if (inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm Password");
        } else if (!inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())) {
            showToast("Password & Confirm Password must be same");
        } else {
            signUp();
        }
    }

    private void signUp() {
        toggleLoading(true);
        //Database instance
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        //The User Document
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_FIRST_NAME, inputFirstName.getText().toString());
        user.put(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
        user.put(Constants.KEY_EMAIl, inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, inputPassword.getText().toString());

        //inserting the user Hashmap in the document of the collection user
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    sharedPreferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    sharedPreferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    sharedPreferenceManager.putString(Constants.KEY_FIRST_NAME, inputFirstName.getText().toString());
                    sharedPreferenceManager.putString(Constants.KEY_LAST_NAME, inputLastName.getText().toString());
                    sharedPreferenceManager.putString(Constants.KEY_EMAIl, inputEmail.getText().toString());
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    toggleLoading(false);
                    showToast("Error: " + e.getMessage());
                });

    }

    private void toggleLoading(Boolean isLoading) {
        if (isLoading) {
            signUpButton.setVisibility(View.INVISIBLE);
            loading.setVisibility(View.VISIBLE);
        } else {
            signUpButton.setVisibility(View.VISIBLE);
            loading.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        toaster.showToast(message);
    }
}