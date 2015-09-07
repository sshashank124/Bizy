package com.shashank.singh.bizy;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class Help extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VolumeProfilesHandler.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        List<String> helpTitles = Arrays.asList(getResources().getStringArray(R.array.help_titles));
        List<String> helpBizy = Arrays.asList(getResources().getStringArray(R.array.help_child_Bizy));
        List<String> helpCreate = Arrays.asList(getResources().getStringArray(R.array.help_child_create));
        List<String> helpEdit = Arrays.asList(getResources().getStringArray(R.array.help_child_edit));
        List<String> helpDelete = Arrays.asList(getResources().getStringArray(R.array.help_child_delete));

        HashMap<String, List<String>> helpChildren = new HashMap<>();

        helpChildren.put(helpTitles.get(0), helpBizy);
        helpChildren.put(helpTitles.get(1), helpCreate);
        helpChildren.put(helpTitles.get(2), helpEdit);
        helpChildren.put(helpTitles.get(3), helpDelete);

        ExpandableListView explistview = (ExpandableListView) findViewById(R.id.help_list);

        explistview.setAdapter(new HelpExpandableListAdapter(this, helpTitles, helpChildren));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_outward_in, R.anim.pull_outward_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}