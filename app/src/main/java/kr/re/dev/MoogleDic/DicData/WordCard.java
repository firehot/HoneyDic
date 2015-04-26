package kr.re.dev.MoogleDic.DicData;

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

import kr.re.dev.MoogleDic.DicData.Database.WordColumns;

/**
 * Created by ice3x2 on 15. 4. 25..
 */
public class WordCard {
    private String mRaw = "";
    private String mWord = "";
    private String mPhonetic = "";
    private List<DescriptionCard> mDescriptionCards;

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
