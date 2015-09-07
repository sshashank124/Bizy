package com.shashank.singh.bizy;

import android.content.Context;

import com.google.gson.Gson;


public class VolumeProfile {

    // region #variables
    public int[] levels;
    public boolean active, vibrate;
    public String name;
    public int st_hr, st_min, end_hr, end_min;
    public boolean days[];
    public int num_id;
    public String id;
    public static Gson gson = new Gson();
    // endregion #variable

    // Constructor
    public VolumeProfile(Context ctx, int num_i, String nm, int[] l, boolean a, boolean v,
                         String st, String et, boolean d[]) {

        this.levels = l;
        this.active = a;
        this.vibrate = v;
        this.name = nm;
        this.days = d;
        this.st_hr = Integer.parseInt(st.substring(0, 2));
        this.st_min = Integer.parseInt(st.substring(3, 5));
        this.end_hr = Integer.parseInt(et.substring(0, 2));
        this.end_min = Integer.parseInt(et.substring(3, 5));
        // if this is a new VolumeProfile instead of an edited one give it a unique id
        if (num_i == -1) {
            this.num_id = (int)(System.currentTimeMillis() / 100L);
            this.id = "VolumeProfile"+this.num_id;
        }
        else {
            this.num_id = num_i;
            this.id = "VolumeProfile"+this.num_id;
        }
        setActive(ctx, a);
    }

    public void setActive(Context ctx, boolean a) {
        this.active = a;
        store(ctx);
        if (a) activate(ctx);
        else cancel(ctx);
    }

    public String getStartTime() { return String.format("%02d:%02d", st_hr, st_min); }

    public String getEndTime() { return String.format("%02d:%02d", end_hr, end_min); }

    public void activate(Context ctx) {
        ProfileReceiver.setAlarm(ctx, gson.toJson(this));
    }

    public void cancel(Context ctx) {
        ProfileReceiver.cancelAlarm(ctx, gson.toJson(this));
    }

    // store in Shared Preferences
    public void store(Context ctx) {
        SharedPrefs vpp = new SharedPrefs(ctx);
        String vp_json = gson.toJson(this);
        SharedPrefs.editor.putString(this.id, vp_json).commit();
    }

    public void remove(Context ctx) {
        setActive(ctx, false);
        SharedPrefs vpp = new SharedPrefs(ctx);
        SharedPrefs.editor.remove(this.id).commit();
    }
}