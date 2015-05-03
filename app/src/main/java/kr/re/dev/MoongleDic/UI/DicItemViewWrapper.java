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
import com.google.common.collect.Lists;
import java.util.List;

import kr.re.dev.MoongleDic.Commons.ViewWrapper;
import kr.re.dev.MoongleDic.DicData.DescriptionCard;
import kr.re.dev.MoongleDic.DicData.WordCard;
import kr.re.dev.MoongleDic.R;

/**
 * DicToast 내부의 단어 뜻을 보여주는 리스트의 아이템 뷰 포함.
 * Created by ice3x2 on 15. 4. 30..
 */
public class DicItemViewWrapper extends ViewWrapper  {


    private static DicItemViewWrapper sRecycledEnd = null;
    private static DicItemViewWrapper sOnEnd = null;
    private static int sCount = 0;

    private DicItemViewWrapper mRecycledPrev;
    private DicItemViewWrapper mOnPrev;
    private boolean mIsRecycled = false;
    private LinearLayout mLayoutHorizontalContentBox;
    private TextView mTextViewWord;
    private TextView mTextViewPhonetic;
    private List<MeanItemWrapper> mMeanItemWrapperList = Lists.newArrayList();
    private WordCard mWordCard;
    private String mTouchedWordString = "";


    public static void recycleAll() {
        DicItemViewWrapper dicItemViewWrapper = sOnEnd;
        while(dicItemViewWrapper != null) {
            sOnEnd = dicItemViewWrapper.mOnPrev;
            dicItemViewWrapper.mIsRecycled = true;
            dicItemViewWrapper.release();
            DicItemViewWrapper oldRecycled =  sRecycledEnd;
            sRecycledEnd = dicItemViewWrapper;
            sRecycledEnd.mRecycledPrev = oldRecycled;
            dicItemViewWrapper = sOnEnd;
            --sCount;
        }
        Log.d("testio", "DicItemViewWrapper is recycled all, count : " + sCount);
    }
    private void release() {
        recycleMeanViewWrappers();
        mWordCard = null;
    }


    public static DicItemViewWrapper findWith(WordCard wordCard) {
        DicItemViewWrapper dicItemViewWrapper = sOnEnd;
        while(dicItemViewWrapper != null) {
            if(dicItemViewWrapper.mWordCard != null && wordCard == dicItemViewWrapper.mWordCard) return dicItemViewWrapper;
            dicItemViewWrapper = dicItemViewWrapper.mOnPrev;
        }
        return null;
    }


    public static DicItemViewWrapper obtain(Context context) {
        DicItemViewWrapper oldDicItemViewWrapper = sOnEnd;
        if(sRecycledEnd == null) {
            sOnEnd =  new DicItemViewWrapper(context);
            Log.d("testio", "DicItemViewWrapper is created : " + ++sCount);
        }  else {
            DicItemViewWrapper newDicItem =  sRecycledEnd;
            newDicItem.mIsRecycled = false;
            sRecycledEnd = sRecycledEnd.mRecycledPrev;
            sOnEnd = newDicItem;
            Log.d("testio", "DicItemViewWrapper is obtained : " + ++sCount);
        }
        sOnEnd.mOnPrev = oldDicItemViewWrapper;
        return sOnEnd;
    }

    public static String flushThouchedWord() {
        String result = "";
        DicItemViewWrapper dicItemViewWrapper = sOnEnd;
        while(dicItemViewWrapper != null) {
            if(!Strings.isNullOrEmpty(dicItemViewWrapper.mTouchedWordString)) {
                result = dicItemViewWrapper.mTouchedWordString;
                dicItemViewWrapper.mTouchedWordString = "";
            }
            dicItemViewWrapper = dicItemViewWrapper.mOnPrev;
        }
        return result;
    }


    public boolean isRecycled() {
        return mIsRecycled;
    }


    public DicItemViewWrapper(Context context) {
        super(context, R.layout.item_wordcard);
        initViews();
    }


    public void setWordCard(WordCard wordCard) {
        mWordCard = wordCard;
        update();
    }

    @Override
    public void update() {
        recycleMeanViewWrappers();
        mTextViewWord.setText(mWordCard.word());
        String phonetic =  mWordCard.phonetic();
        if(!phonetic.isEmpty()) {
            mTextViewPhonetic.setVisibility(View.VISIBLE);
            mTextViewPhonetic.setText("[" + mWordCard.phonetic() + "]");
        }  else {
            mTextViewPhonetic.setVisibility(View.GONE);
            mTextViewPhonetic.setText("");
        }

        List<DescriptionCard> descriptionCardList = mWordCard.getDescriptionCards();
        for(DescriptionCard descriptionCard : descriptionCardList) {
            MeanItemWrapper meanItemWrapper = MeanItemWrapper.obtain(getContext(), (ViewGroup) getView())
                    .setClassOfWord(descriptionCard.wordClass())
                    .setMeanOfWord(descriptionCard.meaning());
            mMeanItemWrapperList.add(meanItemWrapper);
        }
    }



    private void recycleMeanViewWrappers() {
        for(MeanItemWrapper meanItemWrapper : mMeanItemWrapperList) {
            meanItemWrapper.recycle();
        }
    }

    private void initViews() {
        mLayoutHorizontalContentBox = (LinearLayout)findViewById(R.id.linearLayoutHorizontalContentBox);
        mTextViewWord = (TextView)findViewById(R.id.textViewWord);
        mTextViewPhonetic = (TextView)findViewById(R.id.textViewPhonetic);
        mTextViewWord.setOnTouchListener((v,motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchedWordString = mWordCard.word();
            }
            return false;
        });
    }


}
