package com.odd.ostrinov2.dialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.odd.ostrinov2.R;

public class FunnyJunk extends DialogFragment{

        AlertDialog.Builder builder;
        LayoutInflater inflater;
        View dialogView;
        ImageView jojo;

    public FunnyJunk(){}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            builder = new AlertDialog.Builder(getActivity());

            inflater = getActivity().getLayoutInflater();
            dialogView = inflater.inflate(R.layout.activity_yareyare, null);

            builder.setView(dialogView);
            jojo = (ImageView) dialogView.findViewById(R.id.jojo);
            return builder.create();
        }
    }
