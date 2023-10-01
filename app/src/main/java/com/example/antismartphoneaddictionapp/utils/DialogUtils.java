package com.example.antismartphoneaddictionapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

import com.example.antismartphoneaddictionapp.R;

import java.util.Objects;


public class DialogUtils {

    public static Dialog showLoadingDialog(Context context, String message, Dialog dialog) {
        try {
            DialogUtils.dismissDialog(dialog);
            dialog = new Dialog(context, androidx.appcompat.R.style.Base_Theme_AppCompat_Dialog);
            Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
            dialog.setContentView(R.layout.circular_dialog);
            TextView tvLoadingText = dialog.findViewById(R.id.tvLoadingText);
            if (message.trim().isEmpty()) {
                tvLoadingText.setVisibility(View.GONE);
            } else {
                tvLoadingText.setText(message);
                tvLoadingText.setVisibility(View.VISIBLE);
            }
            dialog.setCancelable(false);
            if (!((Activity) context).isFinishing()) {
                dialog.show();
            }
            return dialog;
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void dismissDialog(Dialog dialog) {
        try {
            if (dialog != null) {
                try {
                    dialog.cancel();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static AlertDialog openAlertDialog(final Context context, String message, String positiveBtnText,
                                              boolean showNegativeBtn, boolean isFinish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);

        builder.setPositiveButton(positiveBtnText, (dialog, id) -> {
            dialog.cancel();
            if (isFinish)
                ((Activity) context).finish();
        });

        if (showNegativeBtn)
            builder.setNegativeButton("No", (dialog, id) -> {
                dialog.cancel();
            });
        return builder.create();
    }

    public static AlertDialog deleteDialog(final Context context
            , DialogInterface.OnClickListener onDeleteClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete?\nWARNING: This action cannot be undone");
        builder.setPositiveButton("Yes", onDeleteClickListener);
        builder.setNegativeButton("No", (dialog, id) -> {
            dialog.cancel();
        });
        return builder.create();
    }

}
