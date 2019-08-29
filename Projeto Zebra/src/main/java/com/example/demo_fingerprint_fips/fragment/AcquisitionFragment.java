package com.example.demo_fingerprint_fips.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo_fingerprint_fips.FileUtils;
import com.example.demo_fingerprint_fips.MainActivity;
import com.example.demo_fingerprint_fips.R;
import com.rscja.deviceapi.FingerprintWithFIPS;
import com.rscja.deviceapi.RFIDWithUHF;
import com.rscja.utility.StringUtility;

import static android.content.Context.TELEPHONY_SERVICE;

public class AcquisitionFragment extends KeyDwonFragment  implements  OnClickListener{

	private int RESULT_STATUS_SUCCESS=0;//成功
	private int RESULT_STATUS_CANCEL=-2;//取消
	private int RESULT_STATUS_FAILURE=-1;//失败
	private int RESULT_STATUS_NO_MATCH=-3;//指纹不匹配
	private static final String TAG = "AcquisitionFragment";
	private MainActivity mContext;
	Button btnEnroll;
	Button EnrollStop;
	Button btnPtCapture;
	Button btnPtCaptureStop;
	TextView tvInfo,  tvVersion;
	Button btnCleanAll;
	Button btnGetCount;
	Handler handler=new Handler();
	ScrollView scroll;
	byte[] buff=new byte[1];
	int id=-1;
	String oldMsg="";
	static int tId=100;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fingerprint_acquisition_fragment,
				container, false);
		return v;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = (MainActivity) getActivity();
		init();
	}

	private void init() {
		scroll=(ScrollView)getView().findViewById(R.id.scroll);
		btnEnroll = (Button) getView().findViewById(R.id.btnEnroll);
		btnCleanAll = (Button) getView().findViewById(R.id.btnCleanAll);
		btnGetCount = (Button) getView().findViewById(R.id.btnGetCount);
		EnrollStop = (Button) getView().findViewById(R.id.btnEnrollStop);
		btnPtCapture = (Button) getView().findViewById(R.id.btnPtCapture);
		btnPtCaptureStop = (Button) getView().findViewById(R.id.btnPtCaptureStop);

		tvInfo = (TextView) getView().findViewById(R.id.tvInfo);
		tvVersion=(TextView) getView().findViewById(R.id.tvVersion);
		if(mContext.isPower){
			FingerprintWithFIPS.FingerprintInfo info=mContext.mFingerprint.getPTInfo();
			tvVersion.setText("version:"+info.getVersions() +"  ID:"+info.getId());
		  }
		btnEnroll.setOnClickListener(this);
		btnCleanAll.setOnClickListener(this);
		btnGetCount.setOnClickListener(this);
		EnrollStop.setOnClickListener(this);
		btnPtCapture.setOnClickListener(this);
		btnPtCaptureStop.setOnClickListener(this);
		mContext.mFingerprint.setEnrollCallBack(new EnrollBack());
		mContext.mFingerprint.setIdentificationCallBack(new IdentificationBack());
        mContext.mFingerprint.setPtCaptureCallBack(new CaptureCallBack());
	}
	@Override
	public void onPause() {
		super.onPause();
		tvInfo.setText("");
		mContext.mFingerprint.stopEnroll();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		if(!mContext.isPower){
			Toast.makeText(mContext,"The fingerprints did not run powered on!",Toast.LENGTH_SHORT).show();
			return;
		}
		switch (view.getId()){
			case R.id.btnEnroll:
				buff=null;
				id=-1;
				tvInfo.setText("");
				mContext.mFingerprint.startIdentification();//采集之前先判断指纹是否存在
				btnEnroll.setEnabled(false);
				break;
			case R.id.btnCleanAll:
				int result = mContext.mFingerprint.deleteAllFingers();
				Toast.makeText(mContext,"CleanAll：" + result,Toast.LENGTH_SHORT).show();
				break;
			case R.id.btnGetCount:
				new GetCountTask().execute();
				break;
			case R.id.btnPtCapture:
			  	  btnPtCapture.setEnabled(false);
                  mContext.mFingerprint.startPtCapture();
				break;
			case R.id.btnEnrollStop:
                 mContext.mFingerprint.stopEnroll();
				break;
			case R.id.btnPtCaptureStop:
                 mContext.mFingerprint.stopPtCapture();
				break;
		}
	}

	public class GetCountTask extends AsyncTask<String, Integer, Boolean> {
		ProgressDialog mypDialog;
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			int Count = mContext.mFingerprint.getFingersCount();
			publishProgress(Count);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return  true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			mypDialog.cancel();

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			mypDialog = new ProgressDialog(mContext);
			mypDialog.setCancelable(false);
			mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mypDialog.setMessage("wait...");
			mypDialog.setCanceledOnTouchOutside(false);
			mypDialog.show();
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			mypDialog.setMessage("Count:"+values[0]);

		}
	}
	public void scrollToBottom() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				scroll.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}
	private class  EnrollBack implements FingerprintWithFIPS.EnrollCallBack {
		@Override
		public void messageInfo(String s) {
			setMsg(s);
		}

		@Override
		public void onComplete(boolean result, byte[] bytes, int id,int failuerCode) {
			Log.i(TAG, "failuerCode="+failuerCode);
			if (result) {
				String strMsg = "FingerprintID:" + id ;
				setMsg(strMsg);
				String fileName = "FingerprintID_" + id + ".txt";
				FileUtils.WritFile(fileName, FileUtils.bytes2HexString2(bytes, bytes.length));
				mContext.playSound(1);
			} else {
				mContext.playSound(2);
			}
			btnEnroll.setEnabled(true);
		}
	}

		private class IdentificationBack implements FingerprintWithFIPS.IdentificationCallBack {
			@Override
			public void messageInfo(String s) {
				setMsg(s);
			}

			@Override
			public void onComplete(boolean result, int id,int failuerCode) {
				Log.i(TAG, "failuerCode="+failuerCode);
				if (result) {
					setMsg("Fingerprint ID:" + id);
				} else {
					if(failuerCode==RESULT_STATUS_NO_MATCH){ //指纹不存在
						mContext.mFingerprint.startEnroll();
						return;
					}
				}
				mContext.playSound(2);
				btnEnroll.setEnabled(true);
			}

		}

		private class CaptureCallBack implements FingerprintWithFIPS.PtCaptureCallBack {
			@Override
			public void messageInfo(String s) {
				setMsg(s);
			}

			@Override
			public void onComplete(boolean result, byte[] bytes,int failuerCode) {
				Log.i(TAG, "failuerCode="+failuerCode);
				if (result) {
					//bytes=mContext.mFingerprint.ptConvertTemplateEx(FingerprintWithFIPS.DataFormat.ISO_FMR,bytes,bytes.length);
					mContext.playSound(1);
					String fileName = "FingerprintID_" + (tId++) + ".txt";
					FileUtils.WritFile(fileName, FileUtils.bytes2HexString2(bytes, bytes.length));
					setMsg(fileName);
				} else {
					mContext.playSound(2);
				}
				btnPtCapture.setEnabled(true);
			}
		}

     private void setMsg(String s) {
				if (!oldMsg.equals(s)) {
					String str1 = tvInfo.getText().toString();
					String strMsg = str1 + s + ".\r\n";
					tvInfo.setText(strMsg);
					oldMsg = s;
					scrollToBottom();
				}
			}

	}
