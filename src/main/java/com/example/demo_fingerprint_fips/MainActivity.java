package com.example.demo_fingerprint_fips;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity{

    Button btnSignup;
    Button btnPrintCard;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignup = (Button) findViewById(R.id.btnSignup);
        btnPrintCard = (Button) findViewById(R.id.btnPrintCard);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Working on app", Toast.LENGTH_SHORT).show();
                Intent formIntent = new Intent(MainActivity.this, FormActivity.class);
                startActivity(formIntent);
            }
        });

        btnPrintCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Identification", Toast.LENGTH_SHORT).show();
                Intent formIntent = new Intent(MainActivity.this, IdentificationActivity.class);
                startActivity(formIntent);
            }
        });
    }
}

