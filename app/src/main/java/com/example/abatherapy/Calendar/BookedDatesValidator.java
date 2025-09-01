package com.example.abatherapy.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.material.datepicker.CalendarConstraints;

import java.util.HashSet;
import java.util.Set;

public class BookedDatesValidator implements CalendarConstraints.DateValidator {

    private final Set<Long> bookedDays = new HashSet<>();

    public BookedDatesValidator(Set<Long> bookedDates) {
        bookedDays.addAll(bookedDates);
    }

    @Override
    public boolean isValid(long date) {
        return bookedDays.contains(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) { }

    public static final Parcelable.Creator<BookedDatesValidator> CREATOR = new Parcelable.Creator<BookedDatesValidator>() {
        @Override
        public BookedDatesValidator createFromParcel(Parcel in) {
            return new BookedDatesValidator(new HashSet<>());
        }

        @Override
        public BookedDatesValidator[] newArray(int size) {
            return new BookedDatesValidator[size];
        }
    };
}