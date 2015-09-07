package com.shashank.singh.bizy;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;


public class EditProfile extends ActionBarActivity {

    // region #variables
    private static Gson gson = new Gson();
    private static SharedPrefs vpp;
    private Button name;
    private Button times[] = new Button[2];
    private CheckBox days[] = new CheckBox[7];
    private SeekBar volumes[] = new SeekBar[4];
    private VolumeProfile vp;
    private String name_input;
    // endregion #variables

    //region #CONSTANTS
    private static final int ERROR_NONE = 0;
    private static final int ERROR_NAME_EMPTY = 1;
    private static final int ERROR_TIME_EMPTY = 2;
    private static final int ERROR_TIME_INVALID = 3;
    private static final int ERROR_DAY_EMPTY = 4;
    private static final int ERROR_TIME_CONFLICT = 5;
    //endregion #CONSTANTS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VolumeProfilesHandler.setTheme(this);
        // call base class's onCreate and inflate view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        vpp = new SharedPrefs(getApplicationContext());

        // region #get views
        name = (Button) findViewById(R.id.edit_profile_name);

        times[0] = (Button) findViewById(R.id.edit_profile_start_time);
        times[1] = (Button) findViewById(R.id.edit_profile_end_time);

        days[0] = (CheckBox) findViewById(R.id.edit_profile_MON);
        days[1] = (CheckBox) findViewById(R.id.edit_profile_TUE);
        days[2] = (CheckBox) findViewById(R.id.edit_profile_WED);
        days[3] = (CheckBox) findViewById(R.id.edit_profile_THU);
        days[4] = (CheckBox) findViewById(R.id.edit_profile_FRI);
        days[5] = (CheckBox) findViewById(R.id.edit_profile_SAT);
        days[6] = (CheckBox) findViewById(R.id.edit_profile_SUN);

        volumes[0] = (SeekBar) findViewById(R.id.edit_profile_volume_ring);
        volumes[1] = (SeekBar) findViewById(R.id.edit_profile_volume_music);
        volumes[2] = (SeekBar) findViewById(R.id.edit_profile_volume_alarm);
        volumes[3] = (SeekBar) findViewById(R.id.edit_profile_volume_notif);
        // endregion #get views

        //region #assign custom color to seekbars
        int[] attrs = new int[] { R.attr.active_color };
        TypedArray ta = obtainStyledAttributes(attrs);
        int active_color = ta.getColor(0, 0);
        ta.recycle();
        Resources res = getResources();
        for (SeekBar sb_temp : volumes) {
            Drawable sb = VolumeProfilesHandler.getTintedDrawable(res, R.drawable.seekbar, active_color);
            Drawable sb_thumb = VolumeProfilesHandler.getTintedDrawable(res, R.drawable.seekbar_thumb, active_color);
            sb_temp.setProgressDrawable(sb);
            sb_temp.setThumb(sb_thumb);
        }
        //endregion #assign custom color to seekbars


        // create an input dialog for the name and bind it to the name button
        name_input = "";
        setNamePickerListener();

        // create TimePickerDialogs for the start and end times and bind them to the times[] buttons
        setTimePickerListener(0, 12, 0);
        setTimePickerListener(1, 14, 0);

        // disable notification seekbar if ring volume is 0
        if (volumes[0].getProgress() == 0) volumes[3].setEnabled(false);
        volumes[0].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == volumes[0])
                    volumes[3].setEnabled(seekBar.getProgress() != 0);
            }
        });

        // if profile was not newly created, assign layout elements previous values
        Bundle extras = getIntent().getExtras();
        if (extras == null) return;
        vp = gson.fromJson(extras.getString("profile"), VolumeProfile.class);

        // region #populate with stored values
        name.setText(vp.name);
        times[0].setText(vp.getStartTime());
        times[1].setText(vp.getEndTime());
        for (int i = 0; i < 7; ++i)
            days[i].setChecked(vp.days[i]);
        for (int i = 0; i < 4; ++i)
            volumes[i].setProgress(vp.levels[i]);

        // create an input dialog for the name
        // bind it to the name button
        name_input = vp.name;
        setNamePickerListener();

        // create TimePickerDialogs for the start and end times and bind them to the times[] buttons
        setTimePickerListener(0, vp.st_hr, vp.st_min);
        setTimePickerListener(1, vp.end_hr, vp.end_min);
        // endregion #populate with stored values
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Context ctx = getApplicationContext();
        switch(item.getItemId())
        {
            case R.id.action_discard_profile:
                // Menu Option: Discard delete current VolumeProfile
                if (vp != null) {
                    vp.remove(ctx);
                }
                NavUtils.navigateUpFromSameTask(this);
                Toast.makeText(ctx, "Profile Deleted", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_save_profile:
                // try to save changes
                int result = saveChanges();
                // process output from saveChanges() to see if it failed or was successful
                switch (result) {
                    case ERROR_NAME_EMPTY:
                        Toast.makeText(ctx, "Name cannot be empty",
                                       Toast.LENGTH_SHORT).show();
                        return true;
                    case ERROR_TIME_EMPTY:
                        Toast.makeText(ctx, "Please enter both Start and End Times",
                                       Toast.LENGTH_SHORT).show();
                        return true;
                    case ERROR_TIME_INVALID:
                        Toast.makeText(ctx, "End Time must be after Start Time",
                                       Toast.LENGTH_SHORT).show();
                        return true;
                    case ERROR_DAY_EMPTY:
                        Toast.makeText(ctx, "Please select at least 1 Day",
                                       Toast.LENGTH_SHORT).show();
                        return true;
                    case ERROR_TIME_CONFLICT:
                        Toast.makeText(ctx, "Time range conflicts with other profiles",
                                       Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        // if successfully saved changes, notify user and return to Home
                        Toast.makeText(ctx, "Profile Saved",
                                       Toast.LENGTH_SHORT).show();
                        NavUtils.navigateUpFromSameTask(this);
                        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
                        return true;
                }
            case R.id.action_help:
                // create intent to go to help by starting Help
                Intent intent = new Intent(this, Help.class);
                startActivity(intent);
                overridePendingTransition(R.anim.push_into_in, R.anim.push_into_out);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int saveChanges() {
        String n = name.getText().toString();
        String st = times[0].getText().toString();
        String et = times[1].getText().toString();

        // if name is empty, return failed
        if (n.equals("Profile Name")) return ERROR_NAME_EMPTY;

        // start and end times validity checking
        if (st.length() > 5 || et.length() > 5) return ERROR_TIME_EMPTY;
        if (Integer.parseInt(et.substring(0,2)) < Integer.parseInt(st.substring(0, 2)))
            return 3;
        else if (Integer.parseInt(et.substring(0,2)) == Integer.parseInt(st.substring(0,2)) &&
                 Integer.parseInt(et.substring(3,5)) <= Integer.parseInt(st.substring(3,5)))
            return ERROR_TIME_INVALID;

        // at least one day selected
        boolean[] d = new boolean[7];
        boolean atLeastOne = false;
        int i;
        for (i = 0; i < 7; ++i) {
            d[i] = days[i].isChecked();
            atLeastOne = atLeastOne || d[i];
        }
        if (!atLeastOne) return ERROR_DAY_EMPTY;

        // get volume levels
        int[] l = new int[4];
        for (i = 0; i < 4; ++i)
            l[i] = volumes[i].getProgress();

        Context ctx = getApplicationContext();

        // if VolumeProfile already exists, edit it
        if (vp != null)
            vp = new VolumeProfile(ctx, vp.num_id, n, l, vp.active, true, st, et, d);
        // otherwise, create a new one with a newly generated id (achieved by passing -1)
        else
            vp = new VolumeProfile(ctx, -1, n, l, true, true, st, et, d);

        if (!VolumeProfilesHandler.validTimeRange(ctx, vp))
            return ERROR_TIME_CONFLICT;

        return ERROR_NONE;
    }

    private void setTimePickerListener(final int i, final int hr, final int min) {
        // set OnClickListener for setting the start and end times
        times[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog tpd = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hr, int min) {
                        times[i].setText(String.format("%02d:%02d", hr, min));
                    }
                }, hr, min, true);
                tpd.show();
            }
        });
    }

    private void setNamePickerListener() {
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get profile_name_input_dialog.xml view
                View promptView = LayoutInflater.from(EditProfile.this).
                        inflate(R.layout.profile_name_input_dialog, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EditProfile.this);

                alertDialogBuilder.setView(promptView);

                final EditText editText = (EditText) promptView.findViewById(R.id.edit_profile_name_input_dialog);
                editText.setText(name_input);

                // setup a dialog window
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (editText.getText().toString().isEmpty())
                                    name.setText("Profile Name");
                                else
                                    name.setText(editText.getText().toString());
                                name_input = editText.getText().toString().trim();
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
            }
        });
    }
}