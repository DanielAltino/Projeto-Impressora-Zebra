package com.zebra.printstationcard.fingerprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zebra.printstationcard.R;


public class FirstActivity extends AppCompatActivity {

    Button btnSignup;
    Button btnPrintCard;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_main);

        btnSignup = (Button) findViewById(R.id.btnSignup);
        btnPrintCard = (Button) findViewById(R.id.btnPrintCard);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FirstActivity.this, "WorkiING WHEREEEEEEEE", Toast.LENGTH_SHORT).show();
                Intent formIntent = new Intent(FirstActivity.this, FormActivity.class);
                startActivity(formIntent);
            }
        });

        btnPrintCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FirstActivity.this, "Identification", Toast.LENGTH_SHORT).show();
                Intent formIntent = new Intent(FirstActivity.this, IdentificationActivity.class);
                startActivity(formIntent);
            }
        });
    }
}

