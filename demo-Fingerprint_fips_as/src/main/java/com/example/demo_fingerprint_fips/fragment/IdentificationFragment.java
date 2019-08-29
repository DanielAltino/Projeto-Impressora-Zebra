package com.example.demo_fingerprint_fips.fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;


import com.example.demo_fingerprint_fips.FileUtils;
import com.example.demo_fingerprint_fips.MainActivity;
import com.example.demo_fingerprint_fips.R;
import com.rscja.deviceapi.Fingerprint.BufferEnum;
import com.rscja.deviceapi.FingerprintWithFIPS;
import com.rscja.utility.StringUtility;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;

import static android.content.Context.TELEPHONY_SERVICE;
import static java.lang.Thread.getDefaultUncaughtExceptionHandler;
import static java.lang.Thread.sleep;

public class IdentificationFragment extends KeyDwonFragment implements View.OnClickListener{
	private MainActivity mContext;

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
		mContext = (MainActivity) getActivity();
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

				mContext.playSound(1);
			}else{
				mContext.playSound(2);
			}
			btnIdent.setEnabled(true);
		}

	}
}
