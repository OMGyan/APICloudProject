package com.apicloud.moduleDemo;

import android.app.Activity;
import android.content.Intent;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 该类映射至Javascript中moduleDemo对象<br><br>
 * <strong>Js Example:</strong><br>
 * var module = api.require('moduleDemo');<br>
 * module.xxx();
 * @author APICloud
 *
 */
public class APIModuleDemo extends UZModule {

	static final int ACTIVITY_REQUEST_CODE_A = 100;
	private UZModuleContext mJsCallback;

	public APIModuleDemo(UZWebView webView) {
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
