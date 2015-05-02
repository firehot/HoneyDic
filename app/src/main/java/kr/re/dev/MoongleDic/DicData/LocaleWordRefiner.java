package kr.re.dev.MoongleDic.DicData;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 텍스트와 Locale 를 입력받고, Locale 에 해당하지 않는 단어를 제거하여 반환해주는 static method 를 갖고 있는 클래스다.
 * Created by ice3x2 on 15. 5. 3..
 */
public class LocaleWordRefiner {
    private final static Map<Locale, ArrayList<Character.UnicodeBlock>> UNICODE_MAP =
            ImmutableMap.<Locale, ArrayList<Character.UnicodeBlock>>builder()
            .put(Locale.ENGLISH, Lists.newArrayList
                    (Character.UnicodeBlock.BASIC_LATIN,
                    Character.UnicodeBlock.LATIN_1_SUPPLEMENT,
                    Character.UnicodeBlock.LATIN_EXTENDED_A,
                    Character.UnicodeBlock.LATIN_EXTENDED_B,
                    Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL))

            .put(Locale.KOREAN, Lists.newArrayList
                    (Character.UnicodeBlock.HANGUL_SYLLABLES,
                            Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO,
                            Character.UnicodeBlock.HANGUL_JAMO))

            .put(Locale.JAPANESE, Lists.newArrayList
                    (Character.UnicodeBlock.HIRAGANA,
                            Character.UnicodeBlock.KATAKANA,
                            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
                            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A))

            .build();

    /**
     * 사전이나 번역기에 넣어서 양질을 결과를 얻을 수 있도록 입력받은 단어어들에서 입력받은 지역(Locale) 에 해당하지 않는 단어는 제거하고 두 칸 이상 간격 문자를 제거하여 반환한다.
     * @param line
     * @param locale
     * @return
     */
    public final static String refine(String line, Locale locale) {
        String[] words =  line.split("\\s");
        StringBuilder stringBuilder = new StringBuilder();
        List<Character.UnicodeBlock> unicodeBlockList =  UNICODE_MAP.get(locale);
        if(unicodeBlockList == null) return "";
        for(int i = 0, n = words.length - 1; i <= n; ++i) {
            String word = words[i];
            if(word.equals(" ") || word.isEmpty()) {
                continue;
            }
            boolean sameLocaleLanguage = true;
            char[] strChar = word.toCharArray();
            for(char ch : strChar) {
                if(!unicodeBlockList.contains(Character.UnicodeBlock.of(ch))) {
                    sameLocaleLanguage = false;
                    break;
                }
            }
            if(sameLocaleLanguage && !word.isEmpty()) {
                stringBuilder.append(word);
                if(i != n) {
                    stringBuilder.append(' ');
                }
            }

        }

        int length = stringBuilder.length();
        if(length <= 0) return "";
        int lastChar = stringBuilder.charAt(length - 1);
        if(lastChar == ' ') {
            stringBuilder.deleteCharAt(length - 1);
        }
        return stringBuilder.toString();
    }


}
