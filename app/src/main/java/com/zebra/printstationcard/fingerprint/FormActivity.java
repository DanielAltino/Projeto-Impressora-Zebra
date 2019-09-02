package com.zebra.printstationcard.fingerprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zebra.printstationcard.R;
import com.zebra.printstationcard.util.UIHelper;

import java.util.ArrayList;

public class FormActivity extends AppCompatActivity {

    private Button btnGoToPicture;
    private EditText etName, etSobrenome, etCpf, etRg, etState, etCargo, etTipoSanguineo, etNascimento, etGenero, etPorteArma, etRegistro;
    private String name, sobrenome, cpf, rg, state, cargo, tipo_sanguineo, nascimento, genero, porte_arma, registro, dados;


    private ArrayList<String> Arquivos = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        UIHelper.setLogoOnActionBar(this);

        init();

        btnGoToPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getString();
                Intent cameraIntent = new Intent(FormActivity.this, CameraActivity.class);
                cameraIntent.putExtra("Dados", dados);
                startActivity(cameraIntent);
            }
        });
    }

    private void init() {
        btnGoToPicture = (Button) findViewById(R.id.btnGoToPicture);
        etName = (EditText) findViewById(R.id.etName);
        etSobrenome = (EditText) findViewById(R.id.etSobrenome);
        etRegistro = (EditText) findViewById(R.id.etRegistro);
        etCpf = (EditText) findViewById(R.id.etCPF);
        etRg = (EditText) findViewById(R.id.etRG);
        etState = (EditText) findViewById(R.id.etState);
        etCargo = (EditText) findViewById(R.id.etCargo);
        etTipoSanguineo = (EditText) findViewById(R.id.etTipoSanguineo);
        etNascimento = (EditText) findViewById(R.id.etDataNasc);
        etGenero = (EditText) findViewById(R.id.etGenero);
        etPorteArma = (EditText) findViewById(R.id.etPorte);
    }

    private String getString() {
        name = etName.getText().toString();
        sobrenome = etSobrenome.getText().toString();
        registro = etRegistro.getText().toString();
        cpf = etCpf.getText().toString();
        rg = etRg.getText().toString();
        state = etState.getText().toString();
        cargo = etCargo.getText().toString();
        tipo_sanguineo = etTipoSanguineo.getText().toString();
        nascimento = etNascimento.getText().toString();
        genero = etGenero.getText().toString();
        porte_arma = etPorteArma.getText().toString();

        dados = "NAME:" + name + "£" + "SOBRENOME:" + sobrenome + "£" + "CPF:" + cpf + "£" + "RG:" + rg + "£" + "REGISTRO:" + registro +"£"+ "ESTADO:" + state +"£" + "CARGO:" + cargo
                +"£" + "TIPO_SANGUINEO:" + state + "£" + "NASCIMENTO:" + nascimento +"£" + "GENERO:" + genero +"£" + "PORTE_ARMA:" + porte_arma +";";

        return dados;
    }



}
