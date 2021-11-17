package com.example.newsgateway;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.newsgateway.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private final HashMap<String, ArrayList<Source>> topicsToSource = new HashMap<>();
    private final HashMap<String, ArrayList<Source>> languagesToSource = new HashMap<>();
    private final HashMap<String, ArrayList<Source>> countriesToSource = new HashMap<>();
    private final ArrayList<String> sourcesDisplayed = new ArrayList<>();
    private String selectedTopic = null;
    private String selectedLanguage = null;
    private String selectedCountry = null;

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
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item.getTitle());
            return true;
        }

        SubMenu subMenu = item.getSubMenu();

        if (subMenu == null) {
            int id = item.getItemId();
            if (id == 0) {
                selectedTopic = item.getTitle().toString();
                ArrayList<Source> topics = topicsToSource.get(selectedTopic);
                setTitle(String.format("%s (%s)", getString(R.string.app_name), topics.size()));
                sourcesDisplayed.clear();
                arrayAdapter.notifyDataSetChanged();
                for (Source source : topics) {
                    sourcesDisplayed.add(source.getName());
                }
            }
            else if (id == 1) {
                selectedLanguage = item.getTitle().toString();
                ArrayList<Source> languages = languagesToSource.get(selectedLanguage);
                sourcesDisplayed.clear();
                arrayAdapter.notifyDataSetChanged();
//                String topic = selectedTopic.equalsIgnoreCase("all") ? "All" : selectedTopic;

                List<Source> filteredLanguages;
                if (selectedTopic != null) {
                    filteredLanguages = languages.stream()
                            .filter(source1 -> source1.getCategory().equalsIgnoreCase(selectedTopic))
                            .collect(Collectors.toList());
                } else {
                    filteredLanguages = new ArrayList<>(languages);
                }
                setTitle(String.format("%s (%s)", getString(R.string.app_name), filteredLanguages.size()));
                for (Source source : filteredLanguages) {
                    sourcesDisplayed.add(source.getName());
                }
            }
            else if (id == 2) {
                selectedCountry = item.getTitle().toString();
                ArrayList<Source> countries = countriesToSource.get(selectedCountry);
                sourcesDisplayed.clear();
                arrayAdapter.notifyDataSetChanged();

                List<Source> filteredCountries;
                if (selectedLanguage != null && !selectedLanguage.equalsIgnoreCase("All")) {
                    filteredCountries = countries.stream()
                            .filter(source1 -> source1.getLanguage().equalsIgnoreCase(selectedLanguage))
                            .collect(Collectors.toList());
                } else {
                    filteredCountries = new ArrayList<>(countries);
                }
                setTitle(String.format("%s (%s)", getString(R.string.app_name), filteredCountries.size()));
                for (Source source : filteredCountries) {
                    sourcesDisplayed.add(source.getName());
                }
            }
        }

        arrayAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        opt_menu = menu;
        return true;
    }

    public void updateData(List<Source> sourcesList) {
        topicsToSource.put("all", new ArrayList<>(sourcesList));
        languagesToSource.put("All", new ArrayList<>(sourcesList));
        countriesToSource.put("All", new ArrayList<>(sourcesList));

        for (Source source : sourcesList) {
            String topic = source.getCategory();
            String language = source.getLanguage();
            String country = source.getCountry();

            if (!topicsToSource.containsKey(topic))
                topicsToSource.put(topic, new ArrayList<>());
            Objects.requireNonNull(topicsToSource.get(topic)).add(source);


            String jObject = Utility.convertJsonToString(getResources(), R.raw.language_codes);

            try {
                JSONObject jsonObject  = new JSONObject(jObject);
                JSONArray jsonArray = jsonObject.getJSONArray("languages");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObj = jsonArray.getJSONObject(i);
                    if (jObj.getString("code").toLowerCase(Locale.ROOT).equalsIgnoreCase(language)) {
                        if (!languagesToSource.containsKey(jObj.getString("name"))) {
                            languagesToSource.put(jObj.getString("name"), new ArrayList<>());
                        }
                        Objects.requireNonNull(languagesToSource.get(jObj.getString("name"))).add(source);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String jObjectCountries = Utility.convertJsonToString(getResources(), R.raw.country_codes);

            try {
                JSONObject jsonObject  = new JSONObject(jObjectCountries);
                JSONArray jsonArray = jsonObject.getJSONArray("countries");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObj = jsonArray.getJSONObject(i);
                    if (jObj.getString("code").toLowerCase(Locale.ROOT).equalsIgnoreCase(country)) {
                        if (!countriesToSource.containsKey(jObj.getString("name"))) {
                            countriesToSource.put(jObj.getString("name"), new ArrayList<>());
                        }
                        Objects.requireNonNull(countriesToSource.get(jObj.getString("name"))).add(source);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayList<String> topicsList = new ArrayList<>(topicsToSource.keySet());
        Collections.sort(topicsList);
        ArrayList<String> languagesList = new ArrayList<>(languagesToSource.keySet());
        Collections.sort(languagesList);
        ArrayList<String> countriesList = new ArrayList<>(countriesToSource.keySet());
        Collections.sort(countriesList);

        SubMenu topicsSubMenu = opt_menu.addSubMenu(0,0,0,"Topics");
        SubMenu languagesSubMenu = opt_menu.addSubMenu(0,1,0,"Languages");
        SubMenu countriesSubMenu = opt_menu.addSubMenu(0,2,0,"Countries");

        for (int i = 0; i < topicsList.size(); i++) {
            topicsSubMenu.add(Menu.NONE, 0, i, topicsList.get(i));
        }

        for (int i = 0; i < languagesList.size(); i++) {
            languagesSubMenu.add(Menu.NONE, 1, i, languagesList.get(i));
        }

        for (int i = 0; i < countriesList.size(); i++) {
            countriesSubMenu.add(Menu.NONE, 2, i, countriesList.get(i));
        }

        ArrayList<Source> sources = topicsToSource.get("all");
        for (Source source : sources) {
            sourcesDisplayed.add(source.getName());
        }


        arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_item, sourcesDisplayed);
        mDrawerList.setAdapter(arrayAdapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    public void downloadFailed() {
        sourcesDisplayed.clear();
        arrayAdapter.notifyDataSetChanged();
    }
}