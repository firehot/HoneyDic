package kr.re.dev.MoogleDic.DicService;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import kr.re.dev.MoogleDic.SingleSchedulers;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 발음을 재생할 때 사용,
 * 만약 발음 파일이 없다면 TTS 를 이용하여 재생한다.
 * Created by ice3x2 on 15. 4. 15..
 */
public class PhoneticPlayer {

    private AtomicBoolean mPossibleTTS = new AtomicBoolean(false);
    private AtomicBoolean mIsTTSShoutdown = new AtomicBoolean(false);
    private String mPackageName;
    private Locale mLocale;

    private  Observable<TextToSpeech> mTextToSpeechObserable;


    public static PhoneticPlayer newInstance(Context context, Locale locale) {
        return new PhoneticPlayer(context, locale);
    }


    private PhoneticPlayer(Context context, Locale locale) {

        mPackageName = context.getPackageName();
        mLocale = locale;
        mTextToSpeechObserable = Observable.create((Observable.OnSubscribe<TextToSpeech>) sub -> {
            initTTS(context,locale, sub);
        }).subscribeOn(SingleSchedulers.singleThread()).observeOn(AndroidSchedulers.mainThread())
        .cache();
        mTextToSpeechObserable.subscribe();
    }

    private void initTTS(Context context,Locale locale, Subscriber sub) {
        final AtomicReference<TextToSpeech> ttsRef = new AtomicReference<>();
        CountDownLatch lock = new CountDownLatch(1);
        ttsRef.set(new TextToSpeech(context, status -> {
            new Thread(() ->{
                setLocale(ttsRef.get(), status, locale);
                lock.countDown();
            }).start();
        }));

       try {
            int timeout = 3000;
            lock.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("testio", "Created TTS Engine : " + ttsRef.get().toString() +  "/" + Thread.currentThread().getId());
        sub.onNext(ttsRef.get());
        sub.onCompleted();
    }

    private boolean setLocale(TextToSpeech tts, int status, Locale locale) {
        if(status != TextToSpeech.SUCCESS || tts.setLanguage(locale) != TextToSpeech.SUCCESS) {
            return false;
        } else {
            tts.setPitch(0.9f);
        }
        mPossibleTTS.set(true);
        return  true;
    }


    public void play(String word) {
        Log.i("testio", "call play TTS : " + word);
        mTextToSpeechObserable
        .onErrorResumeNext(e -> Observable.empty())
        .subscribe(tts -> playTTS(tts, word));
    }


    public boolean playTTS(TextToSpeech tts, String word) {
        if(tts == null) {
            return false;
        }
        Log.i("testio", "play tts : " + word);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, mPackageName);
        else
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null);
        return true;
    }

    public boolean isPossibleTTS() {
        return mPossibleTTS.get();
    }


    public boolean isTTSShoutdown() {
        return mIsTTSShoutdown.get();
    }


    public void stop() {
        mTextToSpeechObserable
        .onErrorResumeNext(e -> Observable.empty())
        .subscribe(tts -> {
            tts.stop();
            tts.shutdown();
            mIsTTSShoutdown.set(true);
        });
    }




}
