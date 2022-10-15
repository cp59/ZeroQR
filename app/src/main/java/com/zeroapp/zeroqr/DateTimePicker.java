package com.zeroapp.zeroqr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class DateTimePicker {
    public interface Listener {
        void onDateSelected(Date date);
    }
    public DateTimePicker(Context context,String title,Date defaultDate,Listener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.datetime_picker_dialog_layout,null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                TextInputLayout yearInputLayout = dialogView.findViewById(R.id.yearInputLayout);
                TextInputLayout monthInputLayout = dialogView.findViewById(R.id.monthInputLayout);
                TextInputLayout dayInputLayout = dialogView.findViewById(R.id.dayInputLayout);
                TextInputLayout hourInputLayout = dialogView.findViewById(R.id.hourInputLayout);
                TextInputLayout minuteInputLayout = dialogView.findViewById(R.id.minuteInputLayout);
                yearInputLayout.getEditText().setText(String.valueOf(1900+defaultDate.getYear()));
                monthInputLayout.getEditText().setText(String.valueOf(defaultDate.getMonth()+1));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(defaultDate);
                dayInputLayout.getEditText().setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                hourInputLayout.getEditText().setText(String.valueOf(defaultDate.getHours()));
                minuteInputLayout.getEditText().setText(String.valueOf(defaultDate.getMinutes()));
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int year = 0,month = 0,day = 0,hour = -1,minute = -1;
                        yearInputLayout.setError(null);
                        monthInputLayout.setError(null);
                        dayInputLayout.setError(null);
                        hourInputLayout.setError(null);
                        minuteInputLayout.setError(null);
                        if (yearInputLayout.getEditText().getText().toString().isEmpty()) {
                            yearInputLayout.setError("Invaild!");
                        } else {
                            year = Integer.parseInt(yearInputLayout.getEditText().getText().toString());
                        }
                        if (monthInputLayout.getEditText().getText().toString().isEmpty()) {
                            monthInputLayout.setError("Invaild!");
                        } else {
                            month = Integer.parseInt(monthInputLayout.getEditText().getText().toString());
                        }
                        if (dayInputLayout.getEditText().getText().toString().isEmpty()) {
                            dayInputLayout.setError("Invaild!");
                        } else {
                            day = Integer.parseInt(dayInputLayout.getEditText().getText().toString());
                        }
                        if (hourInputLayout.getEditText().getText().toString().isEmpty()) {
                            hourInputLayout.setError("Invaild!");
                        } else {
                            hour = Integer.parseInt(hourInputLayout.getEditText().getText().toString());
                        }
                        if (minuteInputLayout.getEditText().getText().toString().isEmpty()) {
                            minuteInputLayout.setError("Invaild!");
                        } else {
                            minute = Integer.parseInt(minuteInputLayout.getEditText().getText().toString());
                        }
                        if (month>12 || month == 0) {
                            monthInputLayout.setError("Invaild!");
                            month=0;
                        }
                        String can31Month = "{1} {3} {5} {7} {8} {10} {12}";
                        if (can31Month.contains("{"+ month +"}")) {
                            if (day>31 || day == 0) {
                                dayInputLayout.setError("Invaild!");
                                day=0;
                            }
                        } else {
                            if (day>30 || day ==0) {
                                dayInputLayout.setError("Invaild!");
                                day=0;
                            }
                        }
                        if (hour>23) {
                            hourInputLayout.setError("Invaild!");
                            hour=-1;
                        }
                        if (minute>59) {
                            minuteInputLayout.setError("Invaild!");
                            minute=-1;
                        }
                        if (year == 0 || month == 0 || day == 0 || hour == -1 || minute == -1) {
                            return;
                        }
                        dialog.dismiss();
                        listener.onDateSelected(new GregorianCalendar(year,month-1,day,hour,minute).getTime());
                    }
                });
            }
        });
        dialog.show();
    }
}
