package kr.re.dev.MoongleDic.DicService;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;

import kr.re.dev.MoongleDic.DicData.Database.DicInfoManager;
import kr.re.dev.MoongleDic.DicData.DicSearcher;
import kr.re.dev.MoongleDic.DicData.LocaleWordRefiner;
import kr.re.dev.MoongleDic.DicData.WordCard;
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
    private boolean misArt;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("testio", "Start ClipboardDicService" );
        init();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        HashMap<Integer,String> map = Maps.newHashMap();
//        map.put(15,"TRIM_MEMORY_RUNNING_CRITICAL (Service)" );
//        map.put(10,"TRIM_MEMORY_RUNNING_LOW  (Service)" );
//        map.put(5,"TRIM_MEMORY_RUNNING_MODERATE  (Service)" );
//        map.put(20,"TRIM_MEMORY_UI_HIDDEN  (Service)" );
//        Toast.makeText(getApplicationContext(), map.get(level), Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onCreate() {
        super.onCreate();

    }

    private void init() {
        Context context = getApplicationContext();
        mDicInfo= DicInfoManager.newInstance(context).getDicInfo(DicInfoManager.Dic.EnglishToKorean);
        mDicSearcher = DicSearcher.newInstance(context, mDicInfo.getDicDBName());
        mPhoneticPlayer = PhoneticPlayer.newInstance(context,mDicInfo.getFromLanguage());
        mClipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        mChangedSettingsReceiver = new ChangedSettingsReceiver();
        IntentFilter intentFilter = new IntentFilter(ChangedSettingsReceiver.ACTION_SETTING);
        mChangedSettingsReceiver.settingChangedEvent().subscribe(this::setSetting);
        registerReceiver(mChangedSettingsReceiver, intentFilter);
        misArt =  System.getProperty("java.vm.version").matches("^(2[.]).*");
        setSetting(Settings.getSettings(context));
    }


    @Override
    public void onDestroy() {
        mPhoneticPlayer.close();
        mDicSearcher.close();
        unregisterReceiver(mChangedSettingsReceiver);
        mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        Log.i("testio", "destory ClipboardDicService");
        super.onDestroy();
    }



    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
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
        searchWord(word).flatMap(wordCardToast::show).subscribe(this::endWordCard);
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
        // 이러면 안 되는데... ㅡ , ㅡa
        // 괜히 쓰고 싶다.
        //if(!misArt) {
            System.gc();
        //}
    }

    private void setSetting(Settings setting) {
        mPhoneticPlayer.useTTS(setting.isUseTTS());
    }


    @Override
    protected void finalize() throws Throwable {
        mPhoneticPlayer.close();
        mDicSearcher.close();
        super.finalize();

    }
}
