package com.comapping.android.provider.communication;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.comapping.android.controller.R;

public class LoginActivityTest extends
		ActivityInstrumentationTestCase2<LoginActivity> {

	private Button login;
	private EditText eMail;
	private EditText password;
	private CheckBox check;
	private TextView passwordView;
	/**
	 * The first constructor parameter must refer to the package identifier of
	 * the package hosting the activity to be launched, which is specified in
	 * the AndroidManifest.xml file. This is not necessarily the same as the
	 * java package name of the class - in fact, in some cases it may not match
	 * at all.
	 */
	public LoginActivityTest() {
		super("com.comapping.android.controller", LoginActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	    final LoginActivity a = getActivity();
		login = (Button) a.findViewById(R.id.login);
		eMail = (EditText) a.findViewById(R.id.eMail);
		password = (EditText) a.findViewById(R.id.password);
		passwordView = (TextView) a.findViewById(R.id.passwordTextView);
		check = (CheckBox) a.findViewById(R.id.rememberUserCheckBox);
	}

	/**
	 * The name 'test preconditions' is a convention to signal that if this test
	 * doesn't pass, the test case was not set up properly and it might explain
	 * any and all failures in other tests. This is not guaranteed to run before
	 * other tests, as junit uses reflection to find the tests.
	 */
	@SmallTest
	public void testPreconditions() {
		assertNotNull(login);
		assertTrue("checkbox should be top of login button",
				check.getBottom() < login.getTop());
		assertTrue("password EditText should be right of pasword textView  ",
				passwordView.getRight() < password.getLeft());
		assertTrue("eMail should be focused", eMail.isFocused());
	}

	@SmallTest
	public void testGoingRightFromTopEditTextToLoginButton() {
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		assertTrue("login button should be focused", login.isFocused());
	}

	@MediumTest
	public void testLogin() {
		
		// Email field
		//input "android"
		sendKeys(KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_D,
				KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_O, KeyEvent.KEYCODE_I,
				KeyEvent.KEYCODE_D);
		//input "@"
		sendKeys(KeyEvent.KEYCODE_AT);
		//input "comapping"
		sendKeys(KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_O, KeyEvent.KEYCODE_M,
				KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_P,
				KeyEvent.KEYCODE_I, KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_G);
		//input "."
		sendKeys(KeyEvent.KEYCODE_PERIOD);
		//input "com"
		sendKeys(KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_O, KeyEvent.KEYCODE_M);
		//go down
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		// password field
		sendKeys(KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3);
		//go down
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		assertEquals(eMail.getText(),"android@comapping.com");
		assertEquals(password.getText(),"129");
		assertTrue("login button should be focused", login.isFocused());
	}
}