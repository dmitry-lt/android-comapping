/*
 * Main Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements main controller
 */

package com.comapping.android.controller;

import com.comapping.android.communication.Client;
import com.comapping.android.communication.ConnectionException;
import com.comapping.android.communication.NotLoggedInException;
import com.comapping.android.storage.Storage;

import android.app.Activity;
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

		LoginController.getInstance().activate();
	}

	public void login() {
		MetaMapController.getInstance().activate();
	}

	public void logout() {
		Storage.instance.set("key", "");
		LoginController.getInstance().activate();

		new Thread() {
			public void run() {
				try {
					client.logout();
				} catch (NotLoggedInException e) {
					Log.e("Main", "Logout without login");
				} catch (ConnectionException e) {
					Log.e("Comapping", "Main: connection exception");
				}
			}
		}.start();
	}
}