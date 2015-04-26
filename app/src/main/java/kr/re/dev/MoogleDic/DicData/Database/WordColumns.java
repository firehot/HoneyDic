package kr.re.dev.MoogleDic.DicData.Database;

import io.realm.RealmObject;

/**
 * Created by ice3x2 on 15. 4. 24..
 */
public class WordColumns extends RealmObject {

    private String      word;
    private String      description;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
