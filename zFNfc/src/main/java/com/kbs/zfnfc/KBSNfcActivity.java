package com.kbs.zfnfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author by YX, Date on 2018/8/8.
 */
public class KBSNfcActivity extends Activity{

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private JSONObject json;
    private Intent resultData;
    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mo_modulenfc_main_activity);
        json = new JSONObject();
        resultData = new Intent();
        //获取NfcAdapter实例
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //获取通知
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (nfcAdapter == null) {
            Toast.makeText(this,"对不起！您的设备不支持NFC",Toast.LENGTH_LONG).show();
            goReturnStatus("",1);
        }
        if (nfcAdapter!=null&&!nfcAdapter.isEnabled()) {
            Toast.makeText(this,"请您在系统设置中开启NFC功能",Toast.LENGTH_LONG).show();
            goReturnStatus("",2);
        }
        //因为启动模式是singleTop，于是会调用onNewIntent方法
        onNewIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
    }

    void resolveIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            processTag(intent);
        }
    }

    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
                "B", "C", "D", "E", "F" };
        String out = "";
        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    private String flipHexStr(String s){
        StringBuilder  result = new StringBuilder();
        for (int i = 0; i <=s.length()-2; i=i+2) {
            result.append(new StringBuilder(s.substring(i,i+2)));
        }
        return result.toString();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public void processTag(Intent intent) {//处理tag
        //获取到卡对象
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        //获取卡id这里即uid
        byte[] aa = tagFromIntent.getId();
        String str = ByteArrayToHexString(aa);
        str = flipHexStr(str);

        goReturnStatus(str,3);
    }

    private void goReturnStatus(String str,int code) {
        try {
            json.put("id", str);
            json.put("code", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        resultData.putExtra("result", json.toString());
        setResult(RESULT_OK, resultData);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            //设置程序不优先处理
            nfcAdapter.disableForegroundDispatch(this);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            //设置程序优先处理
            nfcAdapter.enableForegroundDispatch(this, pendingIntent,
                    null,null);
    }
}

