package com.example.antismartphoneaddictionapp.utils;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.antismartphoneaddictionapp.Services.Authentication;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Helper {

    public static void makeSnackBar(View root, String snackTitle) {
        try {
            Snackbar snackbar = Snackbar.make(root, snackTitle, Snackbar.LENGTH_SHORT);
            snackbar.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String[] currentDateTime() {

        Date date = new Date();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        String currentDateTime = dateTimeFormat.format(date);
        String currentMonth = monthFormat.format(date);
        String currentYear = yearFormat.format(date);
        String currentDate = dateFormat.format(date);
        String currentTime = timeFormat.format(date);

        return new String[]{currentDateTime, currentMonth, currentYear, currentDate, currentTime};
    }

    @SuppressLint("HardwareIds")
    public static String getUniqueIdForDevice(Context context) {

        String deviceId = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
        } catch (Exception ignored) {
        }
        try {
            if (deviceId == null) {
                deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        } catch (Exception ignored) {
        }
        return Authentication.EncryptMessage(deviceId);
    }

    public static void goTo(Context context, Class<?> activity) {
        Intent intent = new Intent(context, activity);
        context.startActivity(intent);
    }


    public static void goTo(Context context, Class<?> activity, String name, Serializable value) {
        Intent intent = new Intent(context, activity);
        intent.putExtra(name, value);
        context.startActivity(intent);
    }

    public static void goTo(Context context, Class<?> activity, String name, int value) {
        Intent intent = new Intent(context, activity);
        intent.putExtra(name, value);
        context.startActivity(intent);
    }

    public static void goToWithFlags(Context context, Class<?> activity, int flags) {
        Intent intent = new Intent(context, activity);
        intent.setFlags(flags);
        context.startActivity(intent);
    }


    public static boolean isEmptyFieldValidation(EditText editText) {
        boolean isValidate = true;
        try {

            TextInputLayout textInputLayout = null;
            ViewParent parent = editText.getParent().getParent();
            if (parent instanceof TextInputLayout) {
                textInputLayout = (TextInputLayout) parent;
            }
            if (editText.getText().toString().isEmpty()) {
                if (textInputLayout != null) {
                    textInputLayout.isHelperTextEnabled();
                    textInputLayout.setError("Please " + textInputLayout.getHint());
                    textInputLayout.setErrorEnabled(true);
                } else {
                    editText.setError("Empty");
                }
                isValidate = false;
            } else {
                if (textInputLayout != null) {
                    textInputLayout.setErrorEnabled(false);
                } else {
                    editText.setError(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isValidate = false;
        }
        return isValidate;
    }

    public static boolean isEmptyFieldValidation(EditText[] editTexts) {
        boolean isValidate = true;
        try {
            for (EditText editText : editTexts) {
                TextInputLayout textInputLayout = null;
                ViewParent parent = editText.getParent().getParent();
                if (parent instanceof TextInputLayout) {
                    textInputLayout = (TextInputLayout) parent;
                }
                if (editText.getText().toString().trim().isEmpty()) {
                    if (textInputLayout != null) {
                        textInputLayout.isHelperTextEnabled();
                        textInputLayout.setError("Please " + textInputLayout.getHint());
                        textInputLayout.setErrorEnabled(true);
                    } else {
                        editText.setError("Empty");
                    }
                    isValidate = false;
                } else {
                    if (textInputLayout != null) {
                        textInputLayout.setErrorEnabled(false);
                    } else {
                        editText.setError(null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            isValidate = false;
        }
        return isValidate;
    }


    public static void setTextInputError(View view, String error) {
        ViewParent parent = view.getParent().getParent();
        if (parent instanceof TextInputLayout) {
            TextInputLayout textInputLayout = (TextInputLayout) parent;
            textInputLayout.isHelperTextEnabled();
            textInputLayout.setError(error);
            textInputLayout.setErrorEnabled(true);
        }
    }

    public static void clearText(ViewGroup viewGroup) {

        for (int i = 0, count = viewGroup.getChildCount(); i < count; ++i) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).getText().clear();
            }

            if (view instanceof RadioGroup) {
                ((RadioButton) ((RadioGroup) view).getChildAt(0)).setChecked(true);
            }

            if (view instanceof Spinner) {
                ((Spinner) view).setSelection(0);
            }

            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                clearText((ViewGroup) view);
        }
    }

    public static void clearError(ViewGroup viewGroup) {

        for (int i = 0, count = viewGroup.getChildCount(); i < count; ++i) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).setError(null);
            }

            if (view instanceof RadioGroup) {
                ((RadioButton) ((RadioGroup) view).getChildAt(0)).setError(null);
            }

            if (view instanceof TextInputLayout) {
                ((TextInputLayout) view).setError(null);
                ((TextInputLayout) view).setErrorEnabled(false);
            }

            if (view instanceof ViewGroup && (((ViewGroup) view).getChildCount() > 0))
                clearError((ViewGroup) view);
        }
    }
}