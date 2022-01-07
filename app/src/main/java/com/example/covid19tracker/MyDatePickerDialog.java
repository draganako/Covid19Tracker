package com.example.covid19tracker;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.Calendar;

    public class MyDatePickerDialog extends AlertDialog implements DialogInterface.OnClickListener, DatePicker.OnDateChangedListener {

        private DatePickerDialog.OnDateSetListener mDateSetListener;
        private DatePicker mDatePicker;

        public MyDatePickerDialog(@NonNull Context context) {
            super(context);
            init();
        }

        protected MyDatePickerDialog(@NonNull Context context, int themeResId) {
            super(context, themeResId);
            init();
        }

        protected MyDatePickerDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
            init();
        }


        private void init() {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.calendar_view, null);
            mDatePicker = view.findViewById(R.id.datePicker);
            setView(view);
        }

        public void showDatePicker(DatePickerDialog.OnDateSetListener listener, Calendar defaultDate) {

            setButton(BUTTON_POSITIVE, getContext().getString(android.R.string.ok), this);
            setButton(BUTTON_NEGATIVE, getContext().getString(android.R.string.cancel), this);

            mDateSetListener = listener;

            if (defaultDate == null) {
                defaultDate = Calendar.getInstance();

            }
            int year = defaultDate.get(Calendar.YEAR);
            int monthOfYear = defaultDate.get(Calendar.MONTH);
            int dayOfMonth = defaultDate.get(Calendar.DAY_OF_MONTH);
            mDatePicker.init(year, monthOfYear, dayOfMonth, this);

            show();
        }


        @Override
        public void onClick(@NonNull DialogInterface dialog, int which) {
            switch (which) {
                case BUTTON_POSITIVE:
                    if (mDateSetListener != null) {
                        mDatePicker.clearFocus();//!!! da komituje sve promene primoran
                        if (mDateSetListener != null) {
                            mDateSetListener.onDateSet(mDatePicker, mDatePicker.getYear(),
                                    mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
                        }
                    }
                    break;
                case BUTTON_NEGATIVE:
                    cancel();
                    break;
            }
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        }
    }
