package kr.re.dev.MoongleDic.DicService;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.common.collect.Lists;

import java.util.List;

import kr.re.dev.MoongleDic.DicData.Database.DicInfoManager;
import kr.re.dev.MoongleDic.DicData.DicSearcher;
import kr.re.dev.MoongleDic.DicData.LocaleWordRefiner;
import kr.re.dev.MoongleDic.DicData.WordCard;
import kr.re.dev.MoongleDic.UI.DicItemViewWrapper;
import kr.re.dev.MoongleDic.UI.DicToast;
import rx.Observable;
import rx.functions.Func2;
import rx.subjects.PublishSubject;

public class DicService extends Service {

    private DicToast mDicToast;
    private ClipboardManager mClipboardManager;
    private PhoneticPlayer mPhoneticPlayer;
    private DicSearcher mDicSearcher;
    private DicInfoManager.DicInfo mDicInfo;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }



    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("testio", "create service");
        Context context = getApplicationContext();
        mDicInfo= DicInfoManager.newInstance(context).getDicInfo(DicInfoManager.Dic.EnglishToKorean);
        mDicSearcher = DicSearcher.newInstance(context, mDicInfo.getDicDBName());
        mDicToast = new DicToast(context);
        mClipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        mPhoneticPlayer = PhoneticPlayer.newInstance(context, mDicInfo.getFromLanguage());

    }

    @Override
    public void onDestroy() {
        DicItemViewWrapper.recycleAll();
        mPhoneticPlayer.stop();
        mDicSearcher.close();
        mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        super.onDestroy();
    }

    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            ClipData data =  mClipboardManager.getPrimaryClip();
            if(data.getItemCount() <= 0) return;
            Object textItem =  data.getItemAt(0).getText();
            if(textItem != null) {
                String text = textItem.toString();
                String words = LocaleWordRefiner.refine(text, mDicInfo.getFromLanguage());
                showWord(words);

            }
        }
    };

    private void showWord(String word) {
        Observable.just(word)
                .doOnNext(mPhoneticPlayer::play)
                .flatMap(mDicSearcher::search)
                .zipWith(Lists.newArrayList(word), (wordCards, comWord) -> {
                    if (wordCards.isEmpty() && !comWord.isEmpty()) wordCards.add(WordCard.newInstance(comWord));
                    return wordCards;
                }).
                flatMap(mDicToast::show)
                .subscribe(hide -> {
                    Log.i("testio", (hide == DicToast.HideDirection.Left)?"왼쪽":"오른쪽");
                    Toast.makeText(getApplicationContext(), (hide == DicToast.HideDirection.Left)?"왼쪽":"오른쪽", Toast.LENGTH_SHORT).show();
                });

    }

}
