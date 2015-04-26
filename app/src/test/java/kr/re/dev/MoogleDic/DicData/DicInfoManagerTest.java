package kr.re.dev.MoogleDic.DicData;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.test.ApplicationTestCase;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import kr.re.dev.MoogleDic.DicData.Database.DicInfoManager;

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
    RandomString mRandomString;
    static Locale[] Locales = new Locale[]{Locale.ENGLISH, Locale.KOREAN, Locale.CHINESE, Locale.JAPANESE};
    HashMap<String,DicInfoManager.DicInfo> mMap;

    @Before
    public void setUp() throws Exception {
        Random rand = new Random();
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        Environment.getDataDirectory();
        mRandomString = new RandomString(20);
        mDicInfoManager = DicInfoManager.newInstance(context);
        mMap = new HashMap<>();
        for(int i = 0; i < 10; ++i) {
            DicInfoManager.DicInfo dicInfo =  DicInfoManager.DicInfo.newInstance(mRandomString.nextString(), Locales[rand.nextInt(4)],Locales[rand.nextInt(4)]);
            mMap.put(dicInfo.getDicDBName(), dicInfo);
            mDicInfoManager.pushDicInfo(dicInfo);
            Log.d("testio","Push DicInfo : " + dicInfo.toString());
        }
        mDicInfoManager = DicInfoManager.newInstance(context);
    }


    @After
    public void tearDown() throws Exception {
        mDicInfoManager.clear();
        mMap.clear();
    }

    @Test
    public void testClear() throws  Exception {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        mDicInfoManager.clear();
        mDicInfoManager = DicInfoManager.newInstance(context);
        assertEquals(mDicInfoManager.getDicInfos().size(), 0);
    }

    @Test
    public void testGetDicInfos() throws Exception {
        List<DicInfoManager.DicInfo> dicInfos = mDicInfoManager.getDicInfos();
        for(DicInfoManager.DicInfo dicInfo : dicInfos) {
            DicInfoManager.DicInfo info =  mMap.get(dicInfo.getDicDBName());
            assertEquals(info.getDicDBName(), dicInfo.getDicDBName());
            assertEquals(info.getFromLanguage(), dicInfo.getFromLanguage());
            assertEquals(info.getToLanguage(), dicInfo.getToLanguage());
        }

    }

    @Test
    public void testPushDicInfo() throws Exception {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        Random rand = new Random();
        DicInfoManager.DicInfo dicInfoo =  DicInfoManager.DicInfo.newInstance(mRandomString.nextString(), Locales[rand.nextInt(4)], Locales[rand.nextInt(4)]);
        mMap.put(dicInfoo.getDicDBName(), dicInfoo);
        mDicInfoManager.pushDicInfo(dicInfoo);
        mDicInfoManager = DicInfoManager.newInstance(context);
        List<DicInfoManager.DicInfo> dicInfos = mDicInfoManager.getDicInfos();
        for(DicInfoManager.DicInfo dicInfo : dicInfos) {
            DicInfoManager.DicInfo info =  mMap.get(dicInfo.getDicDBName());
            assertEquals(info.getDicDBName(), dicInfo.getDicDBName());
            assertEquals(info.getFromLanguage(), dicInfo.getFromLanguage());
            assertEquals(info.getToLanguage(), dicInfo.getToLanguage());
        }

    }

    @Test
    public void testRemoveDicInfo() throws Exception {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        Random rand = new Random();
        List<DicInfoManager.DicInfo> infoList = mDicInfoManager.getDicInfos();
        String dbName =  infoList.get(rand.nextInt(infoList.size() - 1)).getDicDBName();
        mDicInfoManager.removeDicInfo(dbName);
        mMap.remove(dbName);
        mDicInfoManager = DicInfoManager.newInstance(context);
        List<DicInfoManager.DicInfo> dicInfos = mDicInfoManager.getDicInfos();
        for(DicInfoManager.DicInfo dicInfo : dicInfos) {
            DicInfoManager.DicInfo info =  mMap.get(dicInfo.getDicDBName());
            assertEquals(info.getDicDBName(), dicInfo.getDicDBName());
            assertEquals(info.getFromLanguage(), dicInfo.getFromLanguage());
            assertEquals(info.getToLanguage(), dicInfo.getToLanguage());
        }
    }


    @Test
    public void testGetSelectedDic() throws Exception {
        Context context = Robolectric.getShadowApplication().getApplicationContext();
        Random rand = new Random();
        List<DicInfoManager.DicInfo> infoList = mDicInfoManager.getDicInfos();
        String dbName =  infoList.get(rand.nextInt(infoList.size() - 1)).getDicDBName();
        mDicInfoManager.selectDic(dbName);
        mDicInfoManager = DicInfoManager.newInstance(context);
        List<DicInfoManager.DicInfo> dicInfos = mDicInfoManager.getDicInfos();
        for(DicInfoManager.DicInfo dicInfo : dicInfos) {
            DicInfoManager.DicInfo infoInMap =  mMap.get(dicInfo.getDicDBName());
            assertEquals(infoInMap.getDicDBName(), dicInfo.getDicDBName());
            assertEquals(infoInMap.getFromLanguage(), dicInfo.getFromLanguage());
            assertEquals(infoInMap.getToLanguage(), dicInfo.getToLanguage());
            if(dicInfo.isSelected()) {
                assertEquals(dicInfo.getDicDBName(), dbName);
            }
        }
        dbName =  infoList.get(rand.nextInt(infoList.size() - 1)).getDicDBName();
        mDicInfoManager.selectDic(dbName);
        mDicInfoManager = DicInfoManager.newInstance(context);
        dicInfos = mDicInfoManager.getDicInfos();
        for(DicInfoManager.DicInfo dicInfo : dicInfos) {
            DicInfoManager.DicInfo infoInMap =  mMap.get(dicInfo.getDicDBName());
            assertEquals(infoInMap.getDicDBName(), dicInfo.getDicDBName());
            assertEquals(infoInMap.getFromLanguage(), dicInfo.getFromLanguage());
            assertEquals(infoInMap.getToLanguage(), dicInfo.getToLanguage());
            if(dicInfo.isSelected()) {
                assertEquals(dicInfo.getDicDBName(), dbName);
            }
            Log.d("testio","select test for DicInfo : " + dicInfo.toString());
        }


    }



    // 출처 : http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
    public static class RandomString {

        private static final char[] symbols;

        static {
            StringBuilder tmp = new StringBuilder();
            for (char ch = '0'; ch <= '9'; ++ch)
                tmp.append(ch);
            for (char ch = 'a'; ch <= 'z'; ++ch)
                tmp.append(ch);
            symbols = tmp.toString().toCharArray();
        }

        private final Random random = new Random();

        private final char[] buf;

        public RandomString(int length) {
            if (length < 1)
                throw new IllegalArgumentException("length < 1: " + length);
            buf = new char[length];
        }

        public String nextString() {
            for (int idx = 0; idx < buf.length; ++idx)
                buf[idx] = symbols[random.nextInt(symbols.length)];
            return new String(buf);
        }
    }

}