package com.unuuu.monarch.sample;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class MainActivityTest {
    @Before
    public void setup() {
    }

    @Test
    public void 基本のテスト() {
        assertThat("基本のテスト").isEqualTo("基本のテスト");
    }
}