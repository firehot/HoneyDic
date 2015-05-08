package kr.re.dev.MoongleDic;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import kr.re.dev.MoongleDic.DicService.ClipboardDicService;
import kr.re.dev.MoongleDic.DicService.Settings;
import kr.re.dev.MoongleDic.UI.SettingFragment;

/**
 *  클립보드 단어사전 HoenyDic::MainActivity class.
 *  메인 액티비티.
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
public class MainActivity extends ActionBarActivity {


    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("testio", "onCreate MainActivity");
        setContentView(R.layout.activity_main);
        initSystemBarColor();
        initViews();
        initToolbar();

        //Intent service = new Intent(getApplicationContext(), ClipboardDicService.class);
        //startService(service);






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

    private void initViews() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.frameLayoutShadow).setVisibility(View.GONE);
        }
        mToolbar = (Toolbar)findViewById(R.id.toolBarMain);
        mToolbar.setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(Color.WHITE);
    }

    private void initToolbar() {
        //mToolbar.setTitle(R.string.app_name);

    }

    private void initSystemBarColor() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.setStatusBarColor(getResources().getColor(R.color.HoneyDic_identity_600));
//            window.setNavigationBarColor(getResources().getColor(R.color.HoneyDic_identity_900));
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintEnabled(true);
            // Kitkat 은 StatusBar 와 NavigationBar 에 그라데이션이 생기기 때문에 그냥 툴바(또는 액션바) 색상과 동일하게 가는 것이 더 이쁨.
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.HoneyDic_identity));
            tintManager.setNavigationBarTintColor(getResources().getColor(R.color.HoneyDic_identity));
        }
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
