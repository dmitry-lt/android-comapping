package com.comapping.android.metamap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.comapping.android.R;

public class AboutDialog implements OnClickListener {

	private static final String ABOUT_STRING = "About\nAndroid Comapping:";
	private static final String DEVELOPERS_STRING = "Version: 1.0\nRelease date: 23.07.09\n\nDevelopers:\nTimur Abishev\nDmitriy Kozorez\nStepan Korshakov\nVladimir Kulikov\nDmitriy Manaev\nVictor Passichenko\nYuri Zemlyanskiy\n\nProject managers:\nNikolay Artamonov\nDmitriy Kichinsky\n";
	
	/** the dialog itself */
	private AlertDialog dialog;

	/** builder for the dialog */
	private AlertDialog.Builder builder;

	/**
	 * public constructor
	 * 
	 * @param context
	 */
	public AboutDialog(Context context) {

		this.builder = new AlertDialog.Builder(context);
		this.builder.setTitle(ABOUT_STRING);
		this.builder.setIcon(R.drawable.matmex_small);
		this.builder.setMessage(DEVELOPERS_STRING);
		this.builder.setPositiveButton("Hide", this);
	}

	/**
	 * show the dialog if it has not already shown
	 */
	public void show() {
		if (this.dialog == null) {
			this.dialog = this.builder.show();
		}
	}

	/**
	 * hide the dialog if it is visible now
	 */
	public void hide() {
		if (this.dialog != null) {
			this.dialog.dismiss();
			this.dialog = null;
		}
	}

	/**
	 * handles button clicks
	 */

	public void onClick(DialogInterface dialog, int button) {
		if (dialog == this.dialog) {
			if (button == DialogInterface.BUTTON_POSITIVE) {
				hide();
			}
		}
	}

}