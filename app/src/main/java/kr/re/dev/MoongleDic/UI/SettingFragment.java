package kr.re.dev.MoongleDic.UI;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.common.base.Strings;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.Spinner;

import kr.re.dev.MoongleDic.DicService.Settings;
import kr.re.dev.MoongleDic.R;

/**
 *  클립보드 단어사전 HoenyDic::SettingFragment class.
 *  세팅 화면을 출력하는 프래그먼트.
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
public class SettingFragment extends Fragment{


    private Spinner mSpinnerKeepWordCardTime;
    private CheckBox mCheckBoxUse;
    private CheckBox mCheckBoxNoneForceClose;
    private CheckBox mCheckBoxUseTTS;
    private CheckBox mCheckBoxSoundEffect;
    private Settings mSettings;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSettings = Settings.getSettings(inflater.getContext());
        View view =  inflater.inflate(R.layout.fragment_option, container,false);
        initChildViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        initSettings();
        super.onResume();
    }

    @Override
    public void onPause() {
        mSettings.commit(getActivity());
        super.onPause();
    }


    public void commitSettings(Context context) {
        mSettings.commit(context);
    }

    private void initSettings() {
        mCheckBoxUse.setCheckedImmediately(mSettings.isUseClipboardDic());
        mCheckBoxNoneForceClose.setCheckedImmediately(mSettings.isWordCardNoneForceClose());
        mCheckBoxUseTTS.setCheckedImmediately(mSettings.isUseTTS());
        mCheckBoxSoundEffect.setCheckedImmediately(mSettings.isSoundEffect());
        mSpinnerKeepWordCardTime.setSelection(findSelection(getActivity().getApplicationContext(), mSettings.getWordCardKeepTime()));
    }

    private void initChildViews(View view) {
        mCheckBoxUse = (CheckBox) view.findViewById(R.id.viewCheckBoxUse);
        mCheckBoxUse.setOnCheckedChangeListener((v, isChecked) -> mSettings.setUseClipboardDic(isChecked));

        mCheckBoxNoneForceClose = (CheckBox) view.findViewById(R.id.checkBoxNoneForceClose);
        mCheckBoxNoneForceClose.setOnCheckedChangeListener((v, isChecked) -> mSettings.setWordCardNoneForceClose(isChecked) );

        mCheckBoxUseTTS = (CheckBox) view.findViewById(R.id.checkBoxUseTTS);
        mCheckBoxUseTTS.setOnCheckedChangeListener((v, isChecked) -> mSettings.setUseTTS(isChecked) );

        mCheckBoxSoundEffect = (CheckBox) view.findViewById(R.id.checkBoxSoundEffect);
        mCheckBoxSoundEffect.setOnCheckedChangeListener((v, isChecked) -> mSettings.setSoundEffect(isChecked));

        mSpinnerKeepWordCardTime = (Spinner)view.findViewById(R.id.viewSpinnerKeepTime);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.KeepWordCardTimeSelectSpinnerItem, R.layout.item_keep_wordcard);
        mSpinnerKeepWordCardTime.setAdapter(adapter);
        mSpinnerKeepWordCardTime.setOnItemClickListener((self_, v, pos, id) -> {
            int time = keepTimeStringToMsInt((String) self_.getAdapter().getItem(pos));
            mSettings.setWordCardKeepTime(time);
            return true;
        });
    }

    private int findSelection(Context context, int inTime) {
        String[] keepTimes =  context.getResources().getStringArray(R.array.KeepWordCardTimeSelectSpinnerItem);
        for(int i = 0, n = keepTimes.length; i < n; ++i) {
            int time =  keepTimeStringToMsInt(keepTimes[i]);
            if(time == inTime) return i;
        }
        return 0;
    }

    private int keepTimeStringToMsInt(String keepTimeString) {
        String time =  keepTimeString.replaceAll("[^0-9]", "");
        if(Strings.isNullOrEmpty(time)) time = "0";
        return Integer.parseInt(time) * 1000;
    }

}
