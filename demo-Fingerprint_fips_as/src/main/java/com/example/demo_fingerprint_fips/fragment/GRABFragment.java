package com.example.demo_fingerprint_fips.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.demo_fingerprint_fips.MainActivity;
import com.example.demo_fingerprint_fips.R;
import com.rscja.deviceapi.FingerprintWithFIPS;

import java.io.File;
import java.util.Arrays;


public class GRABFragment extends Fragment implements View.OnClickListener{
    String TAG="GRABFragment";
    public final static String  PATH = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "fingerprint_fips"
            + File.separator;
    String filePath = PATH + "finger.bmp";
    MainActivity  mContext;
    Button btnGRAB,Stop;
    ImageView iv;
    TextView tvInfo;
    TextView tvPro;
    ScrollView scroll;
    String oldMsg="";
    Handler handler=new Handler();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grab, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContext = (MainActivity) getActivity();
        btnGRAB=(Button)mContext.findViewById(R.id.btnGRAB);
        Stop =(Button) mContext.findViewById(R.id.Stop);
        btnGRAB.setOnClickListener(this);
        Stop.setOnClickListener(this);
        iv= (ImageView)mContext.findViewById(R.id.iv);
        tvInfo=(TextView)mContext.findViewById(R.id.tvInfo);
        tvPro=(TextView)mContext.findViewById(R.id.tvPro);
        scroll=(ScrollView)mContext.findViewById(R.id.scroll);
        mContext.mFingerprint.setGrabCallBack(new GRABCall());
    }
    @Override
    public void onPause() {
        super.onPause();
        mContext.mFingerprint.stopGRAB();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnGRAB:
                mContext.mFingerprint.startGRAB();
                btnGRAB.setEnabled(false);
                break;
            case R.id.Stop:
                mContext.mFingerprint.stopGRAB();
                break;
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

    class GRABCall implements FingerprintWithFIPS.GRABCallBack{
        @Override
        public void messageInfo(String s) {
            if(!oldMsg.equals(s)) {
                String str1=tvInfo.getText().toString();
                String strMsg=str1+s+".\r\n";
                tvInfo.setText(strMsg);
                oldMsg=s;
                scrollToBottom();
            }
        }

        @Override
        public void onComplete(boolean result, byte[] bytes,int failuerCode) {
            Log.i(TAG, "failuerCode="+failuerCode+"  result="+result);
          if(result) {
              if (mContext.mFingerprint.generateImg(bytes, filePath)) {
                  Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                  if (bitmap != null) {
                      iv.setImageBitmap(bitmap);
                      mContext.playSound(1);
                  }
              }
          }else{
              mContext.playSound(2);
          }
            btnGRAB.setEnabled(true);
        }

        @Override
        public void progress(int i) {
            tvPro.setText(i+"%");
        }


    }
}
