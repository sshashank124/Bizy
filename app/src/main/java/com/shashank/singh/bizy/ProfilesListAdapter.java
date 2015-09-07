package com.shashank.singh.bizy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

public class ProfilesListAdapter extends BaseAdapter {

    // region #variables
    private Gson gson = new Gson();
    private SharedPrefs vpp;
    private Context context;
    private Activity activity;
    private VolumeProfile[] data;
    private static LayoutInflater inflater = null;
    // endregion #variables

    //region #CONSTANTS
    private static final int NAME_MAX_DISPLAY_LENGTH = 20;
    //endregion #CONSTANTS

    // Constructor
    public ProfilesListAdapter(Context context, Activity activity, VolumeProfile[] data) {
        this.context = context;
        this.activity = activity;
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() { return data.length; }

    @Override
    public VolumeProfile getItem(int position) { return data[position]; }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // recycle off-screen views
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.row, parent, false);

        // allow user to select individual items in the list
        vi.setClickable(true);
        vi.setFocusable(true);

        // region #define views
        TextView nameView, startTimeView, endTimeView;
        TextView[] dayTextViews = new TextView[7];
        ProgressBar[] progressBars = new ProgressBar[4];
        final CheckBox activeView;
        // endregion #define views

        // region #bind views
        activeView = (CheckBox) vi.findViewById(R.id.profile_active);
        nameView = (TextView) vi.findViewById(R.id.profile_name);

        startTimeView = (TextView) vi.findViewById(R.id.profile_start_time);
        endTimeView = (TextView) vi.findViewById(R.id.profile_end_time);

        dayTextViews[0] = (TextView) vi.findViewById(R.id.profile_day_MON);
        dayTextViews[1] = (TextView) vi.findViewById(R.id.profile_day_TUE);
        dayTextViews[2] = (TextView) vi.findViewById(R.id.profile_day_WED);
        dayTextViews[3] = (TextView) vi.findViewById(R.id.profile_day_THU);
        dayTextViews[4] = (TextView) vi.findViewById(R.id.profile_day_FRI);
        dayTextViews[5] = (TextView) vi.findViewById(R.id.profile_day_SAT);
        dayTextViews[6] = (TextView) vi.findViewById(R.id.profile_day_SUN);

        progressBars[0] = (ProgressBar) vi.findViewById(R.id.row_display_pb_ring);
        progressBars[1] = (ProgressBar) vi.findViewById(R.id.row_display_pb_music);
        progressBars[2] = (ProgressBar) vi.findViewById(R.id.row_display_pb_alarm);
        progressBars[3] = (ProgressBar) vi.findViewById(R.id.row_display_pb_notification);
        // endregion #bind views

        // region #set listeners for checkbox and list items

        // set OnClickListener for active/inactive checkbox
        activeView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                data[position].setActive(buttonView.getContext(), isChecked);
                data[position].active = isChecked;
            }
        });

        // set OnClickListener for editing existing VolumeProfiles
        vi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditProfile.class);
                intent.putExtra("profile", gson.toJson(data[position]));
                v.getContext().startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
            }
        });

        // endregion #set listeners for checkbox and list items

        // region #populate views
        // assign values to layout elements
        activeView.setChecked(data[position].active);

        String nm = data[position].name;
        if (nm.length() > NAME_MAX_DISPLAY_LENGTH)
            nm = nm.substring(0, NAME_MAX_DISPLAY_LENGTH - 3) + "...";
        nameView.setText(nm);

        startTimeView.setText(data[position].getStartTime());
        endTimeView.setText(data[position].getEndTime());

        int[] attrs = new int[] { R.attr.dark_active_color };
        TypedArray ta = context.obtainStyledAttributes(attrs);
        int dark_active_color = ta.getColor(0, 0);
        ta.recycle();

        // set selected days to have a darker color than the rest
        for (int i = 0; i < 7; ++i) {
            if (data[position].days[i])
                dayTextViews[i].setTextColor(context.getResources().getColor(R.color.white));
            else
                dayTextViews[i].setTextColor(dark_active_color);
        }

        // set the 4 (ring, music, alarm, notifications) volume levels
        for (int i = 0; i < 4; ++i) {
            progressBars[i].setProgress(data[position].levels[i]);
        }
        // endregion #populate views

        return vi;
    }
}