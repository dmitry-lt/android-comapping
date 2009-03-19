/*
 * Main Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements main controller
 */

package com.comapping.android.controller;

import com.comapping.android.communication.Client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class Main extends Activity {
	public static Main instance = null;

	public static Main getInstance() {
		return instance;
	}

	public Client client;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("Main Controller", "Application onCreate");

		instance = this;
		client = new Client();

		Login.instance.activate();
	}

	public void login() {
		MetaMap.instance.activate();
	}
}