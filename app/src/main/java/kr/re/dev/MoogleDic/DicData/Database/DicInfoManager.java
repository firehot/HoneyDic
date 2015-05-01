package kr.re.dev.MoogleDic.DicData.Database;


import android.content.Context;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.base.Objects;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Created by ice3x2 on 15. 4. 22..
 *
 * DicInfoManager.
 * 사전 정보를 관리한다.
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
