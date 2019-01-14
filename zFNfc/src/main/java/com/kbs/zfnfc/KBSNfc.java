package com.kbs.zfnfc;

import android.app.Activity;
import android.content.Intent;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author by YX, Date on 2018/8/8.
 */
public class KBSNfc extends UZModule{

    static final int ACTIVITY_REQUEST_CODE_A = 100;
    private UZModuleContext mJsCallback;

    public KBSNfc(UZWebView webView) {
        super(webView);
    }

    public void jsmethod_getNfcIdCode(UZModuleContext moduleContext){
        mJsCallback = moduleContext;
        Intent intent = new Intent(context(),KBSNfcActivity.class);
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == ACTIVITY_REQUEST_CODE_A){
            String result = data.getStringExtra("result");
            if(null != result && null != mJsCallback){
                try {
                    JSONObject ret = new JSONObject(result);
                    mJsCallback.success(ret, true);
                    mJsCallback = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
