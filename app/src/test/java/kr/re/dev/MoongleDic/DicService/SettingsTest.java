package kr.re.dev.MoongleDic.DicService;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

/**
 * Created by ice3x2 on 15. 5. 6..
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class SettingsTest {

    @Test
    public void testSettings() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        Settings settings = Settings.getSettings(context);
        boolean se =  !settings.isSoundEffect();
        boolean ucd =  !settings.isUseClipboardDic();
        boolean ut =  !settings.isUseTTS();
        boolean nc =  !settings.isWordCardNoneForceClose();
        int t = Integer.MAX_VALUE;

        settings.setSoundEffect(se);
        settings.setUseClipboardDic(ucd);
        settings.setUseTTS(ut);
        settings.setWordCardNoneForceClose(nc);
        settings.setWordCardKeepTime(t);
        settings.commit(context);
        settings = null;
        settings = Settings.getSettings(context);
        assertEquals(settings.isSoundEffect(), se);
        assertEquals(settings.isUseClipboardDic(), se);
        assertEquals(settings.isUseTTS(), ut);
        assertEquals(settings.isWordCardNoneForceClose(), nc);
        assertEquals(settings.getWordCardKeepTime(), t);
    }
}