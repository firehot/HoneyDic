package kr.re.dev.MoogleDic.DicData;

import android.app.Application;
import android.content.Context;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import kr.re.dev.MoogleDic.DicData.Database.WordColumns;
import rx.Observable;




/**
 * 사전 검색 테스트.
 * 이슈 : 단어로 DB 에서 뜻 검색하기 [3+] #2
 *
 * Created by ice3x2 on 15. 4. 16..
 */
public class DicSearcherTest extends ApplicationTestCase<Application> {
    public DicSearcherTest() {
        super(Application.class);
    }





    public void  testSearchFor_Vicon_English_Korean_Dictionary() {

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
        dicSearcher.eventFromLoadDB()
                .subscribe(event -> {
                    if (event.isError()) {
                        fail(event.toString());
                        latch.countDown();
                    }

                    Log.d("testio", event.getProgress() + "%");
                });

        Log.d("testio", "search test");
        // 원형 및 레퍼런스 단어 테스트.
        dicSearcher.search("\r    boxing \t\t   \n").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(wordCardList -> {
            WordCard wordCard = wordCardList.get(0);
            assertEquals(wordCard.word(), "boxing");
            assertEquals(wordCard.phonetic(), "box·ing || 'bɒksɪŋ");
            List<DescriptionCard> descriptionCardList = wordCard.getDescriptionCards();
            DescriptionCard descriptionCard = descriptionCardList.get(0);
            assertEquals(descriptionCard.wordClass(), "n.");
            assertEquals(descriptionCard.meaning(), " 권투, 두사람이 치고 받는 운동");

            wordCard = wordCardList.get(1);
            assertEquals(wordCard.raw(), "<C><F><H><M>bɒks</M></H><I><N><U>n.</U> 통; 상자; 칸막이한 좌석; 손바닥으로 침, 주먹으로 침;텔레비젼</N></I><I><N><U>v.</U> 주먹질 하며 싸우다; 박스에 넣다</N></I></F></C>");
            assertEquals(wordCard.word(), "box");
            assertEquals(wordCard.phonetic(), "bɒks");
            descriptionCardList = wordCard.getDescriptionCards();
            descriptionCard = descriptionCardList.get(0);
            assertEquals(descriptionCard.wordClass(), "n.");
            assertEquals(descriptionCard.meaning(), " 통; 상자; 칸막이한 좌석; 손바닥으로 침, 주먹으로 침;텔레비젼");
            descriptionCard = descriptionCardList.get(1);
            assertEquals(descriptionCard.wordClass(), "v.");
            assertEquals(descriptionCard.meaning(), " 주먹질 하며 싸우다; 박스에 넣다");
        });



        dicSearcher.search("\r    box office \t\t   \n").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(wordCardList -> {
            WordCard wordCard = wordCardList.get(0);
            assertEquals(wordCard.word(), "box office");
        });


        dicSearcher.search("\r    trust \t\t   \n").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(wordCardList -> {
            WordCard wordCard = wordCardList.get(0);
            assertEquals(wordCard.word(), "trust");
        });

        dicSearcher.search("\r    Truth \t\t   \n").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(wordCardList -> {
            assertEquals(wordCardList.size(), 2);
            WordCard wordCard = wordCardList.get(0);
            assertEquals(wordCard.word(), "Truth");
        });

        dicSearcher.search("\r    Ladies \t\t   \n").doOnError(e -> {
            StackTraceElement[] stackTraceElements =  e.getStackTrace();
            for(StackTraceElement ste : stackTraceElements) {
                Log.e("testio", ste.toString());
            }
            fail(e.getMessage());
            latch.countDown();
        }).subscribe(wordCardList -> {
            WordCard wordCard = wordCardList.get(0);
            assertEquals(wordCard.word(), "lady");
        });


        dicSearcher.close();

        dicSearcher.search("\r    Trust \t\t   \n").onErrorResumeNext(e -> {
            if(!(e.getCause() instanceof DicSearcher.AlreadyClosedDBException)) {
                fail("not AlreadyClosedDBException");
            }
            latch.countDown();
            return Observable.empty();
        }).subscribe(wordCardList -> {
            fail();
            latch.countDown();
        });




        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

}
