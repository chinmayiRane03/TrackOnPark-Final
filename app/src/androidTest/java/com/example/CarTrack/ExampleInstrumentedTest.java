
/*
<?xml version="1.0" encoding='utf-8' standalone='no'?>
<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
<blaze-out/k8-opt/genfiles/java/com/google/android/gmscore/integ/client/common/res/values-sk/strings.xml
<eat-comment/>
<string name="common_google_play_services_unknown_issue" msgid="2518680582564677258">"Aplikácia <xliff:g id="APP_NAME">%1$s</xliff:g> má problémy so službami Google Play. Skúste to znova."</string>
</resources>                                                                                                                                                                                                                                                                                                                                             
*/


package com.example.CarTrack;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.example.myapplication1", appContext.getPackageName());
    }
}
