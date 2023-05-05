package com.example.restaurantfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Globally Declaring Variables
    // EditText
    EditText inputDistance;
    EditText inputMinimumRange;
    EditText inputMaximumRange;

    // CheckBox
    CheckBox checkAny;
    CheckBox checkSeafood;
    CheckBox checkChinese;
    CheckBox checkItalian;
    CheckBox checkDessert;

    // Number Data Types
    Integer distanceMiles;
    Integer minimumPrice;
    Integer maximumPrice;

    // Function for when instance is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiating Variables
        inputDistance = (EditText) findViewById(R.id.inputDistance);
        inputMinimumRange = (EditText) findViewById(R.id.inputMinimumRange);
        inputMaximumRange = (EditText) findViewById(R.id.inputMaximumRange);

        checkAny = (CheckBox) findViewById(R.id.checkBoxAny);
        checkSeafood = (CheckBox) findViewById(R.id.checkBoxSeafood);
        checkChinese = (CheckBox) findViewById(R.id.checkBoxChinese);
        checkItalian = (CheckBox) findViewById(R.id.checkBoxItalian);
        checkDessert = (CheckBox) findViewById(R.id.checkBoxDessert);

        // Instantiating Integers
        distanceMiles = 5;
        minimumPrice = 0;
        maximumPrice = 60;
    }

    // When submit is pressed
    public void submitPress(View view){
        // Checks for any user input
        if (!(inputDistance.getText().toString().equals(""))){
            distanceMiles = Integer.parseInt(String.valueOf(inputDistance.getText()));
        }

        if (!(inputMinimumRange.getText().toString().equals(""))){
            minimumPrice = Integer.parseInt(String.valueOf(inputMinimumRange.getText()));
        }

        if (!(inputMaximumRange.getText().toString().equals(""))){
            maximumPrice = Integer.parseInt(String.valueOf(inputMaximumRange.getText()));
        }

        // Checks for checked checkboxes and adds it to an arraylist of strings
        ArrayList<String> checkYes = new ArrayList<>();
        if (checkAny.isChecked()){
            checkYes.add(checkAny.getText().toString());
            //Log.i("[CHECKBOXEESSSSSSS]","CheckAny: " + checkYes.get(0)); // Testing
        }

        if (checkSeafood.isChecked()){
            checkYes.add(checkAny.getText().toString());
        }

        if (checkChinese.isChecked()){
            checkYes.add(checkAny.getText().toString());
        }

        if (checkItalian.isChecked()){
            checkYes.add(checkAny.getText().toString());
        }

        if (checkDessert.isChecked()){
            checkYes.add(checkAny.getText().toString());
        }

        // Sending information into next activity (activity_list.java)
        //Intent intent = new Intent(MainActivity.this, activity_list.class);
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("DISTANCE_SENDER", Integer.valueOf(distanceMiles));
        intent.putExtra("MINIMUM_PRICE_SENDER", Integer.valueOf(minimumPrice));
        intent.putExtra("MAXIMUM_PRICE_SENDER", Integer.valueOf(maximumPrice));
        intent.putExtra("CHECK_YES_LIST", checkYes);
        startActivity(intent);
    }
}