package com.comapping.android.view;


import com.comapping.android.controller.R;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.FocusFinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginViewTest extends AndroidTestCase{
	
	private FocusFinder mFocusFinder;

    private ViewGroup mRoot;

    private Button login;
    private EditText eMail;
    private EditText password;
    private CheckBox check;
    private TextView passwordView;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mFocusFinder = FocusFinder.getInstance();
        
        // inflate the layout
        final Context context = getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        mRoot = (ViewGroup) inflater.inflate(R.layout.login, null);
        
        // manually measure it, and lay it out
        mRoot.measure(500, 500);
        mRoot.layout(0, 0, 500, 500);
        
        login = (Button) mRoot.findViewById(R.id.login);
        eMail = (EditText) mRoot.findViewById(R.id.eMail);
        password = (EditText) mRoot.findViewById(R.id.password);
        passwordView = (TextView) mRoot.findViewById(R.id.passwordTextView);
        check = (CheckBox) mRoot.findViewById(R.id.rememberUserCheckBox);
        
    }
        @SmallTest
        public void testPreconditions() {
            assertNotNull(login);
            assertTrue("checkbox should be top of login button",
                    check.getBottom() < login.getTop());
            assertTrue("password EditText should be right of pasword textView  ",
                    passwordView.getRight() < password.getLeft());
        }
        
        @SmallTest
        public void testGoingDownFromUp() {
        	assertEquals("down should be next focus from up",
                    password,
                    mFocusFinder.findNextFocus(mRoot, eMail, View.FOCUS_DOWN));
        	assertEquals("down should be next focus from up",
                    check,
                    mFocusFinder.findNextFocus(mRoot, password, View.FOCUS_DOWN));
        }
        
        
        
}

