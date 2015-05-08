package kr.re.dev.MoongleDic.DicService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 *  클립보드 단어사전 HoenyDic::BootReceiver class.
 *  시스템 부트 완료 및, 커스텀 Action 의 브로드캐스트를 받아서 클립보드 단어사전 서비스 (ClipboardDicService) 를 시작한다.
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
public class BootReceiver extends BroadcastReceiver {

    public final static String ACTION_START_CLIPBOARDDIC = "kr.re.dev.MoongleDic.StartClipboardDic";
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Settings settings =  Settings.getSettings(context);
        if(intent != null && ACTION_START_CLIPBOARDDIC.equals(intent.getAction())) {
            startService(context);
        } else if(settings.isUseClipboardDic()) {
            startService(context);
        }
    }

    private  void startService(Context context) {
        Log.i("testio", "Start ClipboradDicService by BootReceiver.");
        Intent service = new Intent(context, ClipboardDicService.class);
        context.startService(service);
    }
}
