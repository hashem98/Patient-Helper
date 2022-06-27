package com.patient.patienthelper.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.patient.patienthelper.R;
import com.patient.patienthelper.helperClass.MySharedPreferences;
import com.patient.patienthelper.helperClass.UserLogIn;

public class LookingForActivity extends AppCompatActivity {

    Button button;
    Spinner sp;
    private static final String TAG = "";
    MySharedPreferences preferences;
    UserLogIn userLogIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_looking_for);
        findViews();
        preferences = new MySharedPreferences(this);
        getData();
        SpinnerSelected();


    }

    private void getData() {

        if (preferences.contains("userLog")) {
            Gson gson = new Gson();
            userLogIn = gson.fromJson(preferences.getString("userLog", "noData"), UserLogIn.class);

        }
    }

    public void findViews() {
        button = findViewById(R.id.submit_looking_for);
        sp = (Spinner) findViewById(R.id.spinner_looking_for);
    }

    public void SpinnerSelected() {


        button.setOnClickListener(v -> {
            String Selected = String.valueOf(sp.getSelectedItem());
            if (Selected.equals("Patient")) {
                saveData("Patient");
                Intent i = new Intent(this, Select_illActivity.class);
                startActivity(i);

            } else if (Selected.equals("Drug conflict")) {
                saveData("Drug conflict");
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            } else {
                saveData("another");
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
            }

        });


    }

    private void saveData(String status) {

        Gson gson = new Gson();
        String serializedObject = gson.toJson(userLogIn);
        preferences.putString("userLog", serializedObject);
        preferences.putBoolean("FirstLog", false);
        preferences.putString("userStatus",status);
        preferences.apply();
    }


}