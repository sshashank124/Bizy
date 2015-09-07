package com.shashank.singh.bizy;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Collections;


public class Home extends ActionBarActivity {

    // region #variables
    // endregion #variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VolumeProfilesHandler.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // ListView layout for the user's profiles
        ListView listview = (ListView) findViewById(R.id.profiles_list);
        // Allow user to select items in the ListView
        listview.setItemsCanFocus(true);

        // region #populate ListView with VolumeProfiles

        // Empty ArrayList which will later be populated with VolumeProfiles
        ArrayList<VolumeProfile> vpList = VolumeProfilesHandler.getAllVolumeProfiles(this);

        // Sort based on VolumeProfile name
        Collections.sort(vpList, new VolumeProfilesHandler());

        // Convert ArrayList to array so that it can be
        // passed to the custom ListView adapter: ProfilesListAdapter
        final VolumeProfile[] vps = vpList.toArray(new VolumeProfile[vpList.size()]);

        // Inflate ListView through custom class
        listview.setAdapter(new ProfilesListAdapter(this, Home.this, vps));

        // endregion #populate ListView with VolumeProfiles

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_create_new_profile:
                intent = new Intent(this, EditProfile.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, Settings.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.pull_outward_in, R.anim.pull_outward_out);
                return true;
            case R.id.action_help:
                intent = new Intent(this, Help.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_into_in, R.anim.push_into_out);
                return true;
            case R.id.action_rate_app:
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.shashank.singh.bizy"));
                startActivity(intent);
                return true;
            case R.id.action_feedback:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/email");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"sshashank124@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Bizy Feedback");
                startActivity(intent);
                return true;
            case R.id.action_share:
                intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "I'm using Bizy!");
                String shareMessage = "Check out Bizy on the Android Play Store! Bizy "
                        + "automatically manages your phone's volume levels "
                        + "based on your weekly schedule";
                intent.putExtra(android.content.Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(intent, "Share"));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VolumeProfilesHandler.setTheme(this);
        recreate();
    }
}