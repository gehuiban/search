package com.logan19gp.search.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.logan19gp.search.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {
    /**
     * Reads the text of an asset. Should not be run on the UI thread.
     *
     * @param path The path to the asset.
     */
    public static void readAssetWriteDB(Context context, String path) {
        AssetManager mgr = context.getAssets();
        int id = 0;
        Long startTime = System.currentTimeMillis();
        int sizeInsert = 92;
        String values = "";
        boolean isFirst = true;
        InputStream is = null;
        BufferedReader reader = null;
        if (!DbHelper.DatabaseManager.isInitialized()) {
            DbHelper.DatabaseManager.initializeInstance(context);
        }
        SQLiteDatabase db = DbHelper.DatabaseManager.getInstance().openDatabaseWR();
        try {
            is = mgr.open(path);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                values = values + (isFirst ? "" : ", ") + "(\'" + line.replace("\'", "").replace(",", "\',\'") + "\')";
                id++;
                isFirst = false;
                if (id % sizeInsert > sizeInsert - 2) {
                    isFirst = true;
                    DbHelper.saveToDbGameFromServer(db, values);
                    values = "";
                    if (id < 150) {
                        Intent scheduledIntent = new Intent(MainActivity.EVENT_UPDATE);
                        scheduledIntent.putExtra(MainActivity.EVENT_UPDATE, true);
                        context.sendBroadcast(scheduledIntent);
                    }
                }
            }
            if (values.length() > 0) {
                DbHelper.saveToDbGameFromServer(db, values);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
            Long total = System.currentTimeMillis() - startTime;
            Log.d("UPDATE_FILE", "Total time:" + total);

            Intent scheduledIntent = new Intent(MainActivity.EVENT_FINISH_UPDATE);
            scheduledIntent.putExtra(MainActivity.EVENT_FINISH_UPDATE, true);
            context.sendBroadcast(scheduledIntent);
        }
    }


}
