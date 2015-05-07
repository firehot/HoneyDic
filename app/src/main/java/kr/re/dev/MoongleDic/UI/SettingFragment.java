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
 * 세팅 프래그먼트.
 * Created by ice3x2 on 15. 5. 5..
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
