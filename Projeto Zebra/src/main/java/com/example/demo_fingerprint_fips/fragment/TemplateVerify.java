package com.example.demo_fingerprint_fips.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demo_fingerprint_fips.FileUtils;
import com.example.demo_fingerprint_fips.MainActivity;
import com.example.demo_fingerprint_fips.R;
import com.rscja.deviceapi.FingerprintWithFIPS;
import com.rscja.utility.StringUtility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TemplateVerify extends Fragment implements View.OnClickListener{
    private MainActivity mContext;

    private static final String TAG = "TemplateVerify";
     Button btnIdent;
     Button btnStop;
     ListView lsTemplate;

     TextView tvTip ;
     TextView tvInfoMsg ;
    ScrollView scroll2;
     private HashMap<String,String> hashMap;
    String oldMsg="";
    String path;
    Handler handler=new Handler();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        View v = inflater.inflate(R.layout.fragment_template_verify,
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
        mContext.mFingerprint.stopTemplateVerify();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG,"onResume");

    }
    private void init() {

        tvTip= (TextView) getView().findViewById(R.id.tvTip);
        tvInfoMsg= (TextView) getView().findViewById(R.id.tvInfoMsg);
        btnIdent = (Button) getView().findViewById(R.id.btnIdent);
        btnStop= (Button) getView().findViewById(R.id.btnStop);
        scroll2=(ScrollView)getView().findViewById(R.id.scroll2);
        lsTemplate = (ListView) getView().findViewById(R.id.lsTemplate);
        btnStop.setOnClickListener(this);
        btnIdent.setOnClickListener(this);
        tvTip.setText(FileUtils.PATH);
        lsTemplate.setVerticalScrollBarEnabled(true);
        initListTemplate();

        lsTemplate.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                //获得选中项的HashMap对象
                String key=(String)lsTemplate.getItemAtPosition(arg2);
                path=hashMap.get(key);

            }});

        mContext.mFingerprint.setTemplateVerifyCallBack(new TemplateVerifyCall());
    }

    private void initListTemplate(){
        hashMap= FileUtils.ReadFileName();
        List<String> data = new ArrayList<String>();
        Iterator iter = hashMap.entrySet().iterator();
        while (iter.hasNext()) {
               Map.Entry entry = (Map.Entry) iter.next();
               Object key = entry.getKey();
               data.add(key.toString());
           }
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(getActivity(),R.layout.listitem,R.id.list_item,data);
        lsTemplate.setAdapter(adapter);
    }

    public void scrollToBottom() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                scroll2.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnIdent:
                if(!mContext.isPower){
                    Toast.makeText(mContext,"The fingerprints did not run on!",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(path==null || path.length()<0)
                    return;
                String temp=FileUtils.ReadFile(path);
                if(!temp.isEmpty()) {
                    byte[] templateData = StringUtility.hexStringToBytes(temp);
                    if (templateData != null && templateData.length > 0) {
                        char[] template_data=new char[templateData.length];
                        for(int k=0;k<templateData.length;k++){
                            template_data[k]=(char)templateData[k];
                        }
                        mContext.mFingerprint.startTemplateVerify(template_data);
                        btnIdent.setEnabled(false);
                    }
                }
                break;
            case  R.id.btnStop:
                mContext.mFingerprint.stopTemplateVerify();
                break;
        }
    }

    class TemplateVerifyCall implements  FingerprintWithFIPS.TemplateVerifyCallBack{
        @Override
        public void messageInfo(String s) {
            if(!oldMsg.equals(s)) {
                String str1=tvInfoMsg.getText().toString();
                String strMsg=str1+s+".\r\n";
                tvInfoMsg.setText(strMsg);
                oldMsg=s;
                scrollToBottom();
            }
        }

        @Override
        public void onComplete(boolean result,int failuerCode) {
            Log.i(TAG, "failuerCode="+failuerCode);
            btnIdent.setEnabled(true);
            if(result){
                mContext.playSound(1);
            }else{
                mContext.playSound(2);
            }
        }


    }
}
