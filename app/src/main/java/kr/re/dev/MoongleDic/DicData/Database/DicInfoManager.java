package kr.re.dev.MoongleDic.DicData.Database;


import android.content.Context;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.base.Objects;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 *  클립보드 단어사전 HoenyDic::DicInfoManager class. 이 프로젝트에 기본적으로 포함된 사전과 데이터베이스 정보를 갖고 있다.
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
public class DicInfoManager {

    public enum Dic {EnglishToKorean("Vicon English-Korean dictionary");
        private String dicName;
        Dic(String name) {
            dicName = name;
        }
        private String getDicName() {
            return dicName;
        }
    }


    /**
     * DicInfoManager 가 사용하는 파일 이름.
     */
    private static final Map<String, DicInfo> DEFAULT_DIC_MAP = ImmutableMap.<String,DicInfo>builder()
            .put("Vicon English-Korean dictionary",
                    DicInfo.newInstance("Vicon English-Korean Dictionary.db", Locale.ENGLISH, Locale.KOREAN)).build();

    private HashMap<String, DicInfo> mDicInfosMap = null;

    public static DicInfoManager newInstance(Context context) {
        return new DicInfoManager(context.getFilesDir());
    }

    private DicInfoManager(File dataFileDir) {
        mDicInfosMap = new HashMap<>();
        mDicInfosMap.putAll(DEFAULT_DIC_MAP);
    }



    @SuppressWarnings("unchecked")
    public List<DicInfo> getDicInfos() {
        Collection<DicInfo> values = mDicInfosMap.values();
        return Lists.newArrayList(values);
    }

    public DicInfo getDicInfo(Dic dic) {
        return mDicInfosMap.get(dic.getDicName());
    }



    public static class DicInfo implements Serializable{
        private static final long serialVersionUID = 9169383145641170635L;
        private String mDicDBName;
        private Locale mFromLanguage;
        private Locale mToLanguage;
        private boolean mSelected;

        private void setSelect(boolean select) {
            mSelected = select;
        }

        public static DicInfo newInstance(String dicDBName, Locale fromLanguage, Locale toLanguage) {
            DicInfo dicInfo = new DicInfo();
            dicInfo.setSelect(false);
            dicInfo.mToLanguage = toLanguage;
            dicInfo.mFromLanguage = fromLanguage;
            dicInfo.mDicDBName = dicDBName;
            return dicInfo;
        }

        private DicInfo() {}

        public boolean isSelected() {
            return mSelected;
        }

        public String getDicDBName() {
            return mDicDBName;
        }
        public Locale getToLanguage() {
            return mToLanguage;
        }
        public Locale getFromLanguage() {
            return mFromLanguage;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("DicDBName", mDicDBName).add("FromLanguage", mFromLanguage)
                    .add("ToLanguage",mToLanguage).add("isSelected", mSelected).toString();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(mDicDBName, mFromLanguage, mToLanguage);
        }

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof DicInfo)) return false;
            DicInfo that = (DicInfo)o;
            return Objects.equal(mDicDBName, that.mDicDBName) &&
                   Objects.equal(mFromLanguage, that.mFromLanguage) &&
                   Objects.equal(mToLanguage, that.mToLanguage);
        }
    }
}
