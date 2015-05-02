package kr.re.dev.MoongleDic.DicData;

/**
 * Created by ice3x2 on 15. 4. 25..
 */
public class DescriptionCard {

    private String mWordClass = "";
    private String mMeaning = "";


    protected DescriptionCard() {}
    public String meaning() {
        return  mMeaning;
    }
    public String wordClass() {
        return mWordClass;
    }

    public DescriptionCard setMeaning(String meaning) {
        mMeaning = meaning;
        return this;
    }
    public DescriptionCard setWordClass(String wordClass) {
        mWordClass = wordClass;
        return this;
    }
}
