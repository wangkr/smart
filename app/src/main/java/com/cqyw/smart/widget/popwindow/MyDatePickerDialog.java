package com.cqyw.smart.widget.popwindow;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Kairong on 2015/10/16.
 * mail:wangkrhust@gmail.com
 */
public class MyDatePickerDialog extends DatePickerDialog {
    private int maxYear = Calendar.getInstance().get(Calendar.YEAR);
    private int minYear = 1900;
    private int currYear;
    private int currMonthOfYear;
    private int currDayOfMonth;

    public MyDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
        currYear = year;
        currMonthOfYear = monthOfYear;
        currDayOfMonth = dayOfMonth;
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        if(year >= minYear && year <= maxYear){
            currYear = year;
            currMonthOfYear = month;
            currDayOfMonth = day;
        } else {
            if (currYear > maxYear) {
                currYear = maxYear;
            } else if (currYear < minYear) {
                currYear = minYear;
            }
            updateDate(currYear, currMonthOfYear, currDayOfMonth);
        }
    }

    public void setMaxYear(int year){
        maxYear = year;
    }

    public void setMinYear(int year){
        minYear = year;
    }

    public void setTitle(CharSequence title) {
        super.setTitle("生 日");
    }
}
