package com.zebra.printstationcard.fingerprint;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.FingerprintWithFIPS;
import com.zebra.printstationcard.MainActivity;
import com.zebra.printstationcard.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class IdentificationFragment extends KeyDwonFragment implements View.OnClickListener {


    private IdentificationActivity mContext;

    private static final String TAG = "IdentificationFragment";
    Button btnIdent;
    ScrollView scroll;
    Button PowerOn;
    Button Stop;
    TextView tvInfo;
    TextView tvID;
    TextView tvVersion;
    String oldMsg="";
    Handler handler=new Handler();

    TextView tvTest;

    String userID = "";
    String userName = "";
    String userEmail = "";
    String userCPF = "";
    String userRG = "";
    String userState = "";
    Button btnPrintCard;


    public String infoCliente;
    private ArrayList<String> Arquivos = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.activity_identification_fragment,
                container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (IdentificationActivity) getActivity();
        init();
    }
    @Override
    public void onPause() {
        Log.d(TAG,"onPause");
        // TODO Auto-generated method stub
        super.onPause();
        mContext.mFingerprint.stopIdentification();
        tvInfo.setText("");
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG,"onResume");

    }
    private void init() {
        Stop= (Button) getView().findViewById(R.id.Stop);
        btnIdent = (Button) getView().findViewById(R.id.btnIdent);
        tvInfo = (TextView) getView().findViewById(R.id.tvInfo);
        tvID= (TextView) getView().findViewById(R.id.tvID);
        scroll=(ScrollView)getView().findViewById(R.id.scroll);
        Stop.setOnClickListener(this);
        btnIdent.setOnClickListener(this);
        mContext.mFingerprint.setIdentificationCallBack(new IdentificationCall());

        tvTest = (TextView) getView().findViewById(R.id.tvTest);
        btnPrintCard = (Button) getView().findViewById(R.id.btnPrintCard);
        btnPrintCard.setOnClickListener(this);
        btnPrintCard.setVisibility(View.GONE);

    }

    public void scrollToBottom(final View inner) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnIdent:
                if(!mContext.isPower){
                    Toast.makeText(mContext,"The fingerprints did not run powered on!",Toast.LENGTH_SHORT).show();
                    return;
                }
                btnIdent.setEnabled(false);
                tvID.setText("");
                mContext.mFingerprint.startIdentification();
                break;
            case  R.id.Stop:
                Intent zebraIntent = new Intent(getActivity(), MainActivity.class);
                startActivity(zebraIntent);
                mContext.mFingerprint.stopIdentification();
                break;
            case R.id.btnPrintCard:
                Intent princCardIntent = new Intent(mContext, PrintCardActivity.class);
                princCardIntent.putExtra("userID", userID);
                princCardIntent.putExtra("userName", userName);
                princCardIntent.putExtra("userEmail", userEmail);
                princCardIntent.putExtra("userCPF", userCPF);
                princCardIntent.putExtra("userRG", userRG);
                princCardIntent.putExtra("userState", userState);
                startActivity(princCardIntent);

                break;
        }
    }

    class IdentificationCall implements FingerprintWithFIPS.IdentificationCallBack{

        @Override
        public void messageInfo(String s) {
            if(!oldMsg.equals(s)) {
                StringBuffer stringBuffer=new StringBuffer();
                stringBuffer.append(s);
                stringBuffer.append(".\r\n");
                stringBuffer.append(tvInfo.getText());
                tvInfo.setText(stringBuffer.toString());
                oldMsg=s;
                scrollToBottom(getView());
            }
        }

        @Override
        public void onComplete(boolean result, int i,int failuerCode) {
            Log.i(TAG, "failuerCode="+failuerCode);
            if(result) {
                tvID.setText("fingerprintID=" + i);

                click_Carregar(i);

                mContext.playSound(1);
            }else{
                mContext.playSound(2);
            }
            btnIdent.setEnabled(true);
        }

    }

    // GRAVAR UM ARQUIVO TEXTO

    private String ObterDiretorio() {
        File root = android.os.Environment.getExternalStorageDirectory();
        return root.toString();
    }

    public void Listar() {
        File diretorio = new File(ObterDiretorio());
        File[] arquivos = diretorio.listFiles();
        if (arquivos != null) {
            int length = arquivos.length;
            for (int i = 0; i < length; ++i) {
                File f = arquivos[i];
                if (f.isFile()) {
                    Arquivos.add(f.getName());
                }
            }

        }
    }


    private void Mensagem(String msg) {
        Toast.makeText(mContext.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


    public void click_Carregar(int userIdentificationID)
    {
        List<String> datas = new ArrayList<>();

        String lstrNomeArq;
        File arq;
        String lstrlinha;
        try
        {


            arq = new File(Environment.getExternalStorageDirectory(), "arquivo.txt");
            BufferedReader br = new BufferedReader(new FileReader(arq));

            while ((lstrlinha = br.readLine()) != null)
            {
                //Toast.makeText(mContext, lstrlinha, Toast.LENGTH_SHORT).show();
                String[] dataStringSplited = lstrlinha.split(";");
                for (String dataString : dataStringSplited) {
                    String[] dataUserSplited = dataString.split("£");
                    for(String userString : dataUserSplited){
                        datas.add(userString);
                    }
                }
            }
            if(datas != null){
                for(String dataInfos : datas){

                    if(dataInfos.contains("ID")){
                        if(dataInfos.contains(Integer.toString(userIdentificationID)))
                        {
                            //tvTest.setText("USER ID AQUI: >> " + dataInfos + "Plus TEST: " + datas.indexOf(dataInfos) + " PROXIMO E NOME : " + datas.get(datas.indexOf(dataInfos)+1));
                            for(int i = 0; i<6; i++) {
                                String info = datas.get(datas.indexOf(dataInfos) + i);
                                String infoSubstring = info.substring(info.lastIndexOf(":") + 1);
                                tvTest.setText(tvTest.getText() + infoSubstring);
                                switch (i) {
                                    case 0:
                                        userID = infoSubstring;
                                        break;
                                    case 1:
                                        userName = infoSubstring;
                                        break;
                                    case 2:
                                        userEmail = infoSubstring;
                                        break;
                                    case 3:
                                        userCPF = infoSubstring;
                                        break;
                                    case 4:
                                        userRG = infoSubstring;
                                        break;
                                    case 5:
                                        userState = infoSubstring;
                                        break;
                                }
                            }
                        }
                    }
                }
            }
            Mensagem("Texto Carregado com sucesso!");
            btnPrintCard.setVisibility(View.VISIBLE);
        }
        catch (Exception e)
        {
            Mensagem("Erro : " + e.getMessage());
        }
    }
}
