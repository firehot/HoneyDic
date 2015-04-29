package kr.re.dev.MoogleDic.UI;


import android.content.Context;

import kr.re.dev.MoogleDic.Commons.ViewWrapper;
import kr.re.dev.MoogleDic.DicData.WordCard;

/**
 * DicToast 내부의 단어 뜻을 보여주는 리스트의 아이템 뷰 포함.
 * Created by ice3x2 on 15. 4. 30..
 */
public class DicItemViewWrapper extends ViewWrapper {

    private WordCard mWordCard;

    private static DicItemViewWrapper sRecycledEnd = null;
    private static DicItemViewWrapper sOnEnd = null;
    private DicItemViewWrapper mRecycledPrev;
    private DicItemViewWrapper mOnPrev;
    private boolean mIsRecycled = false;

    public static void recycleAll() {
        DicItemViewWrapper dicItemViewWrapper = sOnEnd;
        while(dicItemViewWrapper != null) {
            sOnEnd = dicItemViewWrapper.mOnPrev;
            dicItemViewWrapper.mIsRecycled = true;
            DicItemViewWrapper oldRecycled =  sRecycledEnd;
            sRecycledEnd = dicItemViewWrapper;
            sRecycledEnd.mRecycledPrev = oldRecycled;
            dicItemViewWrapper = sOnEnd;
        }
    }


    public static DicItemViewWrapper obtain(Context context) {
        DicItemViewWrapper oldDicItemViewWrapper = sOnEnd;
        if(sRecycledEnd == null) {
            sOnEnd =  new DicItemViewWrapper(context);
        }  else {
            DicItemViewWrapper newDicItem =  sRecycledEnd;
            newDicItem.mIsRecycled = false;
            sRecycledEnd = sRecycledEnd.mRecycledPrev;
            sOnEnd = newDicItem;
        }
        sOnEnd.mOnPrev = oldDicItemViewWrapper;
        return sOnEnd;
    }

    public boolean isRecycled() {
        return mIsRecycled;
    }


    public DicItemViewWrapper(Context context) {
        super(context);
        // TODO : 레이아웃 배치 구현.
    }

    public void setWordCard(WordCard wordCard) {
        mWordCard = wordCard;
    }

    @Override
    public void update() {
        // TODO : 뷰 변경 구현.
    }

}
