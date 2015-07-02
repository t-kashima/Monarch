package com.unuuu.monarchsample;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by kashima on 15/07/03.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void 基本的なテストをする() {
        assertThat("テストのテスト", is("テストのテスト"));
    }

    @After
    public void tearDown() throws Exception {

    }
}
