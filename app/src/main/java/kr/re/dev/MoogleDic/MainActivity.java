package kr.re.dev.MoogleDic;


import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.LinearLayout;


import kr.re.dev.MoogleDic.DicData.Database.DicInfoManager;
import kr.re.dev.MoogleDic.DicData.DicSearcher;
import kr.re.dev.MoogleDic.DicData.WordCard;
import kr.re.dev.MoogleDic.DicService.PhoneticPlayer;
import kr.re.dev.MoogleDic.UI.DicItemViewWrapper;
import rx.android.schedulers.AndroidSchedulers;

public class MainActivity extends FragmentActivity {

    PhoneticPlayer mPhoneticPlayer;
    DicItemViewWrapper item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //finish();
        /*DicInfoManager dicInfoManager =  DicInfoManager.newInstance(this);
        DicInfoManager.DicInfo dicInfo =  dicInfoManager.getDicInfo(DicInfoManager.Dic.EnglishToKorean);
        item = DicItemViewWrapper.obtain(this);
        ((LinearLayout)findViewById(R.id.linearLayoutMain)).addView(item.getView());
        mPhoneticPlayer = PhoneticPlayer.newInstance(this, dicInfo.getFromLanguage());*/

    }


    @Override
    protected void onPause() {
        super.onPause();
       // DicItemViewWrapper.recycleAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*DicInfoManager dicInfoManager =  DicInfoManager.newInstance(this);
        DicInfoManager.DicInfo dicInfo =  dicInfoManager.getDicInfo(DicInfoManager.Dic.EnglishToKorean);
        DicSearcher dicSearcher =  DicSearcher.newInstance(this,dicInfo.getDicDBName());
        dicSearcher.search("take").observeOn(AndroidSchedulers.mainThread()).subscribe(wordCards -> {
            WordCard wordCard = wordCards.get(0);
            item.setWordCard(wordCard);
            mPhoneticPlayer.play("take");
        });*/

    }
}
