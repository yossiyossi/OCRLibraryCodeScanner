package com.google.android.gms.samples.vision.ocrreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class WelcomeScreen extends AppCompatActivity {

    public static Boolean search = false;
    public static String nums[];
    public static String callNumTarget;
    public static ArrayList<String> target = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_screen);

        Button searchButton = (Button) findViewById(R.id.searchButton);
        Button sortButton = (Button) findViewById(R.id.sortButton);


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText callNumEditText = (EditText) findViewById(R.id.callNumEditText);
                String callNums = callNumEditText.getText().toString();
                search = true;

                callNumTarget = callNums;
                nums = callNums.split("[\\r\\n]+");

                for (int i = 1; i < nums.length; i++) {
                    target.add(nums[i]);
                }
                startActivity(new Intent(WelcomeScreen.this, OcrCaptureActivity.class));
            }

        });

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeScreen.this, OcrCaptureActivity.class));
                search = false;
            }
        });
    }
}