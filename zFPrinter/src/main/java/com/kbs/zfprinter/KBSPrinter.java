package com.kbs.zfprinter;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import zpSDK.zpSDK.zpBluetoothPrinter;

/**
 * Author by YX, Date on 2018/8/14.
 */
public class KBSPrinter extends UZModule{

    public ImageView qRImageView;
    public TextView tvCode;
    public View  printView;
    // 创建接口对象
    private zpBluetoothPrinter mZpAPI;
    private BluetoothAdapter mBluetoothAdapter;
    private Bitmap printBitmap;
    private UZModuleContext mJsCallback;
    private boolean isConnectSuccess;

    public KBSPrinter(UZWebView webView) {
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

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public void jsmethod_initPrinter(UZModuleContext moduleContext){
        init(moduleContext);
    }

    public void jsmethod_goConnectPrinter(UZModuleContext moduleContext){
        goConnectPrinter(moduleContext);
    }

    public void jsmethod_goPrinter(UZModuleContext moduleContext){
            mJsCallback = moduleContext;
            if(isNetworkConnected(context())) {
                Glide.with(context())
                        .load(moduleContext.optString("imgUrl"))
                        .asBitmap()
                        .centerCrop()
                        .into(target);
            }else{
                Toast.makeText(context(), "当前网络未连接,请检查网络故障！", Toast.LENGTH_SHORT).show();
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
        printView = LayoutInflater.from(context()).inflate(R.layout.printer_img,null);
        qRImageView = ((ImageView) printView.findViewById(R.id.iv_img));
        tvCode = ((TextView)  printView.findViewById(R.id.tv_code));
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

    private SimpleTarget target = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
            //这里我们拿到回调回来的bitmap
            qRImageView.setImageBitmap(bitmap);
            tvCode.setText(mJsCallback.optString("tvCode"));
            printBitmap = getBitmapFromView(printView);
            goDrawQrImg( mJsCallback.optInt("Xaxis"), mJsCallback.optInt("Yaxis"), mJsCallback.optInt("Width"), mJsCallback.optInt("Height"));
        }
    };

    //打印操作
    private void goDrawQrImg(int Xaxis,int Yaxis,int Width,int Height) {
        JSONObject ret = new JSONObject();
        boolean drawBitmap = mZpAPI.drawBitmap(printBitmap,Xaxis,Yaxis,Width,Height,0,1);
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
