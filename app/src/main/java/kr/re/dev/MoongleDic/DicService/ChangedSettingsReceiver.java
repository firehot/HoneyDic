package kr.re.dev.MoongleDic.DicService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.common.base.Strings;

import kr.re.dev.MoongleDic.Constants;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 *  클립보드 단어사전 HoenyDic::ChangedSettingsReceiver class.
 *  설정(Settings) 변경 브로드캐스트를 받는다.
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
public class ChangedSettingsReceiver extends BroadcastReceiver {

    private PublishSubject<Settings> mSettingPublishSubject = PublishSubject.create();
    public ChangedSettingsReceiver() {}

    public Observable<Settings> settingChangedEvent() {
        return Observable.merge(mSettingPublishSubject.asObservable(), Observable.<Settings>empty());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(!Strings.isNullOrEmpty(action) && action.equals(Constants.ACTION.BROADCAST_SETTING)) {
            Settings settings =  Settings.getSettings(intent);
            mSettingPublishSubject.onNext(settings);
        }
    }
}
