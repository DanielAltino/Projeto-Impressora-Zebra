package com.zebra.printstationcard.fingerprint;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.rscja.deviceapi.FingerprintWithFIPS;
import com.zebra.printstationcard.R;
import com.zebra.printstationcard.util.UIHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * FIPS指纹模块使用demo
 * <p>
 * 1、使用前请确认您的机器已安装此模块。
 * 2、要正常使用模块需要在\libs\armeabi\目录放置libDeviceAPI.so文件，同时在\libs\目录下放置DeviceAPI.jar文件。
 * 3、在操作设备前需要调用 init()打开设备，使用完后调用 free() 关闭设备
 * <p>
 * <p>
 * 更多函数的使用方法请查看API说明文档
 *
 * @author liuruifeng
 */
public class EnrollActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "MainActivity111";
    public FingerprintWithFIPS mFingerprint;
    private FragmentTabHost mTabHost;
    private FragmentManager fm;
    public boolean isPower = true;

    private int RESULT_STATUS_SUCCESS = 0;//成功
    private int RESULT_STATUS_CANCEL = -2;//取消
    private int RESULT_STATUS_FAILURE = -1;//失败
    private int RESULT_STATUS_NO_MATCH = -3;//指纹不匹配
    Button btnEnroll;
    Button EnrollStop;
    TextView tvInfo, tvVersion;
    Button btnCleanAll;
    Button btnGoToMain;
    Handler handler = new Handler();
    ScrollView scroll;
    byte[] buff = new byte[1];
    int id = -1;
    String oldMsg = "";
    static int tId = 100;


    public String infoCliente;

    private ArrayList<String> Arquivos = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_enroll);

        UIHelper.setLogoOnActionBar(this);

        initSound();
        initViewPageData();
        try {

            mFingerprint = FingerprintWithFIPS.getInstance();

        } catch (Exception ex) {
            Toast.makeText(EnrollActivity.this, ex.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return;
        }


    }


    protected void initViewPageData() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        scroll = (ScrollView) findViewById(R.id.scroll);
        btnEnroll = (Button) findViewById(R.id.btnEnroll);
        btnCleanAll = (Button) findViewById(R.id.btnCleanAll);
        EnrollStop = (Button) findViewById(R.id.btnEnrollStop);
        btnGoToMain = (Button) findViewById(R.id.btnGoToMain);

        tvInfo = (TextView) findViewById(R.id.tvInfo);
        tvVersion = (TextView) findViewById(R.id.tvVersion);

        btnEnroll.setOnClickListener(this);
        btnCleanAll.setOnClickListener(this);
        EnrollStop.setOnClickListener(this);
        btnGoToMain.setOnClickListener(this);

        //btnCleanAll.setVisibility(View.GONE);
        btnGoToMain.setVisibility(View.GONE);

        /*fm = getSupportFragmentManager();
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, fm, R.id.realtabcontent);

        /*mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.fingerprint_tab_identification)).setIndicator(getString(R.string.fingerprint_tab_identification)),
                IdentificationFragment.class, null);

       mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.fingerprint_tab_verify)).setIndicator(getString(R.string.fingerprint_tab_verify)),
              TemplateVerify.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.fingerprint_tab_acquisition)).setIndicator(getString(R.string.fingerprint_tab_acquisition)),
                AcquisitionFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("GRAB").setIndicator("CAPTURAR DIGITAL"),
                GRABFragment.class, null);
 */
        boolean result;
        File file = new File(FileUtils.PATH);
        if (!file.exists()) {
            result = file.mkdirs();
        }

        Listar();
    }

    @Override
    public void onClick(View view) {
        if (!isPower) {
            Toast.makeText(EnrollActivity.this, "The fingerprints did not run powered on!", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (view.getId()) {
            case R.id.btnEnroll:
                mFingerprint.setEnrollCallBack(new EnrollBack());
                mFingerprint.setIdentificationCallBack(new IdentificationBack());
                //Toast.makeText(EnrollActivity.this, "The FINGERPRINT ACTIVITY!", Toast.LENGTH_SHORT).show();
                buff = null;
                id = -1;
                tvInfo.setText("");
                mFingerprint.startIdentification();//采集之前先判断指纹是否存在
                btnEnroll.setEnabled(false);
                break;
            case R.id.btnCleanAll:
                int result = mFingerprint.deleteAllFingers();
                //Toast.makeText(EnrollActivity.this, "CleanAll：" + result, Toast.LENGTH_SHORT).show();
                cleanFile();
                break;
            case R.id.btnEnrollStop:
                mFingerprint.stopEnroll();
                break;
            case R.id.btnGoToMain:
                //Intent cameraIntent = new Intent(FirstActivity.this, CameraActivity.class);
                //startActivity(cameraIntent);
                Intent mainIntent = new Intent(EnrollActivity.this, FirstActivity.class);
                startActivity(mainIntent);
                break;
        }
    }

    public class GetCountTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            int Count = mFingerprint.getFingersCount();
            publishProgress(Count);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.d(TAG, "onPause");
        if (mFingerprint != null) {
            mFingerprint.free();
            tvInfo.setText("");
            mFingerprint.stopEnroll();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Log.d(TAG, "onResume");
        new InitTask().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void init() {
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
                isPower = false;
                Toast.makeText(EnrollActivity.this, "Inicialização do leitor biométrico falhou",
                        Toast.LENGTH_SHORT).show();
            } else {
                isPower = true;
                Toast.makeText(EnrollActivity.this, "Leitor biométrico foi inicializado com sucesso!", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            mypDialog = new ProgressDialog(EnrollActivity.this);
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("Inicializando leitor biométrico, aguarde...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();
        }

    }


    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;

    private void initSound() {
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

    private class EnrollBack implements FingerprintWithFIPS.EnrollCallBack {
        @Override
        public void messageInfo(String s) {
            setMsg(s);
        }

        @Override
        public void onComplete(boolean result, byte[] bytes, int id, int failuerCode) {
            Log.i(TAG, "failuerCode=" + failuerCode);
            if (result) {
                String strMsg = "Biometria salva, com o ID: " + id;
                btnGoToMain.setVisibility(View.VISIBLE);
                setMsg(strMsg);
                String fileName = "FingerprintID_" + id + ".txt";
                FileUtils.WritFile(fileName, FileUtils.bytes2HexString2(bytes, bytes.length));
                playSound(1);

                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    infoCliente = "ID:" + id + "£" + extras.getString("Dados");
                }

                click_Salvar();
            } else {
                playSound(2);
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
        public void onComplete(boolean result, int id, int failuerCode) {
            Log.i(TAG, "failuerCode=" + failuerCode);
            if (result) {
                setMsg("Biometria já existe, com o ID: " + id);
                btnGoToMain.setVisibility(View.VISIBLE);
            } else {
                if (failuerCode == RESULT_STATUS_NO_MATCH) { //指纹不存在
                    mFingerprint.startEnroll();
                    return;
                }
            }
            playSound(2);
            btnEnroll.setEnabled(true);
        }

    }

    private class CaptureCallBack implements FingerprintWithFIPS.PtCaptureCallBack {
        @Override
        public void messageInfo(String s) {
            setMsg(s);
        }

        @Override
        public void onComplete(boolean result, byte[] bytes, int failuerCode) {
            Log.i(TAG, "failuerCode=" + failuerCode);
            if (result) {
                //bytes=mContext.mFingerprint.ptConvertTemplateEx(FingerprintWithFIPS.DataFormat.ISO_FMR,bytes,bytes.length);
                playSound(1);
                String fileName = "FingerprintID_" + (tId++) + ".txt";
                FileUtils.WritFile(fileName, FileUtils.bytes2HexString2(bytes, bytes.length));
                setMsg(fileName);


            } else {
                playSound(2);
            }
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

    public void scrollToBottom() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
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

    public void click_Salvar() {
        String lstrNomeArq;
        File arq;
        byte[] dados;
        try {
            lstrNomeArq = "arquivo.txt";

            arq = new File(Environment.getExternalStorageDirectory(), lstrNomeArq);

            FileOutputStream fos;


            dados = infoCliente.getBytes();

            fos = new FileOutputStream(arq, true);
            fos.write(dados);
            fos.flush();
            fos.close();
            Mensagem("Dados salvos com sucesso!");
            Listar();
        } catch (Exception e) {
            Mensagem("Error: " + e.getMessage());
        }
    }

    private void Mensagem(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }


    public void click_Carregar(View v)
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
                Toast.makeText(this, lstrlinha, Toast.LENGTH_SHORT).show();
            }


            Mensagem("Dados carregados com sucesso!");

        }
        catch (Exception e)
        {
            Mensagem("Error: " + e.getMessage());
        }
    }

    public void cleanFile() {
        String lstrNomeArq;
        File arq;
        try {
            lstrNomeArq = "arquivo.txt";

            arq = new File(Environment.getExternalStorageDirectory(), lstrNomeArq);

            FileOutputStream fos;


            fos = new FileOutputStream(arq);
            fos.write("".getBytes());
            fos.flush();
            fos.close();
            Mensagem("Todos os dados foram deletados!");
            Listar();
        } catch (Exception e) {
            Mensagem("Error: " + e.getMessage());
            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
    }

}

