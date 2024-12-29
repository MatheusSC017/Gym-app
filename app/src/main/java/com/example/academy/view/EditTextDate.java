package com.example.academy.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class EditTextDate extends AppCompatEditText {

    public EditTextDate(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        addTextChangedListener(new TextWatcher() {
            private int previousTextLength = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                previousTextLength = charSequence.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                String currentText = charSequence.toString();

                if (currentText.length() == 2) {
                    if (previousTextLength != 3) {
                        int month = Integer.parseInt(currentText);
                        if (month > 12) setText("12/");
                        else setText(currentText + "/");
                    } else{
                        setText(getText().toString().substring(0, 1));
                    }
                } else if (currentText.length() == 1 && Integer.parseInt(currentText) > 1) {
                    setText("0" + currentText + "/");
                }
                setSelection(getText().length());

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Do nothing
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) ||
                keyCode == KeyEvent.KEYCODE_DEL) {
            return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return isFocused() || super.onTouchEvent(event);
    }
}