package kr.re.dev.MoongleDic.DicService;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.google.common.base.Strings;

import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *  클립보드 영어사전 HoenyDic::PhoneticPlayer class.
 *  단어의 발음을 소리로 재생한다.
 *  Copyright (C) 2015 ice3x2@gmail.com [https://github.com/ice3x2/HoneyDic]
 *  </br></br>
 *
 *  This program is free software:
 *  you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with this program. If not, see < http://www.gnu.org/licenses/ >.
 *
 *  </br></br>
 *  [한글. 번역 출처 : https://wiki.kldp.org/wiki.php/GNU/GPLV3Translation]</br>
 *
 *  이 프로그램은 자유 소프트웨어입니다:
 *  당신은 이것을 자유 소프트웨어 재단이 발표한 GNU 일반 공중 사용허가서의 제3 버전이나 (선택에 따라) 그 이후 버전의 조항 아래 재배포하거나 수정할 수 있습니다.
 *  이 프로그램은 유용하게 쓰이리라는 희망 아래 배포되지만, 특정한 목적에 대한 프로그램의 적합성이나 상업성 여부에 대한 보증을 포함한 어떠한 형태의 보증도 하지 않습니다.
 *  세부 사항은 GNU 일반 공중 사용허가서를 참조하십시오.
 *  당신은 이 프로그램과 함께 GNU 일반 공중 사용허가서를 받았을 것입니다. 만약 그렇지 않다면, < http://www.gnu.org/licenses/ > 를 보십시오.
 *
 */
public class PhoneticPlayer {


    private AtomicBoolean mIsUseTTS = new AtomicBoolean(true);
    private Context mContext;
    private Locale mLocale;
    private TTSManager mTTSManager;


    public static PhoneticPlayer newInstance(Context context, Locale locale) {
        PhoneticPlayer phoneticPlayer =  new PhoneticPlayer(context, locale);
        phoneticPlayer.mContext = context;
        phoneticPlayer.mLocale = locale;
        return phoneticPlayer;
    }

    private PhoneticPlayer() {}

    private PhoneticPlayer(Context context, Locale locale) {
        mLocale = locale;
    }

    public PhoneticPlayer useTTS(boolean useTTS) {
        mIsUseTTS.set(useTTS);
        if(useTTS && (mTTSManager == null || mTTSManager.isShutdown())) {
            mTTSManager = new TTSManager(mContext, mLocale);
        } else if(!useTTS && mTTSManager != null) {
            mTTSManager.close();
            mTTSManager = null;
        }
        return this;
    }

    public void play(String word) {
        useTTS(mIsUseTTS.get());
        if(mTTSManager != null) {
            mTTSManager.play(word);
        }
    }

    public void stop() {
        if(mTTSManager != null) {
            mTTSManager.stop();
        }
    }

    public void close() {
        if(mTTSManager != null) {
            mTTSManager.close();
        }
    }


    private static class TTSManager  {
        Observable<TextToSpeech> textToSpeechObservable;
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AtomicReference<String> mCachedWordRef = new AtomicReference<>("");
        String packageName = "";
        boolean isShutdowns = false;
        boolean isSuccess = false;

        private TTSManager(Context context,Locale locale) {
            this.packageName = context.getPackageName();
            textToSpeechObservable = createInitTTSObserable(context, locale);
            textToSpeechObservable.subscribe();
        }
        private void TTSWorker() {}

        public boolean isShutdown() {
            return isShutdowns;
        }


        private Observable<TextToSpeech> createInitTTSObserable(Context context, Locale locale) {
            return Observable.create((Observable.OnSubscribe<TextToSpeech>) sub -> {
                initTTS(context,locale, sub);
            }).subscribeOn(Schedulers.from(executorService)).observeOn(AndroidSchedulers.mainThread()).cache();
        }


        private void initTTS(Context context,Locale locale, Subscriber sub) {
            final AtomicReference<TextToSpeech> ttsRef = new AtomicReference<>();
            Log.i("testio", "Create TTS Engine start.");
            CountDownLatch lock = new CountDownLatch(1);
            AtomicBoolean isSuccess = new AtomicBoolean(false);
            ttsRef.set(new TextToSpeech(context, status -> {
                new Thread(() -> {
                    isSuccess.set(setLocale(ttsRef.get(), status, locale));
                    lock.countDown();
                }).start();
            }));
            try {
                int timeout = 3000;
                lock.await(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.isSuccess = isSuccess.get();
            Log.i("testio", "Created TTS Engine.");
            sub.onNext(ttsRef.get());
            sub.onCompleted();
            if(!Strings.isNullOrEmpty(mCachedWordRef.get())) play(mCachedWordRef.get());
        }

        public void close() {
            isShutdowns = true;
            textToSpeechObservable
                    .observeOn(Schedulers.from(executorService))
                    .onErrorResumeNext(e -> Observable.empty())
                    .subscribe(tts -> {
                        try {
                            tts.stop();
                            tts.shutdown();
                            Log.i("testio", "Shutdown TTS");
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.i("testio", "Shutdown TTS Error : " + e.toString());
                        }
                        executorService.shutdown();
                    });
            textToSpeechObservable = null;
        }

        public void play(String word) {
            if(isShutdown()) return;
            if(!isSuccess) {
                mCachedWordRef.set(word);
                return;
            }
            textToSpeechObservable.subscribe(tts -> playTTS(tts,word));
        }

        public void stop() {
            if(isShutdown()) return;
            textToSpeechObservable.subscribe(TextToSpeech::stop);
        }

        private boolean setLocale(TextToSpeech tts, int status, Locale locale) {
            if(status != TextToSpeech.SUCCESS || tts.setLanguage(locale) != TextToSpeech.SUCCESS) {
                return false;
            } else {
                tts.setPitch(0.9f);
            }
            return  true;
        }

        private boolean playTTS(TextToSpeech tts, String word) {
            if(tts == null) {
                return false;
            }
            Log.i("testio", "play tts : " + word);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, packageName);
            else
                tts.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            return true;
        }

    }




}
