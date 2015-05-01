package kr.re.dev.MoogleDic;

import android.app.Application;
import android.os.Handler;


import kr.re.dev.MoogleDic.DicData.Database.DicInfoManager;
import kr.re.dev.MoogleDic.DicData.DicSearcher;
import kr.re.dev.MoogleDic.UI.DicToast;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 
 * Created by ice3x2 on 15. 4. 15..
 */
public class MoogleDicApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DicToast dicToast = new DicToast(getApplicationContext());
        DicInfoManager.DicInfo dicInfo = DicInfoManager.newInstance(getApplicationContext()).getDicInfo(DicInfoManager.Dic.EnglishToKorean);
        DicSearcher dicSearcher = DicSearcher.newInstance(getApplicationContext(), dicInfo.getDicDBName());
        dicSearcher.search("are").observeOn(AndroidSchedulers.mainThread()).subscribe(wordCards -> dicToast.show(wordCards));
        new Handler().postDelayed(() -> dicSearcher.search("truth").observeOn(AndroidSchedulers.mainThread()).subscribe(wordCards -> dicToast.show(wordCards)), 4000);
    }
}
