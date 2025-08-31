package com.example.abatherapy.Calendar;

import com.google.android.material.datepicker.CalendarConstraints;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashSet;
import java.util.Set;

public class BlockedDatesValidator implements CalendarConstraints.DateValidator {

    private final Set<Long> blockedDays = new HashSet<>();

    public BlockedDatesValidator(Set<Long> fullyBookedDays) {
        blockedDays.addAll(fullyBookedDays);
    }

    @Override
    public boolean isValid(long date) {
        return !blockedDays.contains(date);
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
