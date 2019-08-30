package com.example.demo_fingerprint_fips;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017-5-22.
 */

public class FileUtils {

    public final static String  PATH = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "fingerprint_fips"
            + File.separator
            +"Template"
            + File.separator;

    public  static void WritFile(String fileName,String data){
        if(data.isEmpty())
            return;
        String filePath= PATH+fileName;
        File file =new File(filePath);
        FileOutputStream fileOutputStream = null;
        if (!file.exists()) {
            try{
                file.createNewFile();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("chmod 0666 "+file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        try{
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (Exception e) {
            }
        }
    }

    public static String  ReadFile(String fileName){
        String filePath= PATH+fileName;
        File file = new File(fileName);
        String data="";

        if(!file.exists())  return null;

        FileInputStream fis = null;
        InputStreamReader inputreader = null;
        BufferedReader buffreader = null;
        try{
            fis = new FileInputStream(file);
            inputreader = new InputStreamReader(fis);
            buffreader = new BufferedReader(inputreader);
            data = buffreader.readLine();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                inputreader.close();
                buffreader.close();
            } catch (Exception e) {
            }
        }
        return data;
    }

    public static HashMap<String,String> ReadFileName(){
        HashMap<String,String> map =new HashMap<String,String>();
        File file = new File(PATH);
        if (file.exists()) {
            File[] files = file.listFiles();
            for(int k=0;k<files.length;k++){
             ///   String str1=files[k].getAbsolutePath();
              //  String str2=files[k].getPath();
                map.put(files[k].getName(),files[k].getAbsolutePath());
            }
            return  map;
        }
        return  null;
    }
    /**
     * byte类型数组转十六进制字符串
     *
     * @param b
     *            byte类型数组
     * @param size
     *            数组长度
     * @return 十六进制字符串
     */
    public static String bytes2HexString(byte[] b, int size) {
        String ret = "";

        try {
            for (int i = 0; i < size; i++) {
                String hex = Integer.toHexString(b[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = "0" + hex;
                }
             //   ret += hex.toUpperCase();
                ret = ret+("0x"+hex.toUpperCase()+",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static String bytes2HexString2(byte[] b, int size) {
        StringBuilder stringBuilder=new StringBuilder();

        try {
            for (int i = 0; i < size; i++) {
                String hex = Integer.toHexString(b[i] & 0xFF);
                if (hex.length() == 1) {
                    stringBuilder.append("0");
                 //   hex = "0" + hex;
                }
                stringBuilder.append(hex);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
