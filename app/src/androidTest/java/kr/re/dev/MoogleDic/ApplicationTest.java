package kr.re.dev.MoogleDic;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.test.ApplicationTestCase;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import kr.re.dev.MoogleDic.DicData.Migration.Ld2Migrator;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {


    public ApplicationTest() {
        super(Application.class);
    }



    public void testDefualt() {
        Ld2Migrator migrator =  Ld2Migrator.newInstance();
        Context context =  getContext();
        /*String file = "Vicon English-Korean Dictionary.ld2";
        CountDownLatch latch = new CountDownLatch(1);
        Observable<Ld2Migrator.MigrationEvent> observable =  Ld2Migrator.newInstance().migrate(context,file);
        observable.observeOn(Schedulers.newThread()).subscribeOn(Schedulers.newThread())
                .doOnError(e -> {
                    Log.d("test", e.getMessage());
                    latch.countDown();
                })
                .subscribe(event -> {
                    Log.d("test",event.getProgress() + "%");
                    if(!event.isComplete()) {
                        Log.d("test", event.getProgress() + "%");
                    }
                    else {
                        Log.d("test", "OK!!");
                        latch.countDown();
                    }
                });


        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        File file = new File(context.getFilesDir(), "Vicon English-Korean Dictionary.db");
        File toFile = new File(Environment.getExternalStorageDirectory(), "Vicon English-Korean Dictionary.db");
        try {
            Files.copy(file, toFile);
        } catch (IOException e) {
            fail(e.toString());
            e.printStackTrace();
        }


    }


}