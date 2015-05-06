package kr.re.dev.MoongleDic;


import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

import kr.re.dev.MoongleDic.UI.SettingFragment;

public class MainActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.HoneyDic_identity_600));
        }

        SettingFragment settingFragment = new SettingFragment();
        FragmentManager manager =  getSupportFragmentManager();
        FragmentTransaction ft =  manager.beginTransaction();
        ft.add(R.id.frameLayoutContent,settingFragment);
        ft.commit();


        //CheckBox checkBox = (CheckBox)findViewById(R.id.viewCheckBoxUse);
        // spinner = (Spinner)findViewById(R.id.viewSpinnerKeepTime);



/*
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.KeepWordCardTimeSelectSpinnerItem, R.layout.item_keep_wordcard);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener((self_, view, pos, id) -> {


        });*/

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
