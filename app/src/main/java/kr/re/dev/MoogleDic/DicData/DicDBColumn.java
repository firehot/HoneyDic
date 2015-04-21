package kr.re.dev.MoogleDic.DicData;


//import com.j256.ormlite.field.DatabaseField;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 사전 DB 의 Column.
 * word 와 내용을 담고 있다.
 * Created by ice3x2 on 15. 4. 15..
 */
public class DicDBColumn extends RealmObject {

    @PrimaryKey
    private String      word;
    private String      description;
    private String      dicName;


    public DicDBColumn() {}
    public DicDBColumn(String word, String description, String dicName) {
        this.word = word;
        this.description = description;
        this.dicName = dicName;
    }


    public void setWord(String word) {
        this.word = word;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDicName(String dicName) {
        this.dicName = dicName;
    }

    public String getWord() {
        return word;
    }

    /**
     * Lingoes XML 형태로 뜻을 반환한다. 가공하여 사용해야함.
     * @return Lingoes (ld2) XML
     */
    public String getDescription() {
        return description;
    }

    /**
     * 사전의 이름을 반환한다.
     * @return 사전 이름
     */
    public String getDicName() {
        return dicName;
    }
}
