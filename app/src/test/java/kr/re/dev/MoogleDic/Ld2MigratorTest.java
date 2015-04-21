package kr.re.dev.MoogleDic;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import kr.re.dev.MoogleDic.DicData.DicDataDBHelper;
import kr.re.dev.MoogleDic.DicData.Migration.Ld2Migrator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by ice3x2 on 15. 4. 19..
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class Ld2MigratorTest {


    public void testMigrate() throws IOException, InterruptedException {
        Ld2Migrator migrator =  Ld2Migrator.newInstance();
        Context context =  Robolectric.getShadowApplication().getApplicationContext();
        String file = "Vicon English-Korean Dictionary.ld2";

        Object value = SimpleHandler.run(new Call(migrator,"getDBFile", context, file));
        assertNotNull(value);


        /*
        CountDownLatch latch = new CountDownLatch(1);
        Observable<Ld2Migrator.MigrationEvent> observable =  Ld2Migrator.newInstance().migrate(context, file);
        observable.observeOn(Schedulers.newThread()).subscribeOn(Schedulers.newThread())
                .doOnError(e -> {
                    System.out.println(e.getMessage());
                    latch.countDown();
                })
                .subscribe(event -> {
                    if(!event.isComplete()) {
                        System.out.print(event.getProgress() + "%");
                    }
                    else {
                        List<DicDBColumn> list = null;
                        try {
                            list = event.getDicDBHelper().getDao().queryForAll();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            latch.countDown();
                            return;
                        }
                        for(DicDBColumn column : list) {
                            String dicName = column.getDicName();
                            String word = column.getWord();
                            String description = column.getDescription();
                            System.out.print(column.toString());
                            assertThat(description, is(description));
                            assertNotNull(dicName);
                            assertNotNull(word);
                            assertNotNull(description);
                        }
                        latch.countDown();
                    }
                });



        latch.await();
*/

        /*Observable.just(context, "Vicon English-Korean Dictionary.ld2")
                .observeOn(Schedulers.io())*/

    }



}
