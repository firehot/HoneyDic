package kr.re.dev.MoongleDic.DicData.Database;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ice3x2 on 15. 4. 24..
 */
public class KeyWordColumns extends RealmObject{

    @PrimaryKey
    private String      keyword;
    private String      dicName;
    private int         refs;
    private boolean     isBase;
    private RealmList<WordColumns> words;


    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDicName() {
        return dicName;
    }

    public void setDicName(String dicName) {
        this.dicName = dicName;
    }

    public int getRefs() {
        return refs;
    }

    public void setRefs(int refs) {
        this.refs = refs;
    }

    public boolean isBase() {
        return isBase;
    }

    public void setIsBase(boolean isBase) {
        this.isBase = isBase;
    }

    public RealmList<WordColumns> getWords() {
        return words;
    }

    public void setWords(RealmList<WordColumns> words) {
        this.words = words;
    }


}
