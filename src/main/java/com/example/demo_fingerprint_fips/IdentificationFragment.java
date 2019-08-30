package com.example.demo_fingerprint_fips;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.FingerprintWithFIPS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class IdentificationFragment extends KeyDwonFragment implements OnClickListener{


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


	public String infoCliente;
	private ArrayList<String> Arquivos = new ArrayList<String>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.i(TAG, "onCreateView()");
		View v = inflater.inflate(R.layout.fingerprint_identification_fragment,
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
				mContext.mFingerprint.stopIdentification();
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

                click_Carregar();

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


	public void click_Carregar()
	{
		String lstrNomeArq;
		File arq;
		String lstrlinha;
		try
		{


			arq = new File(Environment.getExternalStorageDirectory(), "arquivo.txt");
			BufferedReader br = new BufferedReader(new FileReader(arq));

			while ((lstrlinha = br.readLine()) != null)
			{
				Toast.makeText(mContext, lstrlinha, Toast.LENGTH_SHORT).show();
			}


			Mensagem("Texto Carregado com sucesso!");

		}
		catch (Exception e)
		{
			Mensagem("Erro : " + e.getMessage());
		}
	}
}
