package com.logan19gp.search;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.logan19gp.search.Utils.DbHelper;
import com.logan19gp.search.Utils.FileUtil;

public class UpdateProdService extends Service {
    private static final String TAG = "UpdateProdService";
    private IBinder mBinder;
    private boolean mAllowRebind;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        DbHelper.DatabaseManager.initializeInstance(this);
        HandlerThread mHandlerThread = new HandlerThread("LocalServiceThread");
        mHandlerThread.start();
        Handler mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    FileUtil.readAssetWriteDB(getApplication(), "items.csv");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreateService");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroyService");
    }
}
