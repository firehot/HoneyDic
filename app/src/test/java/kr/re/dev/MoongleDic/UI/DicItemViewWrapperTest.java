package kr.re.dev.MoongleDic.UI;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
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
        int count = 0;
        for(DicItemViewWrapper item : items) {
            assertThat(item.isRecycled(), is(false));
        }
        DicItemViewWrapper.recycleAll();
        for(DicItemViewWrapper item : items) {
            assertThat(item.isRecycled(), is(true));
        }
        LinkedList<DicItemViewWrapper> newItems = Lists.newLinkedList(Lists.newArrayList(
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context),
                DicItemViewWrapper.obtain(context)));




        for(int i = 0, n = newItems.size(); i < n; ++i) {
            DicItemViewWrapper itemsNew =  newItems.get(i);
            DicItemViewWrapper itemsOld =  items.get(i);
            //newItems.remove(itemsOld);
            assertThat(itemsNew.isRecycled(), is(false));
            assertThat(itemsOld.isRecycled(), is(false));
            assertEquals(itemsNew, itemsOld);
        }

    }

    @Test
    public void testMeanViewWrapperObtain() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        FrameLayout layout = new FrameLayout(context);
        List<DicItemViewWrapper.MeanViewWrapper> items = Lists.newArrayList(
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout));
        for(DicItemViewWrapper.MeanViewWrapper item : items) {
            assertTrue(!item.isRecycled());
        }
        for(DicItemViewWrapper.MeanViewWrapper item : items) {
            assertTrue(!item.isRecycled());
            item.recycle();
        }
        List<DicItemViewWrapper.MeanViewWrapper> newItems = Lists.newArrayList(
                DicItemViewWrapper.MeanViewWrapper.obtain(context, layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout),
                DicItemViewWrapper.MeanViewWrapper.obtain(context,layout));

        for(int i = 0, n = newItems.size(); i < n; ++i) {
            DicItemViewWrapper.MeanViewWrapper itemsNew =  newItems.get(n - i - 1);
            DicItemViewWrapper.MeanViewWrapper itemsOld =  items.get(i);
            assertTrue(!itemsNew.isRecycled());
            assertTrue(!itemsOld.isRecycled());
            assertEquals(itemsNew, itemsOld);
        }
    }


    @Test
    public void testVerifyView() throws IllegalAccessException {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        DicItemViewWrapper dicItemViewWrapper = DicItemViewWrapper.obtain(context);
        DicItemViewWrapper.recycleAll();

        FrameLayout layout =  new FrameLayout(context);
        DicItemViewWrapper.MeanViewWrapper meanViewWrapper =  DicItemViewWrapper.MeanViewWrapper.obtain(context, layout);
        assertTrue(verifyView(meanViewWrapper, "mParent") > 0);
        List<DicItemViewWrapper.MeanViewWrapper> list = Arrays.asList(DicItemViewWrapper.MeanViewWrapper.obtain(context, layout),
        DicItemViewWrapper.MeanViewWrapper.obtain(context, layout),
        DicItemViewWrapper.MeanViewWrapper.obtain(context, layout),
        DicItemViewWrapper.MeanViewWrapper.obtain(context, layout));
        assertEquals(layout.getChildCount(), 5);
        meanViewWrapper.recycle();
        for(DicItemViewWrapper.MeanViewWrapper mv : list) {
            mv.recycle();
        }
        assertEquals(layout.getChildCount(), 0);

    }

    public int verifyView(Object obj, String... noVerifyFieldName) throws IllegalAccessException {
        Class<?> clz = obj.getClass();
        List<Field> fieldList = Lists.newArrayList();
        int resule = 0;
        while(!clz.equals(Object.class)) {
            Collections.addAll(fieldList, clz.getDeclaredFields());
            clz = clz.getSuperclass();
        }
        for(Field field : fieldList) {
            if(View.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                Object view =  field.get(obj);
                if(!Arrays.asList(noVerifyFieldName).contains(field.getName())) {
                    if (view == null) throw new NullPointerException(field.getName() + " is Null");
                }
                else resule++;
            }
        }
        return resule;
    }

}