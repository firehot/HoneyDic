package kr.re.dev.MoongleDic;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import kr.re.dev.MoongleDic.DicService.ClipboardDicService;
import kr.re.dev.MoongleDic.DicService.Settings;
import kr.re.dev.MoongleDic.UI.SettingFragment;

public class MainActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("testio", "onCreate MainActivity");
        setContentView(R.layout.activity_main);

        //Intent service = new Intent(getApplicationContext(), ClipboardDicService.class);
        //startService(service);




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

        Intent intent = new Intent(getApplicationContext(), ClipboardDicService.class);
        bindService(intent, mServiceConnection, Context.BIND_ADJUST_WITH_ACTIVITY | Context.BIND_AUTO_CREATE);


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
    protected void onDestroy() {
        unbindService(mServiceConnection);
        if(!Settings.getSettings(this).isUseClipboardDic()) {
            stopClipboardDicService();
        }
        super.onDestroy();
    }

    private void stopClipboardDicService() {
        Intent intent = new Intent(this,ClipboardDicService.class);
        stopService(intent);
    }



    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("testio", "onServiceConnected : ClipboardDicService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("testio", "onServiceDisconnected : ClipboardDicService");
        }

    };



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
