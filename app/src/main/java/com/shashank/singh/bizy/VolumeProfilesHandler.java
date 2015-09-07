package com.shashank.singh.bizy;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.View;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

// Custom Comparator Class to allow sorting VolumeProfiles based on their names
class VolumeProfilesHandler implements Comparator<VolumeProfile> {

    //region #variables
    private static Gson gson = new Gson();
    private static SharedPrefs vpp;
    //endregion #variables

    //region #CONSTANTS
    public static final int BLUE = 0, GREEN = 1, GRAY = 2;
    //endregion #CONSTANTS

    @Override
    public int compare(VolumeProfile vp1, VolumeProfile vp2) {
        return vp1.name.compareTo(vp2.name);
    }

    public static ArrayList<VolumeProfile> getAllVolumeProfiles(Context ctx) {
        vpp = new SharedPrefs(ctx);
        // get all SharedPreferences
        Map<String, ?> items = SharedPrefs.settings.getAll();
        // Empty ArrayList which will later be populated with VolumeProfiles
        ArrayList<VolumeProfile> vpList = new ArrayList<>();
        // Add to vpList if it is a VolumeProfile
        for (Map.Entry<String, ?> entry : items.entrySet()) {
            if (entry.getKey().startsWith("VolumeProfile"))
                vpList.add(gson.fromJson(entry.getValue().toString(), VolumeProfile.class));
        }
        return vpList;
    }

    public static boolean validTimeRange(Context ctx, VolumeProfile vp_new) {
        int st, end, st_new, end_new;
        ArrayList<VolumeProfile> vpList = getAllVolumeProfiles(ctx);
        st_new = getTimeInSeconds(vp_new.st_hr, vp_new.st_min) + 1;
        end_new = getTimeInSeconds(vp_new.end_hr, vp_new.end_min);
        for (VolumeProfile vp : vpList) {
            if (vp.num_id == vp_new.num_id) continue;
            st = getTimeInSeconds(vp.st_hr, vp.st_min) + 1;
            end = getTimeInSeconds(vp.end_hr, vp.end_min);
            for (int i = 0; i < 7; ++i) {
                if (vp.days[i] && vp_new.days[i]) {
                    // if time ranges overlap
                    if ((st <= end_new) && (end >= st_new)) return false;
                }
            }
        }
        return true;
    }

    public static int getTimeInSeconds(int hr, int min) {
        return (hr * 3600) + (min * 60);
    }

    public static Drawable getTintedDrawable(Resources res, @DrawableRes int drawableResId,
                                             int color) {
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = res.getDrawable(drawableResId, null);
        } else {
            drawable = res.getDrawable(drawableResId);
        }
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static void setBackground(View vi, Drawable drw) {
        if (Build.VERSION.SDK_INT >= 16) {
            vi.setBackground(drw);
        } else {
            vi.setBackgroundDrawable(drw);
        }
    }

    public static void setTheme(Context ctx) {
        new SharedPrefs(ctx);
        switch(SharedPrefs.settings.getInt("theme", BLUE)) {
            case BLUE:
                ctx.setTheme(R.style.AppThemeBlue);
                break;
            case GREEN:
                ctx.setTheme(R.style.AppThemeGreen);
                break;
            case GRAY:
                ctx.setTheme(R.style.AppThemeGray);
                break;
        }
    }
}