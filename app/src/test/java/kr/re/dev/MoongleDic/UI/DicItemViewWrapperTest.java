package kr.re.dev.MoongleDic.UI;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.common.collect.Lists;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import kr.re.dev.MoongleDic.DicData.Database.DicInfoManager;
import kr.re.dev.MoongleDic.DicData.WordCard;
import kr.re.dev.MoongleDic.R;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by ice3x2 on 15. 4. 30..
 */

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class DicItemViewWrapperTest {

    @Test
    public void testLineBreak() {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        DicItemViewWrapper item = DicItemViewWrapper.obtain(context);
        WordCard wordCard = mock(WordCard.class);
        when(wordCard.getDescriptionCards()).thenReturn(Lists.newArrayList());
        when(wordCard.word()).thenReturn("하하하하히히히");
        when(wordCard.phonetic()).thenReturn("[ 발음기호 || 발음기호 ]");
        item.setWordCard(wordCard);
        assertTrue(((LinearLayout) item.getView()).indexOfChild(item.findViewById(R.id.textViewPhonetic)) >= 0);
    }


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
        List<MeanItemWrapper> items = Lists.newArrayList(
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout));
        for(MeanItemWrapper item : items) {
            assertTrue(!item.isRecycled());
        }
        for(MeanItemWrapper item : items) {
            assertTrue(!item.isRecycled());
            item.recycle();
        }
        List<MeanItemWrapper> newItems = Lists.newArrayList(
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout),
                MeanItemWrapper.obtain(context, layout));

        for(int i = 0, n = newItems.size(); i < n; ++i) {
            MeanItemWrapper itemsNew =  newItems.get(n - i - 1);
            MeanItemWrapper itemsOld =  items.get(i);
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
        MeanItemWrapper meanItemWrapper =  MeanItemWrapper.obtain(context, layout);
        assertTrue(verifyView(meanItemWrapper, "mParent") > 0);
        List<MeanItemWrapper> list = Arrays.asList(MeanItemWrapper.obtain(context, layout),
        MeanItemWrapper.obtain(context, layout),
        MeanItemWrapper.obtain(context, layout),
        MeanItemWrapper.obtain(context, layout));
        assertEquals(layout.getChildCount(), 5);
        meanItemWrapper.recycle();
        for(MeanItemWrapper mv : list) {
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
                    else resule++;
                }
            }
        }
        return resule;
    }

}