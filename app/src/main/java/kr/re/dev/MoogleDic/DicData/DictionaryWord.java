package kr.re.dev.MoogleDic.DicData;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * 사전 DB 의 Column.
 * word 와 내용을 담고 있다.
 * Created by ice3x2 on 15. 4. 15..
 */
@DatabaseTable
public class DictionaryWord {


    @DatabaseField(id = true)
    private String      word = "";

    @DatabaseField(foreign = true)
    private String      indexWord = "";

    @DatabaseField
    private String      dicName;
    @DatabaseField(canBeNull = true)
    private String      description;
    @DatabaseField
    private int         refs = 0;
    @DatabaseField
    private boolean     isBase = true;

    @ForeignCollectionField
    ForeignCollection<DictionaryWord> references;


    public DictionaryWord() {
    }

    public DictionaryWord(Dao<DictionaryWord, String> dao) {
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }


    public String getDicName() {
        return dicName;
    }

    public void setDicName(String dicName) {
        this.dicName = dicName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

//    public RealmList<DictionaryWord> getWords() {
//        return words;
//    }
//
//    public void setWords(RealmList<DictionaryWord> words) {
//        this.words = words;
//    }





}
