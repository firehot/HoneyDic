
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;

import kr.re.dev.MoogleDic.DicData.DicSearcher;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by ice3x2 on 15. 4. 19..
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18, manifest = "./app/src/main/AndroidManifest.xml")
public class RoboTest  {

    DicSearcher mDicSearcher;

    @Before
    public void setUp() throws Exception {
        mDicSearcher = new DicSearcher();
    }

    @Test
    public void testSearch() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Observable<Integer> observable = Observable.create((Observable.OnSubscribe<Integer>) sub -> {
            for(int i = 0; i < 100; ++i) {
                sub.onNext(i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).observeOn(Schedulers.newThread()).subscribeOn(Schedulers.newThread());
        observable.subscribe(s -> {
           System.out.println(s);
            if(s >= 99) latch.countDown();

        });

        latch.await();


    }
}
