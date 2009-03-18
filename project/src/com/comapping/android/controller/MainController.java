/*
 * Main Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements main controller
 */

package com.comapping.android.controller;

import com.comapping.android.commapingserver.ComappingServer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainController extends Activity {
	public static MainController instance = null;

	public static MainController getInstance() {
		return instance;
	}

	public ComappingServer server;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i("Main Controller", "Application onCreate");

		instance = this;
		server = new ComappingServer();

		LoginViewController.instance.activate();
	}

	public void login() {
		MetaMapViewController.instance.activate();
	}
}