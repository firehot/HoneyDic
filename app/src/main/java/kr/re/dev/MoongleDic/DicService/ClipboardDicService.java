package kr.re.dev.MoongleDic.DicService;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Debug;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;

import kr.re.dev.MoongleDic.Constants;
import kr.re.dev.MoongleDic.DicData.Database.DicInfoManager;
import kr.re.dev.MoongleDic.DicData.DicSearcher;
import kr.re.dev.MoongleDic.DicData.LocaleWordRefiner;
import kr.re.dev.MoongleDic.DicData.WordCard;
import kr.re.dev.MoongleDic.MainActivity;
import kr.re.dev.MoongleDic.R;
import kr.re.dev.MoongleDic.UI.WordCardToast;
import rx.Observable;

/**
 *  클립보드 단어사전 HoenyDic::ChangedSettingsReceiver class.
 *  클립보드 영어 사전 서비스. 안드로이드 시스템의 백그라운드에서 클립보드 이벤트를 받고 단어 뜻을 단어 카드 토스트(WordCardToast) 를 통하여 보여준다.
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
public class ClipboardDicService extends Service {


    private ClipboardManager mClipboardManager;
    private PhoneticPlayer mPhoneticPlayer;
    private DicSearcher mDicSearcher;
    private DicInfoManager.DicInfo mDicInfo;
    private String mCurrentKeyword = "";
    private ChangedSettingsReceiver mChangedSettingsReceiver;
    private Settings mSettings;
    private SoundEffectPlayer mSoundEffectPlayer;
    private boolean mIsInit = false;
    private boolean misArt;



    @Override
    public void onCreate() {
        Log.i("testio", "create : ClipboardDicService");
        init();
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("testio", "Start : ClipboardDicService   flags : " + flags +  "  startID : "  + startId + " this : " + this);
        Log.i("testio", " intent : " + intent);
        init();

        Log.i("testio", " isUseClipboardDic : " + mSettings.isUseClipboardDic());
        return (mSettings.isUseClipboardDic())?START_STICKY:START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.i("testio", "bind : ClipboardDicService");
        return new Handler();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i("testio", "rebind : ClipboardDicService");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("testio", "onUnbind : ClipboardDicService");
        if(!mSettings.isUseClipboardDic()) {
            stopSelf();
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
//        HashMap<Integer,String> map = Maps.newHashMap();
//         map.put(15,"TRIM_MEMORY_RUNNING_CRITICAL (Service)" );
//          map.put(10,"TRIM_MEMORY_RUNNING_LOW  (Service)" );
//        map.put(5,"TRIM_MEMORY_RUNNING_MODERATE  (Service)" );
//        map.put(20,"TRIM_MEMORY_UI_HIDDEN  (Service)" );
//        String msg = map.get(level);
//        if(Strings.isNullOrEmpty(msg)) msg = level + "";
//       Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("testio", "remove task");
        if(!mSettings.isWordCardNoneForceClose()) {
            release();
        }


        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.i("testio", "destory ClipboardDicService");
        release();
        super.onDestroy();
    }


    private void disableNoneForceClose() {
        stopForeground(true);
    }

    private void enableNoneForceClose() {

        Bitmap icon = BitmapFactory.decodeResource(getResources(),  R.mipmap.ic_launcher);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent =  PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.Notification_Title))
                .setTicker(getResources().getString(R.string.Notification_Title))
                .setContentText(getResources().getString(R.string.Notification_Content))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(Constants.NOTIFICATION_ID, notification);
    }


    private void init() {
        if(mIsInit) return;
        mIsInit = true;
        Context context = getApplicationContext();
        mDicInfo= DicInfoManager.newInstance(context).getDicInfo(DicInfoManager.Dic.EnglishToKorean);
        mDicSearcher = DicSearcher.newInstance(context, mDicInfo.getDicDBName());
        mPhoneticPlayer = PhoneticPlayer.newInstance(context,mDicInfo.getFromLanguage());
        mClipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        mChangedSettingsReceiver = new ChangedSettingsReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION.BROADCAST_SETTING);
        mChangedSettingsReceiver.settingChangedEvent().subscribe(this::setSetting);
        registerReceiver(mChangedSettingsReceiver, intentFilter);
        misArt =  System.getProperty("java.vm.version").matches("^(2[.]).*");
        mSoundEffectPlayer = SoundEffectPlayer.getInstance(this);
        setSetting(Settings.getSettings(context));
    }


    private void release() {
        if(!mIsInit) return;
        mIsInit = false;
        mPhoneticPlayer.close();
        mDicSearcher.close();
        mSoundEffectPlayer.release();
        unregisterReceiver(mChangedSettingsReceiver);
        mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }


    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                Log.i("testio", "onPrimaryClipChanged");
                String text = getTextOnClipboard();
                String keyWord = LocaleWordRefiner.refine(text, mDicInfo.getFromLanguage());
                // 다른 애플리케이션의 클립보드 조작(?)으로
                // 이미 출력된 단어 카드의 중복 출력을 방지한다.
                if (!mCurrentKeyword.equals(keyWord)) {
                    mCurrentKeyword = keyWord;
                    showWordToast(keyWord);
                }
            }

            private String getTextOnClipboard() {
                ClipData data = mClipboardManager.getPrimaryClip();
                if (data.getItemCount() <= 0) return "";
                Object textItem = data.getItemAt(0).getText();
                if (textItem != null) {
                    return textItem.toString();
                }
                return "";
            }

        };


    private void showWordToast(String word) {
        WordCardToast wordCardToast = WordCardToast.newInstance(getApplicationContext());
        searchWord(word).flatMap(words -> wordCardToast.show(words, mSettings.getWordCardKeepTime())).subscribe(this::endWordCard);
        wordCardToast.getSelectWordEvent().subscribe(mPhoneticPlayer::play);
    }

    private Observable<List<WordCard>> searchWord(String word) {
        return Observable.just(word)
                .doOnNext(this::playTTS)
                .flatMap(mDicSearcher::search)
                .zipWith(Lists.newArrayList(word), (wordCards, comWord) -> {
                    if (wordCards.isEmpty() && !comWord.isEmpty())
                        wordCards.add(WordCard.newInstance(comWord));
                    return wordCards;
         });
    }

    private void playTTS(String word) {
        mPhoneticPlayer.play(word);
    }


    private void endWordCard(WordCardToast.HideDirection hide) {
        mCurrentKeyword = "";
        mSoundEffectPlayer.play(SoundEffectPlayer.SoundEffect.TrashWordCard);
        Toast.makeText(getApplicationContext(), (hide == WordCardToast.HideDirection.Left)?"왼쪽":"오른쪽", Toast.LENGTH_SHORT).show();
        mPhoneticPlayer.stop();
        Log.i("testio", (Debug.getNativeHeapAllocatedSize() / 1024.0f / 1024.0f) + "Mb");
    }

    private void setSetting(Settings setting) {
        mSettings = setting;
        mPhoneticPlayer.useTTS(setting.isUseTTS());
        if(setting.isWordCardNoneForceClose() && setting.isUseClipboardDic())
            enableNoneForceClose();
        else
            disableNoneForceClose();

        if(setting.isSoundEffect() && mSoundEffectPlayer.isReleased())
            mSoundEffectPlayer = SoundEffectPlayer.getInstance(getApplicationContext());
        else if(!setting.isSoundEffect())
            mSoundEffectPlayer.release();

        Intent intentBootBroadcast = new Intent(BootReceiver.ACTION_START_CLIPBOARDDIC);
        sendBroadcast(intentBootBroadcast);
    }

    public class Handler extends Binder {
        /**
         * null 또는 empty string 을 넣으면 stop 을 호출한다.
         * @param word
         */
        public void playPhonetic(String word) {
            if(Strings.isNullOrEmpty(word)) {
                mPhoneticPlayer.stop();
            } else {
                mPhoneticPlayer.play(word);
            }
        }
        public Observable<List<WordCard>> searchWord(String word) {
            return mDicSearcher.search(word);
        }
    }


}
