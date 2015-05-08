package kr.re.dev.MoongleDic.DicData;

import com.google.common.collect.Lists;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import kr.re.dev.MoongleDic.DicData.Database.WordColumns;

/**
 *  클립보드 단어사전 HoenyDic::WordCard class.
 *  단어와 그와 매치되는 뜻을 포함한 여러 정보를 갖고 있다.
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
public class WordCard {
    private String mRaw = "";
    private String mWord = "";
    private String mPhonetic = "";
    private List<DescriptionCard> mDescriptionCards = Lists.newArrayList();

    public static WordCard newInstance(String word) {
        WordCard wordCard =  new WordCard();
        wordCard.mWord = word;
        return wordCard;
    }

    public static WordCard newInstance(WordColumns wordColumns) {
        WordCard wordCard =  fromWordColumns(wordColumns);
        return wordCard;
    }

    private static WordCard fromWordColumns(WordColumns wordColumns) {
        WordCard wordCard = null;
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();

        }
        String row = wordColumns.getDescription();
        InputSource is = new InputSource(new StringReader(row));
        Document document = null;
        try {
            document = documentBuilder.parse(is);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(document != null) {
            wordCard = parseToWordCard(document);
        }
        else {
            wordCard = new WordCard();
            wordCard.mDescriptionCards = Lists.newArrayListWithExpectedSize(0);
        }
        wordCard.mRaw = row;
        wordCard.mWord = wordColumns.getWord();
        return wordCard;
    }

    private static WordCard parseToWordCard(Document document) {
        WordCard wordCard =  new WordCard();
        Element element =  document.getDocumentElement();
        NodeList phonetics =  element.getElementsByTagName("M");
        if(phonetics.getLength() > 0) {
            wordCard.mPhonetic = phonetics.item(0).getTextContent();
        }
        NodeList descriptions =  element.getElementsByTagName("N");
        wordCard.mDescriptionCards =  parseFromN(descriptions);
        return wordCard;
    }

    private static List<DescriptionCard> parseFromN(NodeList descriptionNodeList) {
        int descriptionsLen = descriptionNodeList.getLength();
        int childLen = 0;
        List<DescriptionCard> resultList = Lists.newArrayListWithExpectedSize(descriptionsLen);
        for(int i = 0; i < descriptionsLen; ++i) {
            DescriptionCard descriptionCard = new DescriptionCard();
            NodeList childs =  descriptionNodeList.item(i).getChildNodes();
            childLen = childs.getLength();
            for(int ci = 0; ci < childLen; ++ci) {
                Node node = childs.item(ci);
                if(node.getNodeName().equals("U")) {
                    descriptionCard.setWordClass(node.getTextContent());
                }
                if(node.getNodeName().equals("#text")) {
                    descriptionCard.setMeaning(node.getTextContent());
                }
            }
            resultList.add(descriptionCard);
        }
        return resultList;
    }


    public List<DescriptionCard> getDescriptionCards() {

       return Lists.newArrayList(mDescriptionCards);
    }


    /**
     * 발음
     * @return
     */
    public String phonetic() {
        return  mPhonetic;
    }

    /**
     * 뜻.
     * @return
     */
    public String word() {
        return mWord;
    }

    /**
     * xml 데이터.
     * @return
     */
    public String raw() {
        return  mRaw;
    }
}
