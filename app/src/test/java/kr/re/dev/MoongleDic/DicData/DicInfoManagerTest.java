package kr.re.dev.MoongleDic.DicData;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import kr.re.dev.MoongleDic.DicData.Database.DicInfoManager;

import static junit.framework.Assert.assertEquals;

/**
 * Created by ice3x2 on 15. 4. 22..
 *
 *
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class DicInfoManagerTest{




    DicInfoManager mDicInfoManager;

    @Before
    public void setUp() throws Exception {

        Context context = Robolectric.getShadowApplication().getApplicationContext();
        mDicInfoManager = DicInfoManager.newInstance(context);
        mDicInfoManager = DicInfoManager.newInstance(context);
    }

    @Test
    public void testSuccess() {

    }


}