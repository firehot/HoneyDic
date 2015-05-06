package kr.re.dev.MoongleDic.DicService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.common.base.Strings;

import rx.Observable;
import rx.subjects.PublishSubject;

public class ChangedSettingsReceiver extends BroadcastReceiver {

    public final static String ACTION_SETTING = "kr.re.dev.MoongleDic.Settings";

    private PublishSubject<Settings> mSettingPublishSubject = PublishSubject.create();
    public ChangedSettingsReceiver() {}

    public Observable<Settings> settingChangedEvent() {
        return Observable.merge(mSettingPublishSubject.asObservable(), Observable.<Settings>empty());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(!Strings.isNullOrEmpty(action) && action.equals(ACTION_SETTING)) {
            Settings settings =  Settings.getSettings(intent);
            mSettingPublishSubject.onNext(settings);
        }
    }
}
