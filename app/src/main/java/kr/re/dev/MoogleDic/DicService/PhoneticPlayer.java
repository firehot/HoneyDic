package kr.re.dev.MoogleDic.DicService;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import kr.re.dev.MoogleDic.Commons.ProgressEvent;
import rx.Observable;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;

/**
 * 발음을 재생할 때 사용,
 * 만약 발음 파일이 없다면 TTS 를 이용하여 재생한다.
 * Created by ice3x2 on 15. 4. 15..
 */
public class PhoneticPlayer {
    private TextToSpeech mTextToSpeech;
    private Object mMonitor = new Object();
    private AtomicBoolean mPossibleTTS = new AtomicBoolean(false);
    private AtomicBoolean mUseTTS = new AtomicBoolean(true);
    private String mPackageName;
    private Locale mLocale;
    private ReplaySubject<ProgressEvent> mInitTTSSubject = ReplaySubject.create();



    public static PhoneticPlayer newInstance(Context context, Locale locale) {
        return new PhoneticPlayer(context, locale);
    }

    public Observable<ProgressEvent> eventFormInitTTS() {
        return Observable.merge(mInitTTSSubject, Observable.empty()).subscribeOn(Schedulers.computation()).onErrorReturn(ex -> ProgressEvent.obtain(ex));

    }

    private PhoneticPlayer(Context context, Locale locale) {
        mPackageName = context.getPackageName();
        mLocale = locale;
        mTextToSpeech = new TextToSpeech(context,status -> setLocale(status, locale));
    }

    public void useTTS() {
        mPossibleTTS.set(true);

    }
    public void unuseTTS() {
        mPossibleTTS.set(false);
    }

    public void play(String word) {
        if(mPossibleTTS.get() && mUseTTS.get()) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mTextToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, mPackageName);
            else
                mTextToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public boolean isPossibleTTS() {
        return mPossibleTTS.get();
    }


    private void setLocale(int status, Locale locale) {

        mPossibleTTS.set(status == TextToSpeech.SUCCESS &&
                mTextToSpeech.setLanguage(locale) == TextToSpeech.SUCCESS);
        if(status != TextToSpeech.SUCCESS) {
            Exception e =  new Exception("Can not used TTS engine.");
            mInitTTSSubject.onError(e);
        } else {
            mTextToSpeech.setPitch(0.9f);
            //mTextToSpeech.setSpeechRate(0.9f);

        }
        mInitTTSSubject.onNext(ProgressEvent.obtain(100,100,true));
        mInitTTSSubject.onCompleted();
    }

    public void stop() {
        if(mPossibleTTS.get()) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
    }




}
