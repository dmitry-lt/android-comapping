package com.comapping.android.metamap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.comapping.android.R;

public class AboutDialog implements OnClickListener {

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
		this.builder.setTitle(R.string.AboutDialogTitle);
		this.builder.setIcon(R.drawable.app_icon_64);
		this.builder.setMessage(R.string.AboutDevelopers);
		this.builder.setPositiveButton(R.string.NeutralButtonText, this);
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