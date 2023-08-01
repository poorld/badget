package com.poorld.badget.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.poorld.badget.R;
import com.poorld.badget.adapter.ItemAppAdapter;
import com.poorld.badget.entity.ItemAppEntity;
import com.poorld.badget.utils.PkgManager;

import java.util.List;

public class SelectAppAct extends AppCompatActivity {



    private MaterialToolbar toolbar;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ItemAppAdapter mAppAdapter;

    private boolean mShowSystemApp = false;

    private String mSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_select);
        
        initView();

        initData();

    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        mRecyclerView = findViewById(R.id.rv_list);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(getDrawable(R.drawable.baseline_arrow_back_24));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("TAG", "onRefresh: ");
                refreshData();
            }
        });
        mSwipeRefreshLayout.measure(0,0);
        mSwipeRefreshLayout.setRefreshing(true);

        mRecyclerView.setNestedScrollingEnabled(true);
    }

    private void initData() {

        Log.d("TAG", "initData: ");
        mAppAdapter = new ItemAppAdapter(this);

        mRecyclerView.setAdapter(mAppAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        refreshData();
    }

    private void refreshData() {
        Log.d("TAG", "mShowSystemApp: " + mShowSystemApp);

        List<ItemAppEntity> apps = mShowSystemApp ? PkgManager.getSystemAndUserApp(this) : PkgManager.getUserApp(this);

        if (!TextUtils.isEmpty(mSearch)) {
            apps = PkgManager.filterApp(apps, mSearch);
        }
        Log.d("TAG", "userAppsSize=" + apps.size());
        mAppAdapter.setList(apps);
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_show_system) {
            mShowSystemApp = !item.isChecked();
            item.setChecked(mShowSystemApp);

            mSwipeRefreshLayout.setRefreshing(true);
            initData();
        } /*else if (item.getItemId() == R.id.menu_show_system) {

        } else if (item.getItemId() == R.id.menu_sort_by_label) {

        } else if (item.getItemId() == R.id.menu_sort_by_package_name) {

        } else if (item.getItemId() == R.id.menu_sort_by_install_time) {

        } else if (item.getItemId() == R.id.menu_sort_by_update_time) {

        } else if (item.getItemId() == R.id.menu_reverse_order) {

        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_list, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSearch = s;
                refreshData();
                return true;
            }
        });
        return true;
    }

}
