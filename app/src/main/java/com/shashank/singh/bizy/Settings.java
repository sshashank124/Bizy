package com.shashank.singh.bizy;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class Settings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VolumeProfilesHandler.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        new SharedPrefs(this);

        //region #theme radiobuttons
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.settings_theme_radio_group);
        int theme = SharedPrefs.settings.getInt("theme", 0);
        switch (theme) {
            case 0:
                radioGroup.check(R.id.settings_blue_theme);
                break;
            case 1:
                radioGroup.check(R.id.settings_green_theme);
                break;
            case 2:
                radioGroup.check(R.id.settings_gray_theme);
                break;
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.settings_blue_theme:
                        SharedPrefs.editor.putInt("theme", 0);
                        break;
                    case R.id.settings_green_theme:
                        SharedPrefs.editor.putInt("theme", 1);
                        break;
                    case R.id.settings_gray_theme:
                        SharedPrefs.editor.putInt("theme", 2);
                        break;
                }
                SharedPrefs.editor.commit();
                recreate();
            }
        });
        //endregion #theme radiobuttons

        //region #notify user checkbox
        CheckBox notify_user = (CheckBox) findViewById(R.id.settings_notify_profile_change);
        notify_user.setChecked(SharedPrefs.settings.getBoolean("notify", false));
        notify_user.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPrefs.editor.putBoolean("notify", isChecked).commit();
            }
        });
        //endregion #notify user checkbox

        //region #initialize ad
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        //endregion #initialize ad
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, new Intent());
        super.onBackPressed();
        overridePendingTransition(R.anim.push_into_in, R.anim.push_into_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}