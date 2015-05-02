package kr.re.dev.MoongleDic;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import kr.re.dev.MoongleDic.Commons.ProgressEvent;
import kr.re.dev.MoongleDic.DicData.Database.KeyWordColumns;
import kr.re.dev.MoongleDic.DicData.Migration.Ld2Migrator;
import kr.re.dev.MoongleDic.DicData.Database.WordColumns;
import rx.Observable;

/**
 * Created by ice3x2 on 15. 4. 24..
 */
public class InputRealmDBTest  extends ApplicationTestCase<Application> {


    public InputRealmDBTest() {
        super(Application.class);
    }


    public void testInputDB() {
        Context context =  getContext();
        String file = "TXT_To_DB_Text.txt";
        String db = "TXT_To_DB_Text.db";
        File df = new File(context.getFilesDir(), db);
        if(df.isFile()) {
            assertTrue(df.delete());
        }
        CountDownLatch latch = new CountDownLatch(1);
        Observable<ProgressEvent> observable =   Ld2Migrator.newInstance().migrateFromTextFile(context, file);
        observable
                .subscribe(event -> {
                    Log.d("testio", event.getProgress() + "%");
                    if (!event.isComplete()) {
                        Log.d("testio", event.getProgress() + "%");
                    } else {
                        Log.d("testio", "OK!!");
                        latch.countDown();
                    }
                });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Realm realm =  Realm.getInstance(context, db);

        RealmResults<KeyWordColumns> keyResults =  realm.where(KeyWordColumns.class).findAll();
        for(int i= 0, n = keyResults.size();i < n; ++i) {
            Log.i("testio","keyword : " + keyResults.get(i).getKeyword());
        }
        assertEquals(keyResults.size(), 8);
        RealmResults<WordColumns> wordResults =  realm.where(WordColumns.class).findAll();
        for(int i= 0, n = wordResults.size();i < n; ++i) {
            Log.i("testio","word : " + wordResults.get(i).getWord());
        }
        assertEquals(wordResults.size(), 5);

        KeyWordColumns keyWordColumns = realm.where(KeyWordColumns.class).equalTo("keyword","Combined", false).findFirst();
        assertTrue(!keyWordColumns.isBase());
        assertEquals(keyWordColumns.getDicName(), db);
        assertEquals(keyWordColumns.getRefs(), 1);
        RealmList<WordColumns> wordColumnses =  keyWordColumns.getWords();
        assertEquals(wordColumnses.get(0).getWord(), "combine");
        assertEquals(wordColumnses.get(0).getDescription(), "DFSDF@RASDFSADFASFDEQWFSADFASF");


        keyWordColumns = realm.where(KeyWordColumns.class).equalTo("keyword","truth", false).findFirst();
        assertTrue(!keyWordColumns.isBase());
        assertEquals(keyWordColumns.getDicName(), db);
        assertEquals(keyWordColumns.getRefs(), 2);
        wordColumnses =  keyWordColumns.getWords();
        assertEquals(wordColumnses.size(), 2);
        assertEquals(wordColumnses.get(0).getWord(), "true");
        assertEquals(wordColumnses.get(0).getDescription(), "@!$DFWASDF@#$DSGASDFASDF");
        assertEquals(wordColumnses.get(1).getWord(), "truing");
        assertEquals(wordColumnses.get(1).getDescription(), "@#$DSFWERFSADFSAFDWQEFDSFASF");


        keyWordColumns = realm.where(KeyWordColumns.class).equalTo("keyword","truing", false).findFirst();
        assertTrue(keyWordColumns.isBase());
        assertEquals(keyWordColumns.getDicName(), db);
        assertEquals(keyWordColumns.getRefs(), 1);
        wordColumnses =  keyWordColumns.getWords();
        assertEquals(wordColumnses.size(), 2);
        assertEquals(wordColumnses.get(0).getWord(), "truing");
        assertEquals(wordColumnses.get(0).getDescription(), "@#$DSFWERFSADFSAFDWQEFDSFASF");
        assertEquals(wordColumnses.get(1).getWord(), "true");
        assertEquals(wordColumnses.get(1).getDescription(), "@!$DFWASDF@#$DSGASDFASDF");

        keyWordColumns = realm.where(KeyWordColumns.class).equalTo("keyword","combine", false).findFirst();
        assertTrue(keyWordColumns.isBase());
        assertEquals(keyWordColumns.getDicName(), db);
        assertEquals(keyWordColumns.getRefs(), 0);
        wordColumnses =  keyWordColumns.getWords();
        assertEquals(wordColumnses.size(), 1);
        assertEquals(wordColumnses.get(0).getWord(), "combine");
        assertEquals(wordColumnses.get(0).getDescription(), "DFSDF@RASDFSADFASFDEQWFSADFASF");


        realm.close();

        df.delete();

        /*

        File fromFile = new File(context.getFilesDir(), "Vicon English-Korean Dictionary.db");
        File toFile = new File(Environment.getExternalStorageDirectory(), "Vicon English-Korean Dictionary.db");
        try {
            Files.copy(fromFile, toFile);
        } catch (IOException e) {
            fail(e.toString());
            e.printStackTrace();
        }*/

    }

}

/**
 8

 word
 [base]
 sdF#$SFSARWSDF@#$@#$WSAASDF
 0

 toast
 [base]
 asdwefwfsdfaf
 0

 true
 [base]
 @!$DFWASDF@#$DSGASDFASDF
 0

 truing
 [base]
 @#$DSFWERFSADFSAFDWQEFDSFASF
 1
 true

 words
 [derivation]
 1
 word

 truth
 [derivation]
 2
 true
 truing

 combined
 [derivation]
 1
 combine

 combine
 [base]
 DFSDF@RASDFSADFASFDEQWFSADFASF
 1
 combined
 */