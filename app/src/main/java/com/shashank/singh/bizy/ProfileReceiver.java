package com.shashank.singh.bizy;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import java.util.Calendar;


public class ProfileReceiver extends BroadcastReceiver
{
    //region #variables
    private static Gson gson = new Gson();
    private static SharedPrefs vpp;
    //endregion #variables

    @Override
    public void onReceive(Context ctx, Intent intent) {
        PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        new SharedPrefs(ctx);

        // extract data from extras
        Bundle extras = intent.getExtras();
        boolean start = extras.getBoolean("start");
        String vp_str = extras.getString("VolumeProfile");
        VolumeProfile vp = gson.fromJson(vp_str, VolumeProfile.class);

        if (!dayIncluded(vp.days)) {
            setAlarm(ctx, vp_str);
            return;
        }

        int levels[];
        // if this was triggered at the start of a profile
        if (start) {
            storeVolumesToPrefs(ctx);
            levels = vp.levels;
            if (SharedPrefs.settings.getBoolean("notify", false)) {
                notifyUser(ctx, vp.name, "Started", levels);
            }
        }
        // if this was triggered at the end of a profile
        else {
            levels = getVolumesFromPrefs(ctx);
            if (SharedPrefs.settings.getBoolean("notify", false)) {
                notifyUser(ctx, vp.name, "Ended", levels);
            }
        }

        // set volume levels
        setStreamVolumes(ctx, levels);

        // call setAlarm(...) to schedule next alarm
        setAlarm(ctx, vp_str);

        // release WakeLock
        wl.release();
    }

    // setAlarm(...) - for triggering actions on start and end of VolumeProfiles
    public static void setAlarm(Context ctx, String vp_str) {

        VolumeProfile vp = gson.fromJson(vp_str, VolumeProfile.class);
        vpp = new SharedPrefs(ctx);

        Intent intent = new Intent(ctx, ProfileReceiver.class);
        intent.putExtra("VolumeProfile", vp_str);
        intent.putExtra("start", true);

        //region #create start time calendar
        Calendar vp_st = Calendar.getInstance();
        vp_st.set(Calendar.HOUR_OF_DAY, vp.st_hr);
        vp_st.set(Calendar.MINUTE, vp.st_min);
        vp_st.set(Calendar.SECOND, 1);
        vp_st.set(Calendar.MILLISECOND, 0);
        //endregion #create start time calendar

        //region #create end time calendar
        Calendar vp_end = Calendar.getInstance();
        vp_end.set(Calendar.HOUR_OF_DAY, vp.end_hr);
        vp_end.set(Calendar.MINUTE, vp.end_min);
        vp_end.set(Calendar.SECOND, 0);
        vp_end.set(Calendar.MILLISECOND, 0);
        //endregion #create end time calendar

        Calendar calendar = Calendar.getInstance();
        if (calendar.after(vp_st) && calendar.before(vp_end)) {
            if (dayIncluded(vp.days)) {
                if(!SharedPrefs.settings.contains("ring")) {
                    storeVolumesToPrefs(ctx);
                }
                setStreamVolumes(ctx, vp.levels);
            }
            calendar = vp_end;
            intent.putExtra("start", false);
        }
        else {
            if (calendar.after(vp_end)) {
                calendar = vp_st;
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            else {
                calendar = vp_st;
            }
        }

        PendingIntent pi = PendingIntent.getBroadcast(ctx, vp.num_id,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT < 19)
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        else
            am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
    }

    public static void cancelAlarm(Context ctx, String vp_str) {

        VolumeProfile vp = gson.fromJson(vp_str, VolumeProfile.class);

        //region #create start time calendar
        Calendar vp_st = Calendar.getInstance();
        vp_st.set(Calendar.HOUR_OF_DAY, vp.st_hr);
        vp_st.set(Calendar.MINUTE, vp.st_min);
        vp_st.set(Calendar.SECOND, 1);
        vp_st.set(Calendar.MILLISECOND, 0);
        //endregion #create start time calendar

        //region #create end time calendar
        Calendar vp_end = Calendar.getInstance();
        vp_end.set(Calendar.HOUR_OF_DAY, vp.end_hr);
        vp_end.set(Calendar.MINUTE, vp.end_min);
        vp_end.set(Calendar.SECOND, 0);
        vp_end.set(Calendar.MILLISECOND, 0);
        //endregion #create end time calendar

        Calendar calendar = Calendar.getInstance();
        if (calendar.after(vp_st) && calendar.before(vp_end)) {
            if (dayIncluded(vp.days)) {
                setStreamVolumes(ctx, getVolumesFromPrefs(ctx));
            }
        }

        Intent intent = new Intent(ctx, ProfileReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(ctx, vp.num_id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

    public static int[] getVolumesFromPrefs(Context ctx) {
        vpp = new SharedPrefs(ctx);
        int[] levels = new int[4];
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

        levels[0] = am.getStreamVolume(AudioManager.STREAM_RING);
        levels[1] = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        levels[2] = am.getStreamVolume(AudioManager.STREAM_ALARM);
        levels[3] = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        levels[0] = SharedPrefs.settings.getInt("ring", levels[0]);
        levels[1] = SharedPrefs.settings.getInt("music", levels[1]);
        levels[2] = SharedPrefs.settings.getInt("alarm", levels[2]);
        levels[3] = SharedPrefs.settings.getInt("notif", levels[3]);

        SharedPrefs.editor.remove("ring");
        SharedPrefs.editor.remove("music");
        SharedPrefs.editor.remove("alarm");
        SharedPrefs.editor.remove("notif");
        SharedPrefs.editor.commit();

        return levels;
    }

    public static void storeVolumesToPrefs(Context ctx) {
        vpp = new SharedPrefs(ctx);
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        SharedPrefs.editor.putInt("ring", am.getStreamVolume(AudioManager.STREAM_RING));
        SharedPrefs.editor.putInt("music", am.getStreamVolume(AudioManager.STREAM_MUSIC));
        SharedPrefs.editor.putInt("alarm", am.getStreamVolume(AudioManager.STREAM_ALARM));
        SharedPrefs.editor.putInt("notif", am.getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        SharedPrefs.editor.commit();
    }

    public static void setStreamVolumes(Context ctx, int[] levels) {
        vpp = new SharedPrefs(ctx);
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_RING, levels[0], 0);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, levels[1], 0);
        am.setStreamVolume(AudioManager.STREAM_ALARM, levels[2], 0);
        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, levels[3], 0);
    }

    // check if today's day was included in list of days for the profile to be activated on
    public static boolean dayIncluded(boolean[] days) {
        return days[(Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7];
    }

    public static void notifyUser(Context ctx, String name, String profile_status, int[] levels) {
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(String.format("%s - %s", profile_status, name))
                .setContentText("Swipe down for details")
                .setAutoCancel(true);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle("Current volumes");
        inboxStyle.addLine(String.format("Ring: %.0f%%", levels[0] / 0.07));
        inboxStyle.addLine(String.format("Music: %.0f%%", levels[1] / 0.15));
        inboxStyle.addLine(String.format("Alarm: %.0f%%", levels[2] / 0.07));
        inboxStyle.addLine(String.format("Notification: %.0f%%", levels[3] / 0.07));
        nBuilder.setStyle(inboxStyle);

        NotificationManager nm = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(0);
        nm.notify(0, nBuilder.build());
    }
}