package com.example.abatherapy.Activities.Appointments;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.material.datepicker.CalendarConstraints;
import java.util.Calendar;

// Custom validator to block past dates and weekends
public class FutureWeekdayValidator implements CalendarConstraints.DateValidator {

    public FutureWeekdayValidator() {}

    protected FutureWeekdayValidator(Parcel in) {}

    @Override
    public boolean isValid(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);

        // block past dates
        if (date < System.currentTimeMillis()) return false;

        // block weekends
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {}

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<FutureWeekdayValidator> CREATOR =
            new Parcelable.Creator<FutureWeekdayValidator>() {
                @Override
                public FutureWeekdayValidator createFromParcel(Parcel in) {
                    return new FutureWeekdayValidator(in);
                }

                @Override
                public FutureWeekdayValidator[] newArray(int size) {
                    return new FutureWeekdayValidator[size];
                }
            };
}