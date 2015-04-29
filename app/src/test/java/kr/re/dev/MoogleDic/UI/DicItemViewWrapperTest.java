package kr.re.dev.MoogleDic.UI;

import android.content.Context;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created by ice3x2 on 15. 4. 30..
 */

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class DicItemViewWrapperTest {

    @Test
    public void testObtain() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        List<DicItemViewWrapper> items = Lists.newArrayList(
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context));
        for(DicItemViewWrapper item : items) {
            assertThat(item.isRecycled(), is(false));
        }
        DicItemViewWrapper.recycleAll();
        for(DicItemViewWrapper item : items) {
            assertThat(item.isRecycled(), is(true));
        }
        List<DicItemViewWrapper> newItems = Lists.newArrayList(
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context));

        for(int i = 0, n = newItems.size(); i < n; ++i) {
            DicItemViewWrapper itemsNew =  newItems.get(i);
            DicItemViewWrapper itemsOld =  items.get(i);
            assertThat(itemsNew.isRecycled(), is(false));
            assertThat(itemsOld.isRecycled(), is(false));
            assertEquals(itemsNew, itemsOld);

        }

    }

}