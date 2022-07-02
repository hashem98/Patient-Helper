package com.patient.patienthelper.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.core.Amplify;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.patient.patienthelper.R;
import com.patient.patienthelper.api.Disease;
import com.patient.patienthelper.helperClass.HashTable.HashTable;
import com.patient.patienthelper.helperClass.MySharedPreferences;
import com.patient.patienthelper.helperClass.UserLogIn;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    public static List<AuthUserAttribute> userAttributes = new ArrayList<>();
    private TextInputEditText email;
    private TextInputEditText password;
    private MaterialButton loginBtn;
    private TextView signupBtn;
    private CheckBox deviceRememberCheckBox;
    private TextView forgetPassword;
    private String emailString;
    private String passwordString;
    Animation scaleDown,scaleUp;
    LottieAnimationView loading;
    UserLogIn userLogIn;
    MySharedPreferences mySharedPreferences;
    HashTable hashTable = new HashTable<>(20);


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mySharedPreferences = new MySharedPreferences(this);
        inflateViews();
        setUPButton();
        setAllViewsAnim();

    }
    private void setAllViewsAnim() {
        setAnim(forgetPassword);
        setAnim(email);
        setAnim(password);
        setAnim(deviceRememberCheckBox);
        setAnim(loginBtn);
        setAnim(signupBtn);

    }

    private void setAnim(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction()==MotionEvent.ACTION_DOWN)
                {
                    view.startAnimation(scaleUp);
                } else if (event.getAction()==MotionEvent.ACTION_UP)
                {
                    view.startAnimation(scaleDown);
                }

                return false;
            }
        });
    }

    //    inflate all views to be able to reach
    private void inflateViews() {
        scaleDown= AnimationUtils.loadAnimation(this,(R.anim.scale_down));
        scaleUp= AnimationUtils.loadAnimation(this,(R.anim.scale_up));
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.signin_btn);
        signupBtn = findViewById(R.id.create_account_button);
        deviceRememberCheckBox = findViewById(R.id.remember_device_checkBox);
        loading = findViewById(R.id.loading);
        forgetPassword = findViewById(R.id.forget_password);

    }

    // method to hold Listeners
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setUPButton() {

        signupBtn.setOnClickListener(view -> {
            startActivity(new Intent(this, SignUpActivity.class));
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);


        });

        loginBtn.setOnClickListener(view -> {
            getAllAsString();
            loading.setVisibility(View.VISIBLE);
            loginButtonAction();
        });

        forgetPassword.setOnClickListener(view -> {
            startActivity(new Intent(this, ForgetPasswordActivity.class));
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);

        });
    }

    //  get all the strings from views
    private void getAllAsString() {

        emailString = email.getText().toString().trim();
        passwordString = password.getText().toString().trim();
    }

    //    validate email
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loginButtonAction() {

        if (TextUtils.isEmpty(emailString) || !emailString.contains("@")) {

            email.setError("Enter valid Email");
            loading.setVisibility(View.INVISIBLE);

        } else if (TextUtils.isEmpty(passwordString)) {

            password.setError("Enter Password");
            loading.setVisibility(View.INVISIBLE);

        } else {

            login();

        }

        View view2 = this.getCurrentFocus();
        if (view2 != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
        }
    }

//    setup aws login

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void login() {


        Amplify.Auth.signIn(
                emailString,
                passwordString,
                result -> {
                    Log.i(TAG, result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete");
                    if (result.isSignInComplete()) {
                        if (deviceRememberCheckBox.isChecked()) {

                            rememberDevice();
                        }
                        onlineFetchCurrentUserAttributes();

                        savePasswordSharedPreferences();


                    } else {

                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            onResume();
                        });
                    }
                },
                error -> {
                    Log.e(TAG, error.toString());
                    runOnUiThread(() -> {
                        Toast.makeText(this, "username or Password Incorrect", Toast.LENGTH_SHORT).show();
                        onResume();
                    });
                }
        );

    }

    private void rememberDevice() {
        Amplify.Auth.rememberDevice(
                () -> Log.i(TAG, "Remember device succeeded"),
                error -> Log.e(TAG, "Remember device failed with error " + error)
        );

    }

    //    Fetch Current User Attributes to use them later
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onlineFetchCurrentUserAttributes() {

        Amplify.Auth.fetchUserAttributes(
                attributes -> {
                    userAttributes.clear();
                    Log.i(TAG, "User attributes = " + attributes);
                    userAttributes = attributes;
                    saveUserData();
                    runOnUiThread(() -> {

                        if (checkFirstLogin()) {
                            loading.setVisibility(View.INVISIBLE);

                            startActivity(new Intent(LoginActivity.this, LookingForActivity.class));
                            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                            finish();
                        } else {
                            getDisease();
                            loading.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                            finish();
                        }
                        finish();
                    });
                },
                error -> Log.e(TAG, "Failed to fetch user attributes.", error)
        );
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
    private void getDisease() {
        Gson gson = new Gson();

        hashTable=gson.fromJson(mySharedPreferences.getString("ApiData",null),HashTable.class);
        Disease disease = (Disease) hashTable.get(userLogIn.getDiseaseName());
        mySharedPreferences.putString("userDisease",gson.toJson(disease));
        mySharedPreferences.apply();
    }

    //    Save password in local device to remember
    private void savePasswordSharedPreferences() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferenceEditor = sharedPreferences.edit();

        preferenceEditor.putString(emailString, passwordString);
        preferenceEditor.apply();
    }

//    //  intent to home
//    private void navigateToActivity(Class activity) {
//        progressBar.setVisibility(View.INVISIBLE);
//        startActivity(new Intent(LoginActivity.this, activity.class));
//        finish();
//
//    }

    private void saveUserData() {

        System.out.println(userAttributes + "oooooooooooooo");


        String fullName = userAttributes.get(3).getValue() + " " + userAttributes.get(5).getValue();
        String diseaseName=userAttributes.get(4).getValue();
        String id = userAttributes.get(0).getValue();
        Boolean email_verified = userAttributes.get(1).getValue().equals("true");
        String firstName = userAttributes.get(3).getValue();
        String lastName = userAttributes.get(5).getValue();
        String email = userAttributes.get(6).getValue();
        String status = userAttributes.get(2).getValue();
        Boolean firstLogin = status.equals("test");
        String imageId = email.replace("@", "")
                .replace("_", "").replace("-", "")
                .replace(".", "") + "jpg";

        userLogIn = new UserLogIn(fullName, firstName, firstName, lastName, id, email, email_verified, firstLogin, status, imageId,diseaseName);
        System.out.println(userLogIn);
//        User attributes = [AuthUserAttribute {key=AuthUserAttributeKey
//    {attributeKey=sub}, value=2c96687f-cbea-424c-81a0-195bea94e5e8},
//        AuthUserAttribute {key=AuthUserAttributeKey {attributeKey=email_verified}, value=true},
//        AuthUserAttribute {key=AuthUserAttributeKey {attributeKey=custom:status1}, value=Drug conflict},
//        AuthUserAttribute {key=AuthUserAttributeKey {attributeKey=name}, value=cghbdcgdf},
//        AuthUserAttribute {key=AuthUserAttributeKey {attributeKey=custom:user_disease}, value=null},
//        AuthUserAttribute {key=AuthUserAttributeKey {attributeKey=family_name}, value=fdgfdgdfgd},
//        AuthUserAttribute {key=AuthUserAttributeKey {attributeKey=email}, value=hashemsmadi98@gmail.com}]


        final Gson gson = new Gson();
        String serializedObject = gson.toJson(userLogIn);
        mySharedPreferences.putString("userLog", serializedObject);

        mySharedPreferences.apply();
    }

    private Boolean checkFirstLogin() {
        return userLogIn.getFirstLogIn();
    }


}