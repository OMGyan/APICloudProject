package com.kbs.zfuniversalprinter;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import zpSDK.zpSDK.zpBluetoothPrinter;

/**
 * Author by YX, Date on 2018/8/20.
 */
public class KBSUniversalPrinter extends UZModule{
    public RelativeLayout  printView;
    private zpBluetoothPrinter mZpAPI;
    private BluetoothAdapter mBluetoothAdapter;
    private UZModuleContext mJsCallback;
    private boolean isConnectSuccess;
    private List<ImageView> imageViewList = new ArrayList<>();
    private List<String> urlList = new ArrayList<>();
    private int Xint = 0;

    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bitmap printBitmap  = getBitmapFromView(printView);
            goDrawQrImg(printBitmap,printBitmap.getWidth(), printBitmap.getHeight());
        }
    };
    public KBSUniversalPrinter(UZWebView webView) {
        super(webView);
    }

    private void goConnectPrinter(UZModuleContext moduleContext) {
        JSONObject ret = new JSONObject();
        if(mZpAPI.getAllPrinters().size()==0){
            isConnectSuccess = false;
            try {
                ret.put("code",1);
                ret.put("isConnectSuccess",isConnectSuccess);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            moduleContext.success(ret, true);
            moduleContext = null;
        }else {
            if (isSupported(moduleContext.optString("printerName"))) {
                //开启打印机,传入打印机MAC地址
                if (mZpAPI.openPrinterSync(moduleContext.optString("printerMac"))) {
                    isConnectSuccess = true;
                    try {
                        ret.put("isConnectSuccess",isConnectSuccess);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    moduleContext.success(ret, true);
                    moduleContext = null;
                } else {
                    try {
                        ret.put("code",2);
                        ret.put("isConnectSuccess",isConnectSuccess);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    moduleContext.success(ret, true);
                    moduleContext = null;
                }
            }else {
                Toast.makeText(context(), "打印机名称不是接口所支持的打印机", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void jsmethod_initPrinter(UZModuleContext moduleContext){
        init(moduleContext);
    }

    public void jsmethod_goConnectPrinter(UZModuleContext moduleContext){
        goConnectPrinter(moduleContext);
    }
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void jsmethod_goPrinter(UZModuleContext moduleContext){
        imageViewList.clear();
        urlList.clear();
        mJsCallback = moduleContext;
        JSONObject printViewClass  = moduleContext.optJSONObject("printView");
         printView = new RelativeLayout(context());
            try {
                JSONArray viewClassJSONArray = printViewClass.getJSONArray("list");
                RelativeLayout.LayoutParams printViewLayoutParams = new RelativeLayout.LayoutParams(printViewClass.getInt("width"), printViewClass.getInt("height"));
                printView.setLayoutParams(printViewLayoutParams);
                for (int i = 0; i < viewClassJSONArray.length(); i++) {
                    JSONObject classJSONArrayJSONObject = viewClassJSONArray.getJSONObject(i);
                    if("image".equals(classJSONArrayJSONObject.getString("type"))){
                        ImageView qRImageView = new ImageView(context());
                        int width = classJSONArrayJSONObject.getInt("width");
                        int height = classJSONArrayJSONObject.getInt("height");
                        int x = classJSONArrayJSONObject.getInt("x");
                        int y = classJSONArrayJSONObject.getInt("y");
                        RelativeLayout.LayoutParams qRImageViewParams = new RelativeLayout.LayoutParams(width,height);
                        qRImageView.setLayoutParams(qRImageViewParams);
                        qRImageView.setX(x);
                        qRImageView.setY(y);
                        imageViewList.add(qRImageView);
                        urlList.add(classJSONArrayJSONObject.getString("url"));
                        printView.addView(qRImageView);
                    }else {
                        int x = classJSONArrayJSONObject.getInt("x");
                        int y = classJSONArrayJSONObject.getInt("y");
                        TextView tvCode = new TextView(context());
                        RelativeLayout.LayoutParams tvCodeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                        tvCode.setLayoutParams(tvCodeParams);
                        tvCode.setY(y);
                        tvCode.setX(x);
                        tvCode.setTextColor(context().getResources().getColor(R.color.color_black));
                        tvCode.setTextSize(classJSONArrayJSONObject.getInt("size"));
                        tvCode.setText(classJSONArrayJSONObject.getString("info"));
                        printView.addView(tvCode);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if(urlList.size()!=0){
            if(isNetworkConnected(context())) {
                for (int i = 0; i < urlList.size(); i++) {
                    Xint = i;
                    Glide.with(context())
                            .load(urlList.get(i))
                            .asBitmap()
                            .centerCrop()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                    imageViewList.get(Xint).setImageBitmap(bitmap);
                                    if(urlList.size() == 1){
                                        Bitmap printBitmap  = getBitmapFromView(printView);
                                        goDrawQrImg(printBitmap,printBitmap.getWidth(), printBitmap.getHeight());
                                    }
                                }
                            });
                }
                if(urlList.size()>1){
                    mhandler.sendEmptyMessage(1);
                }
            }else{
                Toast.makeText(context(), "当前网络未连接,请检查网络故障！", Toast.LENGTH_SHORT).show();
            }
        }else {
            Bitmap printBitmap  = getBitmapFromView(printView);
            goDrawQrImg(printBitmap,printBitmap.getWidth(), printBitmap.getHeight());
        }


    }

    //网络连接检测
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }



    private void init(UZModuleContext moduleContext) {

        JSONObject ret = new JSONObject();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            try {
                ret.put("code",1);
                ret.put("isInit",false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            moduleContext.success(ret, true);
            moduleContext = null;
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            try {
                ret.put("code",2);
                ret.put("isInit",false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            moduleContext.success(ret, true);
            moduleContext = null;
            return;
        }

        //打印控件不为null,进行实例化
        if (mZpAPI == null) {
            mZpAPI = new zpBluetoothPrinter(context());
        }

        try {
            ArrayList<zpBluetoothPrinter.print> allPrinters = mZpAPI.getAllPrinters();
            ArrayList<JSONObject> objectArrayList = new ArrayList<>();
            if(allPrinters.size()>0){
                for (int i = 0; i < allPrinters.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("mac",allPrinters.get(i).Getmac());
                    jsonObject.put("name",allPrinters.get(i).GetName());
                    objectArrayList.add(jsonObject);
                }
                ret.put("printerList",objectArrayList);
            }else {
                ret.put("printerList",JSONObject.NULL);
            }
            ret.put("isInit",true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        moduleContext.success(ret, true);
        moduleContext = null;
    }


    //打印操作
    private void goDrawQrImg(Bitmap printBitmap, int Width, int Height) {
        JSONObject ret = new JSONObject();

        boolean drawBitmap = mZpAPI.drawBitmap(printBitmap,0,0,Width,Height,0,1);
        if (drawBitmap) {
            try {
                ret.put("isPrintSuccess",true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mJsCallback.success(ret, true);
            mJsCallback = null;
        } else {
            try {
                ret.put("isPrintSuccess",false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mJsCallback.success(ret, true);
            mJsCallback = null;
        }
    }


    /**
     * 判断给定的打印机名称是否是接口所支持的打印机,防止非SDK支持打印机调用出错
     *
     * @param printerName 打印机名称
     * @return 是否支持
     */
    public boolean isSupported(String printerName) {
        return Pattern.compile("^B3" + "_\\d{4}[L]?$").matcher(printerName).matches();
    }

    //将View转成Bitmap
    public Bitmap getBitmapFromView(View currentView){
        currentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        currentView.layout(0, 0, currentView.getMeasuredWidth(), currentView.getMeasuredHeight());
        currentView.buildDrawingCache();
        Bitmap bitmap = currentView.getDrawingCache();
        return bitmap;
    }

}
