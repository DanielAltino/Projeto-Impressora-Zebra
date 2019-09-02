package com.zebra.printstationcard.fingerprint;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zebra.printstationcard.MainActivity;
import com.zebra.printstationcard.R;

public class PrintCardActivity extends AppCompatActivity {

    EditText etIDToPrint, etNameToPrint, etEmailToPrint,etCPFToPrint,etRGToPrint,etStateToPrint;
    Button btnConfirmData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_card);

        Bundle extras = getIntent().getExtras();
        String userID = extras.getString("userID");
        String userName = extras.getString("userName");
        String userEmail = extras.getString("userEmail");
        String userCPF = extras.getString("userCPF");
        String userRG = extras.getString("userRG");
        String userState = extras.getString("userState");
        String userCargo = extras.getString("userCargo");
        String userTipoSang = extras.getString("userTipoSang");
        String userNascimento = extras.getString("userNascimento");
        String userGenero = extras.getString("userGenero");
        String userPorteArma = extras.getString("userPorteArma");

        etIDToPrint = (EditText) findViewById(R.id.etIDToPrint);
        etNameToPrint = (EditText) findViewById(R.id.etNameToPrint);
        etEmailToPrint = (EditText) findViewById(R.id.etEmailToPrint);
        etCPFToPrint = (EditText) findViewById(R.id.etCPFToPrint);
        etRGToPrint = (EditText) findViewById(R.id.etRGToPrint);
        etStateToPrint = (EditText) findViewById(R.id.etStateToPrint);
        btnConfirmData = (Button) findViewById(R.id.btnConfirmDataToPrint);

        etIDToPrint.setText(userID);
        etNameToPrint.setText(userName);
        etEmailToPrint.setText(userEmail);
        etCPFToPrint.setText(userCPF);
        etRGToPrint.setText(userRG);
        etStateToPrint.setText(userState);

        btnConfirmData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent zebraIntent = new Intent(PrintCardActivity.this, MainActivity.class);
                startActivity(zebraIntent);
            }
        });
    }
}
