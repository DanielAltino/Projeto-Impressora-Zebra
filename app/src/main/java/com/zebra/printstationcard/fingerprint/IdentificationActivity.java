package com.zebra.printstationcard.fingerprint;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.rscja.deviceapi.FingerprintWithFIPS;
import com.zebra.printstationcard.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class IdentificationActivity extends AppCompatActivity {

    private final static String TAG = "IdentificationActivity";
    public FingerprintWithFIPS mFingerprint;
    private FragmentTabHost mTabHost;
    private FragmentManager fm;
    public  boolean   isPower=true;


    public String infoCliente;
    private ArrayList<String> Arquivos = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_identification);
        initSound();
        initViewPageData();
        try {

            mFingerprint = FingerprintWithFIPS.getInstance();

        } catch (Exception ex) {
            Toast.makeText(IdentificationActivity.this, ex.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

    }


    protected void initViewPageData() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        fm = getSupportFragmentManager();
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, fm, R.id.realtabcontent);

        /*mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.fingerprint_tab_identification)).setIndicator(getString(R.string.fingerprint_tab_identification)),
                IdentificationFragment.class, null);

       mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.fingerprint_tab_verify)).setIndicator(getString(R.string.fingerprint_tab_verify)),
              TemplateVerify.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.fingerprint_tab_acquisition)).setIndicator(getString(R.string.fingerprint_tab_acquisition)),
                AcquisitionFragment.class, null);
         */
        mTabHost.addTab(mTabHost.newTabSpec("").setIndicator(""),
                IdentificationFragment.class, null);

        boolean result;
        File file=new File(FileUtils.PATH);
        if(!file.exists()){
            result = file.mkdirs();
        }
    }



    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d(TAG,"onPause");
        if (mFingerprint != null) {
            mFingerprint.free();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG,"onResume");
        new InitTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public void init(){
        mFingerprint.free();
        new InitTask().execute();
    }
    /**
     * 设备上电异步类
     *
     * @author liuruifeng
     */
    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            return mFingerprint.init();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            mypDialog.cancel();

            if (!result) {
                isPower=false;
                Toast.makeText(IdentificationActivity.this, "init fail",
                        Toast.LENGTH_SHORT).show();
            }else{
                isPower=true;
                Toast.makeText(IdentificationActivity.this, "init success", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            mypDialog = new ProgressDialog(IdentificationActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

    }



    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;
    private void initSound(){
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
        soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);// 实例化AudioManager对象
    }
    /**
     * 播放提示音
     *
     * @param id 成功1，失败2
     */
    public void playSound(int id) {

        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 返回当前AudioManager对象的最大音量值
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 返回当前AudioManager对象的音量值
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;
        try {
            soundPool.play(soundMap.get(id), volumnRatio, // 左声道音量
                    volumnRatio, // 右声道音量
                    1, // 优先级，0为最低
                    0, // 循环次数，0无不循环，-1无永远循环
                    1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
            );
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }


}
