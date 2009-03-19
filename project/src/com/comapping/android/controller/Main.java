/*
 * Main Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements main controller
 */

package com.comapping.android.controller;

import com.comapping.android.communication.Client;
import com.comapping.android.communication.NotLoggedInException;
import com.comapping.android.storage.Storage;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class Main extends Activity {
	public static Main instance = null;

	public static Main getInstance() {
		return instance;
	}

	public Client client = new Client();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("Main Controller", "Application onCreate");

		instance = this;
		client.clear();

		Login.instance.activate();
	}

	public void login() {
		MetaMap.instance.activate();
	}

	public void logout() {
		Storage.instance.set("key", "");
		Login.instance.activate();

		new Thread() {
			public void run() {
				try {
					client.logout();
				} catch (NotLoggedInException e) {
					Log.e("Main", "Logout without login");
				}
			}
		}.start();
	}
}