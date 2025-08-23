package com.example.abatherapy.Calendar;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.material.datepicker.CalendarConstraints;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BlockedDatesValidator implements CalendarConstraints.DateValidator {
    private final List<Long> blockedDates = new ArrayList<>();

    public BlockedDatesValidator(List<String> dateStrings) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (String d : dateStrings) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(d));
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                blockedDates.add(cal.getTimeInMillis());
            } catch (ParseException ignored) {}
        }
    }

    @Override
    public boolean isValid(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return !blockedDates.contains(cal.getTimeInMillis());
    }

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {}

    public static final Parcelable.Creator<BlockedDatesValidator> CREATOR =
            new Parcelable.Creator<>() {
                public BlockedDatesValidator createFromParcel(Parcel in) { return new BlockedDatesValidator(new ArrayList<>()); }
                public BlockedDatesValidator[] newArray(int size) { return new BlockedDatesValidator[size]; }
            };
}
