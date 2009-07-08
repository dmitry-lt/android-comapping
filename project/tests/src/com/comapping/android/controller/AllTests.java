package com.comapping.android.controller;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.comapping.android.model.map.builder.MapBuilderTest;
import com.comapping.android.model.text.builder.FormattedTextSaxBuilderTest;
import com.comapping.android.view.LoginViewTest;

public class AllTests extends TestSuite {

    public static Test suite() {
    	TestSuite suite = new TestSuite();
    	suite.addTestSuite(MapBuilderTest.class);
    	suite.addTestSuite(LoginViewTest.class);
    	suite.addTestSuite(FormattedTextSaxBuilderTest.class);
        return suite;
    }
}
