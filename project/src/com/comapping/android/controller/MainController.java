/*
 * Main Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements main controller
 */

package com.comapping.android.controller;

import com.comapping.android.Options;
import com.comapping.android.communication.Client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MainController extends Activity {
	private static MainController instance = null;

	public static MainController getInstance() {
		return instance;
	}

	public Client client = new Client();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("Main Controller", "Application onCreate");

		instance = this;
		client.clientSideLogout();

		MetaMapController.getInstance().activate();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("Main Controller", "finish code: " + resultCode);

		if (((resultCode == RESULT_CANCELED) && (requestCode == Client.LOGIN_REQUEST_CODE))
				|| (resultCode == Options.RESULT_CHAIN_CLOSE)) {
			setResult(Options.RESULT_CHAIN_CLOSE);
			Log.i("Main Controller", "finish");
			finish();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void logout() {
		/*
		 * Storage.instance.set("key", "");
		 * LoginController.getInstance().activate();
		 * 
		 * new Thread() { public void run() { try { client.logout(); } catch
		 * (NotLoggedInException e) { Log.e("Main", "Logout without login"); }
		 * catch (ConnectionException e) { Log.e("Comapping",
		 * "Main: connection exception"); } } }.start();
		 */
	}
}