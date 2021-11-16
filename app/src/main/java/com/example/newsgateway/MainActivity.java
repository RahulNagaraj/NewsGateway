package com.example.newsgateway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    private final HashMap<String, ArrayList<Source>> topicsToSource = new HashMap<>();
    private final HashMap<String, ArrayList<Source>> languagesToSource = new HashMap<>();
    private final HashMap<String, ArrayList<Source>> countriesToSource = new HashMap<>();

    private Menu opt_menu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private ArrayAdapter<String> arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.drawer_list);

        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
//                    selectItem(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this,            /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );

        new Thread(new NewsAPIRunnable(this)).start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        Log.d(TAG, "onOptionsItemSelected: " + item);

//        List<String> lst = regionToSubRegion.get(item.getTitle().toString());
//        if (lst != null) {
//            subRegionDisplayed.addAll(lst);
//        }
//
//        arrayAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        opt_menu = menu;
        return true;
    }

    public void updateData(List<Source> sourcesList) {
        for (Source source : sourcesList) {
            String topic = source.getCategory();
            String language = source.getLanguage();
            String country = source.getCountry();

            if (!topicsToSource.containsKey(topic))
                topicsToSource.put(topic, new ArrayList<>());
            Objects.requireNonNull(topicsToSource.get(topic)).add(source);

            if (!languagesToSource.containsKey(language))
                languagesToSource.put(language, new ArrayList<>());
            Objects.requireNonNull(languagesToSource.get(language)).add(source);

            if (!countriesToSource.containsKey(country))
                countriesToSource.put(country, new ArrayList<>());
            Objects.requireNonNull(countriesToSource.get(country)).add(source);
        }

        ArrayList<String> topicsList = new ArrayList<>(topicsToSource.keySet());
        ArrayList<String> languagesList = new ArrayList<>(languagesToSource.keySet());
        ArrayList<String> countriesList = new ArrayList<>(countriesToSource.keySet());

        SubMenu topicsSubMenu = opt_menu.addSubMenu(0,1,0,"Topics");
        SubMenu languagesSubMenu = opt_menu.addSubMenu(0,1,0,"Languages");
        SubMenu countriesSubMenu = opt_menu.addSubMenu(0,1,0,"Countries");
        for (String s : topicsList) {
            topicsSubMenu.add(s);
        }

        for (String s : languagesList) {
            languagesSubMenu.add(s);
        }

        for (String s : countriesList) {
            countriesSubMenu.add(s);
        }

//        Log.d(TAG, "updateData: " + opt_menu);


//        arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_item, subRegionDisplayed);
//        mDrawerList.setAdapter(arrayAdapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    public void downloadFailed() {
//        hourlyList.clear();
//        adapter.notifyItemRangeChanged(0, hourlyList.size());
    }
}