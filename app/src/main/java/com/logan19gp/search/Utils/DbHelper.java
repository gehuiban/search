package com.logan19gp.search.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String CREATE_TABLE_IF_NOT = "CREATE TABLE IF NOT EXISTS";
    public static final String DATABASE_NAME = "products.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ", ";

    public static final String PRODUCTS_TABLE_NAME = "products";
    public static final String ID = "id";
    public static final String UPC = "upc";
    public static final String DESCRIPTION = "description";
    public static final String MANUFACTURER = "manufacturer";
    public static final String BRAND = "brand";

    private static final String PRODUCTS_TABLE_CREATE =
            CREATE_TABLE_IF_NOT + " " + PRODUCTS_TABLE_NAME + " (" +
                    ID + " TEXT PRIMARY KEY" + COMMA_SEP +
                    UPC + TEXT_TYPE + COMMA_SEP +
                    DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    MANUFACTURER + TEXT_TYPE + COMMA_SEP +
                    BRAND + TEXT_TYPE + ");";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.w("createTable", PRODUCTS_TABLE_CREATE);
        sqLiteDatabase.execSQL(PRODUCTS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public static class DatabaseManager {
        private static DatabaseManager instance;
        private static DbHelper mDatabaseHelper;
        private AtomicInteger mOpenCounter = new AtomicInteger();
        private SQLiteDatabase mDatabase;
        private static Context mContext;

        public static synchronized void initializeInstance(Context context) {
            mContext = context;
            getInstance();
        }

        public static Boolean isInitialized() {
            getInstance().openDatabaseRD();
            return instance != null;
        }

        public static synchronized DatabaseManager getInstance() {
            if (instance == null) {
                instance = new DatabaseManager();
                mDatabaseHelper = mDatabaseHelper == null ? new DbHelper(mContext) : mDatabaseHelper;
            }

            return instance;
        }

        public synchronized SQLiteDatabase openDatabaseRD() {
            if (mOpenCounter.incrementAndGet() == 1) {
                mDatabase = mDatabaseHelper.getReadableDatabase();
            }
            return mDatabase;
        }

        public synchronized SQLiteDatabase openDatabaseWR() {
            if (mOpenCounter.incrementAndGet() == 1) {
                mDatabase = mDatabaseHelper.getWritableDatabase();
            }
            return mDatabase;
        }

        public synchronized void closeDatabase() {
            if (mOpenCounter.decrementAndGet() == 0) {
                mDatabase.close();

            }
        }
    }

    private static Cursor getCursor(String selectQuery) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabaseRD();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            Log.d("cursor size:", "" + (cursor == null ? "null" : cursor.getCount()));
        } catch (Exception ex) {
            Log.e("cursor issue ", ex.toString());
            ex.printStackTrace();
        }
        return cursor;
    }

    public static void saveToDbGameFromServer(SQLiteDatabase db, String products) {
        try {
            String selectQuery = "INSERT INTO " + PRODUCTS_TABLE_NAME + " (" +
                    ID + COMMA_SEP + UPC + COMMA_SEP + DESCRIPTION + COMMA_SEP +
                    MANUFACTURER + COMMA_SEP + BRAND + ") VALUES " + products + ";";
            Cursor cursor = db.rawQuery(selectQuery, null);
            String cursorSize = cursor == null ? "null" : cursor.getCount() + "";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static int getCountRow(String whereClause) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabaseRD();
        String query = "SELECT COUNT(*) FROM " + PRODUCTS_TABLE_NAME + (whereClause != null ? whereClause : "");
        Long numRows = DatabaseUtils.longForQuery(db, query, null);
        return numRows.intValue();
    }

    /**
     * @param queryStr
     * @param limit
     * @return
     */
    public static ArrayList<Product> getProducts(String queryStr, Integer limit) {
        String selectQuery = "SELECT " + ID + COMMA_SEP + UPC + COMMA_SEP + DESCRIPTION + COMMA_SEP + MANUFACTURER + COMMA_SEP + BRAND +
                " FROM " + PRODUCTS_TABLE_NAME + (queryStr != null && queryStr.length() > 0 ? " WHERE " + DESCRIPTION + " like '%" + queryStr + "%'" : "") +
                " ORDER BY " + DESCRIPTION + " ASC" +
                (limit != null && limit > 0 ? " limit " + limit : "");

        Cursor cursor = getCursor(selectQuery);
        ArrayList<Product> returnArray = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(0));
                product.setUpc(cursor.getLong(1));
                product.setDescription(cursor.getString(2).replace("&apos;", "'"));
                product.setManufacturer(cursor.getString(3).replace("&apos;", "'"));
                product.setBrand(cursor.getString(4).replace("&apos;", "'"));
                returnArray.add(product);
            }
            while (cursor.moveToNext());
        }
        return returnArray;
    }

}
