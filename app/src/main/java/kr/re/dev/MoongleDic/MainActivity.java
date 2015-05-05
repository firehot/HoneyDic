package kr.re.dev.MoongleDic;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.rey.material.drawable.CheckBoxDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.Button;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.Spinner;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import kr.re.dev.MoongleDic.DicService.PhoneticPlayer;
import kr.re.dev.MoongleDic.UI.DicItemViewWrapper;

public class MainActivity extends FragmentActivity {

    PhoneticPlayer mPhoneticPlayer;
    DicItemViewWrapper item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_option);
        //CheckBox checkBox = (CheckBox)findViewById(R.id.viewCheckBoxUse);
        Spinner spinner = (Spinner)findViewById(R.id.viewSpinnerKeepTime);




        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.KeepWordCardTimeSelectSpinnerItem, R.layout.item_keep_wordcard);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener((self_, view, pos, id) -> {


        });

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
