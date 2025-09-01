package com.example.abatherapy.Calendar;

import com.google.android.material.datepicker.CalendarConstraints;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class BlockedDatesValidator implements CalendarConstraints.DateValidator {

    private final Set<Long> blockedDays = new HashSet<>();

    public BlockedDatesValidator(Set<Long> fullyBookedDays) {
        blockedDays.addAll(fullyBookedDays);
    }

    @Override
    public boolean isValid(long date) {
        // Block fully booked dates
        if (blockedDays.contains(date)) return false;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);

        // Block past dates
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        if (date < today.getTimeInMillis()) return false;

        // Block weekends
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) { }

    public static final Parcelable.Creator<BlockedDatesValidator> CREATOR = new Parcelable.Creator<BlockedDatesValidator>() {
        @Override
        public BlockedDatesValidator createFromParcel(Parcel parcel) {
            return new BlockedDatesValidator(new HashSet<>());
        }

        @Override
        public BlockedDatesValidator[] newArray(int i) {
            return new BlockedDatesValidator[i];
        }
    };
}
