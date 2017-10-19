package com.dave.soul.exchange_app.view.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dave.soul.exchange_app.R


/**
 * Created by soul on 2017. 10. 19..
 */
class BackPressedDialog(activity: Activity) : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_back_pressed_ad, container, false)
        return v
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setPositiveButton("취소", DialogInterface.OnClickListener { dialog, id ->
                    dismiss()
                })
                .setNegativeButton("종료", DialogInterface.OnClickListener { dialog, id ->
                    activity.finish()
                })
        // Create the AlertDialog object and return it
        return builder.create()
    }
}