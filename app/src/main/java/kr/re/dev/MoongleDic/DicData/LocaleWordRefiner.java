package kr.re.dev.MoongleDic.DicData;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 *  클립보드 단어사전 HoenyDic::LocaleWordRefiner class.
 *  텍스트와 지역 정보를 입력받고, 지역에 해당하지 않는 알파벳을 포함하는 단어를 제거하여 반환해준다.
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
        if(isURI(line)) return "";

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

    private static boolean isURI(String line) {
        return line.contains("://");


    }


}
