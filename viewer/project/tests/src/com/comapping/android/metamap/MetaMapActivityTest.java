package com.comapping.android.metamap;

import com.comapping.android.R;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.KeyEvent;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class MetaMapActivityTest extends
		ActivityInstrumentationTestCase2<MetaMapActivity> {

	private ImageButton upLevelButton;
	private ImageButton homeButton;
	private ImageButton syncButton;
	private ImageButton switchButton;
	private TextView textView;
	private ListView listView;

	/**
	 * The first constructor parameter must refer to the package identifier of
	 * the package hosting the activity to be launched, which is specified in
	 * the AndroidManifest.xml file. This is not necessarily the same as the
	 * java package name of the class - in fact, in some cases it may not match
	 * at all.
	 */
	public MetaMapActivityTest() {
		super("com.comapping.android", MetaMapActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		final MetaMapActivity a = getActivity();

		upLevelButton = (ImageButton) a.findViewById(R.id.upLevelButton);
		homeButton = (ImageButton) a.findViewById(R.id.homeButton);
		syncButton = (ImageButton) a.findViewById(R.id.synchronizeButton);
		switchButton = (ImageButton) a.findViewById(R.id.viewSwitcher);
		textView = (TextView) a.findViewById(R.id.emptyListText);
		listView = (ListView) a.findViewById(R.id.listView);

	}

	/**
	 * The name 'test preconditions' is a convention to signal that if this test
	 * doesn't pass, the test case was not set up properly and it might explain
	 * any and all failures in other tests. This is not guaranteed to run before
	 * other tests, as junit uses reflection to find the tests.
	 */
	@MediumTest
	public void testPreconditions() {
		assertNotNull(upLevelButton);
		assertNotNull(homeButton);
		assertNotNull(syncButton);
		assertNotNull(switchButton);

		assertFalse("upLevelButton should be disabled", upLevelButton
				.isEnabled());
		assertFalse("homeButton should be disabled", homeButton.isEnabled());
	}

	@MediumTest
	public void testSwitchToSdCardViewAndClickMap() {
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
		assertTrue("sdcard view switched", switchButton.isFocused());
		assertFalse("sync button is disabled", syncButton.isEnabled());
		assertFalse("upLevelButton should be disabled", upLevelButton
				.isEnabled());
		assertFalse("homeButton should be disabled", homeButton.isEnabled());

		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
	}

	@MediumTest
	public void testListView() {
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		assertTrue(listView.isFocused());

		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				switchButton.requestFocus();
			}
		});
		// wait for the request to go through
		getInstrumentation().waitForIdleSync();

		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
	}

	@MediumTest
	public void testFolderAndUpButton() {
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
		assertTrue("upLevelButton should be disabled", upLevelButton
				.isEnabled());
		assertTrue("homeButton should be disabled", homeButton.isEnabled());
		// Give login button focus by having it
		// request focus. We post it
		// to the UI thread because we are not running on the same thread, and
		// any direct api calls that change state must be made from the UI
		// thread.
		// This is in contrast to instrumentation calls that send events that
		// are
		// processed through the framework and eventually find their way to
		// affecting the ui thread.
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				homeButton.requestFocus();
			}
		});
		// wait for the request to go through
		getInstrumentation().waitForIdleSync();

		assertTrue(homeButton.isFocused());
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);

		assertFalse(homeButton.isEnabled());
		assertTrue(switchButton.isEnabled());

		sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);

		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				upLevelButton.requestFocus();
			}
		});
		// wait for the request to go through
		getInstrumentation().waitForIdleSync();

		assertTrue(upLevelButton.isFocused());
		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
		assertFalse(upLevelButton.isEnabled());

		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				switchButton.requestFocus();
			}
		});
		// wait for the request to go through
		getInstrumentation().waitForIdleSync();

		sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
	}
}
