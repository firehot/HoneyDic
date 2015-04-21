package kr.re.dev.MoogleDic.DicData;


import android.content.Context;

import com.google.common.base.MoreObjects;
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



/**
 * Created by ice3x2 on 15. 4. 22..
 *
 * DicInfoManager.
 * 사전 정보를 관리한다.
 *
 */
public class DicInfoManager {

    /**
     * DicInfoManager 가 사용하는 파일 이름.
     */
    public static String FILE_NAME = "DicInfos.";
    private File mDicInfosFile = null;
    private HashMap<String, DicInfo> mDicInfosMap = null;

    public static DicInfoManager newInstance(Context context) {
        return new DicInfoManager(context.getFilesDir());
    }

    private DicInfoManager(File dataFileDir) {
        mDicInfosFile = new File(dataFileDir, FILE_NAME);
        mDicInfosMap = open(mDicInfosFile);
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, DicInfo> open(File file) {
        if(!file.isFile()) return new HashMap<>();
        try {
            FileInputStream fis =  new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object obj = ois.readObject();
            ois.close();
            return (HashMap<String, DicInfo>)obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }

    private void save(File file, HashMap<String, DicInfo> map) {
        try {
            FileOutputStream fos =  new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(map);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public List<DicInfo> getDicInfos() {
        Collection<DicInfo> values = mDicInfosMap.values();
        return Lists.newArrayList(values);
    }

    public void pushDicInfo(DicInfo dicInfo) {
        DicInfo containDicInfo =  mDicInfosMap.get(dicInfo.getDicDBName());
        if(containDicInfo != null && containDicInfo.equals(dicInfo)) return;
        mDicInfosMap.put(dicInfo.getDicDBName(), dicInfo);
        save(mDicInfosFile, mDicInfosMap);
    }

    public void removeDicInfo(String dicDBName) {
        DicInfo removedDicInfo =  mDicInfosMap.remove(dicDBName);
        if(removedDicInfo == null) return;
        save(mDicInfosFile, mDicInfosMap);
    }

    public DicInfo getSelectedDic() {
        Collection<DicInfo> values = mDicInfosMap.values();
        DicInfo selectedDicInfo = null;
        for(DicInfo info : values) {
            if(info.isSelected()) {
                selectedDicInfo = info;
                break;
            }
        }
        return selectedDicInfo;
    }

    public void selectDic(String dicDBName) {
        DicInfo selectedDicInfo = getSelectedDic();
        DicInfo willSelectDicInfo =  mDicInfosMap.get(dicDBName);
        if(willSelectDicInfo == null) return;
        willSelectDicInfo.setSelect(true);
        if(selectedDicInfo != null)  selectedDicInfo.setSelect(false);
        save(mDicInfosFile, mDicInfosMap);
    }

    public void clear() {
        mDicInfosMap.clear();
        save(mDicInfosFile, mDicInfosMap);
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
