package kr.re.dev.MoogleDic.DicService;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;


import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import rx.schedulers.Schedulers;

/**
 * Created by ice3x2 on 15. 4. 22..
 * 단어 발음 재생 모듈 테스트.
 * 이 녀석은... 테스트 하기 좀 애매한데...
 * 한 번 돌려보고 귀로 듣는다.ㅎㅎ
 */
public class PhoneticPlayerTest extends ApplicationTestCase<Application> {
    public PhoneticPlayerTest() {
        super(Application.class);
    }

    PhoneticPlayer phoneticPlayer;


    public void setUp() throws Exception {
        long startTime = System.currentTimeMillis();
        phoneticPlayer = PhoneticPlayer.newInstance(getContext(), Locale.ENGLISH);
        CountDownLatch latch = new CountDownLatch(1);
        phoneticPlayer.eventFormInitTTS().observeOn(Schedulers.newThread()).doOnError(e -> Log.i("testio",e.getMessage())).doOnCompleted(() -> {
            latch.countDown();
            Log.d("testio", (System.currentTimeMillis() - startTime) + "ms");
        }).subscribe();
        latch.await();
    }


    public void tearDown() throws Exception {
        phoneticPlayer.stop();
    }

    public void testPlayEnglishTTS()throws Exception  {
        assertTrue(phoneticPlayer.isPossibleTTS());

        phoneticPlayer.useTTS();
        phoneticPlayer.play("name");
        phoneticPlayer.play("hohoho");

        int i = 0;



    }

    public void testPlayEnglishNoneTTS() throws Exception {
        //phoneticPlayer.unuseTTS();
        phoneticPlayer.play("name");
        phoneticPlayer.play("hohoho");

        int i = 0;
    }
}