package kr.re.dev.MoongleDic.UI;


import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

import kr.re.dev.MoongleDic.Commons.ViewWrapper;
import kr.re.dev.MoongleDic.DicData.DescriptionCard;
import kr.re.dev.MoongleDic.DicData.WordCard;
import kr.re.dev.MoongleDic.R;

/**
 * DicToast 내부의 단어 뜻을 보여주는 리스트의 아이템 뷰 포함.
 * Created by ice3x2 on 15. 4. 30..
 */
public class DicItemViewWrapper extends ViewWrapper {

    private LinearLayout mLayoutHorizontalContentBox;
    private TextView mTextViewWord;
    private TextView mTextViewPhonetic;
    private List<MeanItemWrapper> mMeanItemWrapperList = Lists.newArrayList();
    private static String mTouchedWordString = "";
    private String mWord = "";

    public  static String flushThouchedWord() {
        String result = mTouchedWordString;
        mTouchedWordString = "";
        return result;
    }

    public void cleanMeanItems() {
        for(MeanItemWrapper meanItemWrapper :  mMeanItemWrapperList) {
            meanItemWrapper.removeFromParent();
        }
        mMeanItemWrapperList.clear();
    }

    public DicItemViewWrapper(Context context) {
        super(context, R.layout.item_wordcard);
        initViews();
    }


    public void setWordCard(WordCard wordCard) {
        updateFromWordCard(wordCard);

        update();
    }

    private void updateFromWordCard(WordCard wordCard) {
        mWord = wordCard.word();
        mTextViewWord.setText(mWord);
        String phonetic =  wordCard.phonetic();
        if(!phonetic.isEmpty()) {
            mTextViewPhonetic.setVisibility(View.VISIBLE);
            mTextViewPhonetic.setText("[" + wordCard.phonetic() + "]");
        }  else {
            mTextViewPhonetic.setVisibility(View.GONE);
            mTextViewPhonetic.setText("");
        }

        List<DescriptionCard> descriptionCardList = wordCard.getDescriptionCards();
        updateMeanItems(descriptionCardList);
    }

    private void updateMeanItems(List<DescriptionCard> descriptionCards) {
        MeanItemWrapper meanItemWrapper = null;
        Collections.sort(mMeanItemWrapperList);
        int meanItemsSize =  mMeanItemWrapperList.size();
        int i = 0, n = descriptionCards.size();
        while(i < n) {
            if (meanItemsSize <= i) {
                meanItemWrapper = new MeanItemWrapper(getContext());
                mMeanItemWrapperList.add(meanItemWrapper);
                addView(meanItemWrapper);
                ++meanItemsSize;
            } else {
                meanItemWrapper = mMeanItemWrapperList.get(i);
            }
            DescriptionCard descriptionCard = descriptionCards.get(i);
            meanItemWrapper.setClassOfWord(descriptionCard.wordClass()).setMeanOfWord(descriptionCard.meaning());
            meanItemWrapper.getView().setVisibility(View.VISIBLE);
            ++i;
        }
        while(i < meanItemsSize) {
            meanItemWrapper.getView().setVisibility(View.GONE);
            ++i;
        }
    }



    @Override
    public void update() {

    }


    private void initViews() {
        mLayoutHorizontalContentBox = (LinearLayout)findViewById(R.id.linearLayoutHorizontalContentBox);
        mTextViewWord = (TextView)findViewById(R.id.textViewWord);
        mTextViewPhonetic = (TextView)findViewById(R.id.textViewPhonetic);
        mTextViewWord.setOnTouchListener((v,motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchedWordString = mWord;
            }
            return false;
        });
    }



}
