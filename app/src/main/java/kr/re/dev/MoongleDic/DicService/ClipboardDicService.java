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

public class ClipboardDicService extends Service {

    private WordCardToast mWordCardToast;
    private ClipboardManager mClipboardManager;
    private PhoneticPlayer mPhoneticPlayer;
    private DicSearcher mDicSearcher;
    private DicInfoManager.DicInfo mDicInfo;
    private String mCurrentKeyword = "";
    private ChangedSettingsReceiver mChangedSettingsReceiver;
    private Settings mSettings;
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
        return super.onUnbind(intent);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        HashMap<Integer,String> map = Maps.newHashMap();
         map.put(15,"TRIM_MEMORY_RUNNING_CRITICAL (Service)" );
          map.put(10,"TRIM_MEMORY_RUNNING_LOW  (Service)" );
        map.put(5,"TRIM_MEMORY_RUNNING_MODERATE  (Service)" );
        map.put(20,"TRIM_MEMORY_UI_HIDDEN  (Service)" );
        String msg = map.get(level);
        if(Strings.isNullOrEmpty(msg)) msg = level + "";
       Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
        setSetting(Settings.getSettings(context));
    }


    private void release() {
        if(!mIsInit) return;
        mIsInit = false;
        mPhoneticPlayer.close();
        mDicSearcher.close();
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
        Toast.makeText(getApplicationContext(), (hide == WordCardToast.HideDirection.Left)?"왼쪽":"오른쪽", Toast.LENGTH_SHORT).show();
        mPhoneticPlayer.stop();
        Log.i("testio", (Debug.getNativeHeapAllocatedSize() / 1024.0f / 1024.0f) + "Mb");
        mWordCardToast = null;
    }

    private void setSetting(Settings setting) {
        mSettings = setting;
        mPhoneticPlayer.useTTS(setting.isUseTTS());

        if(setting.isWordCardNoneForceClose() && setting.isUseClipboardDic()) enableNoneForceClose();
        else disableNoneForceClose();

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
