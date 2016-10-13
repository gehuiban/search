package com.logan19gp.search;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.logan19gp.search.Utils.DbHelper;
import com.logan19gp.search.Utils.Product;
import com.logan19gp.search.Utils.ProductAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final String EVENT_FINISH_UPDATE = "EVENT_FINISH_UPDATE";
    public static final String EVENT_UPDATE = "EVENT_UPDATE";
    private static final String PREFS_FILE = "prefs_file";
    private static final String DATA_LOADED = "DATA_LOADED";
    private static final String SEARCH_STR = "SEARCH_STR";
    private String existingSearchQuery;
    boolean isInForeground;
    private BroadcastReceiver eventReceiver;
    private ProductAdapter productAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DbHelper.DatabaseManager.initializeInstance(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSupportActionBar(toolbar);
        productAdapter = new ProductAdapter();
        recyclerView.setAdapter(productAdapter);
        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        boolean isUpdating = prefs.getBoolean(DATA_LOADED, false);
        int count = DbHelper.getCountRow(null);
        if (count < 1 || !isUpdating) {
            Intent updateService = new Intent(this, UpdateProdService.class);
            startService(updateService);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(DATA_LOADED, true);
            editor.apply();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isInForeground = false;

        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SEARCH_STR, existingSearchQuery);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isInForeground = true;

        SharedPreferences prefs = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        existingSearchQuery = prefs.getString(SEARCH_STR, "");
        showSearchResult(existingSearchQuery);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (eventReceiver == null) {
            IntentFilter mFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            mFilter.addAction(EVENT_FINISH_UPDATE);
            mFilter.addAction(EVENT_UPDATE);
            eventReceiver = new ActivityEventReceiver();
            registerReceiver(eventReceiver, mFilter);
        }
    }

    @Override
    protected void onStop() {
        try {
            if (eventReceiver != null) {
                unregisterReceiver(eventReceiver);
                eventReceiver = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        super.onStop();
    }

    public class ActivityEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleEventReceived(intent);
        }
    }

    private void handleEventReceived(Intent intent) {
        if (isInForeground && intent != null && intent.getExtras() != null) {
            Boolean isEnded = intent.getBooleanExtra(EVENT_FINISH_UPDATE, false);
            Boolean isUpdated = intent.getBooleanExtra(EVENT_UPDATE, false);
            if (Boolean.TRUE.equals(isEnded)) {
                showSearchResult(existingSearchQuery);
            } else if (Boolean.TRUE.equals(isUpdated)) {
                showSearchResult(existingSearchQuery);
            }
        }
    }

    private void showSearchResult(final String query) {
        ArrayList<Product> products = DbHelper.getProducts(query, 200);
        productAdapter.clearProducts();
        productAdapter.addProducts(products);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setQueryHint("Search by Item Description");
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        SearchView.SearchAutoComplete theTextArea = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        if (existingSearchQuery.length() > 0) {
            mSearchView.callOnClick();
            theTextArea.setText(existingSearchQuery);
            mSearchView.setIconifiedByDefault(false);
        }
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                }
            }
        });
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                showSearchResult(newText);
                mSearchView.setIconified(false);
                existingSearchQuery = newText;
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onSearchRequested() {
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
