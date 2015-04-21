package kr.re.dev.MoogleDic.DicData;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;


/**
 * Created by ice3x2 on 15. 4. 16..
 */
public class DicSearcherTest extends ApplicationTestCase<Application> {
    public DicSearcherTest() {
        super(Application.class);
    }

    public void  testSearch() {
       Context context =  getContext();
        File dbFile = new File(context.getFilesDir(), "Vicon English-Korean Dictionary.db");
        dbFile.delete();

        CountDownLatch latch = new CountDownLatch(1);
        DicSearcher dicSearcher = null;
        try {
            dicSearcher = DicSearcher.newInstance(context, "Vicon English-Korean Dictionary.db");
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        dicSearcher.eventFromLoadDB().doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();}).subscribe(event -> {
            Log.d("testio", event.getProgress() + "%");
            if (event.getProgress() >= 99) {
                //latch.countDown();
            }
        });

        dicSearcher.search("\r    taken \t\t   \n").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(dicDBColumn -> {
            assertNotNull(dicDBColumn.getDescription());
            assertNotNull(dicDBColumn.getWord());
            assertNotNull(dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getWord());
            Log.d("testio", dicDBColumn.getDescription());
        });


        dicSearcher.search("\r    name \t\t   \n").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(dicDBColumn -> {
            assertNotNull(dicDBColumn.getDescription());
            assertNotNull(dicDBColumn.getWord());
            assertNotNull(dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getWord());
            Log.d("testio", dicDBColumn.getDescription());
        });

        dicSearcher.search("\r    fetch \t\t   \n").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(dicDBColumn -> {
            assertNotNull(dicDBColumn.getDescription());
            assertNotNull(dicDBColumn.getWord());
            assertNotNull(dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getWord());
            Log.d("testio", dicDBColumn.getDescription());
        });

        dicSearcher.search("\r    HOLD \t\t   \n").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(dicDBColumn -> {
            assertNotNull(dicDBColumn.getDescription());
            assertNotNull(dicDBColumn.getWord());
            assertNotNull(dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getWord());
            Log.d("testio", dicDBColumn.getDescription());
        });

        dicSearcher.search("SensitivE ").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(dicDBColumn -> {
            assertNotNull(dicDBColumn.getDescription());
            assertNotNull(dicDBColumn.getWord());
            assertNotNull(dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getWord());
            Log.d("testio", dicDBColumn.getDescription());
        });

        dicSearcher.search("sdfsdaf").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(dicDBColumn -> {
            assertNotNull(dicDBColumn.getWord());
            Log.d("testio", dicDBColumn.getDicName());
            Log.d("testio", dicDBColumn.getWord());
            Log.d("testio", dicDBColumn.getDescription());
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
