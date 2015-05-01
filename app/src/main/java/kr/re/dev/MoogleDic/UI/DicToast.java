package kr.re.dev.MoogleDic.UI;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import java.util.List;

import kr.re.dev.MoogleDic.Commons.ViewWrapper;
import kr.re.dev.MoogleDic.DicData.WordCard;
import kr.re.dev.MoogleDic.R;
import rx.Observable;

/**
 * 사전에서 찾은 내용을 출력할 때 사용하는 토스트.
 * 첫 번째 버전에서 좌우 슬라이딩이 가능하다.
 * Created by ice3x2 on 15. 4. 15..
 */
public class DicToast extends ViewWrapper {



    @Override
    public void update() {

    }

    enum Hide { Left, Rigth}


    private ListView    mListViewWordCard;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private WindowManager mWindowManager;
    private WordCardListAdapter mListAdapter;
    private boolean mIsAttached = false;


    private void DicToast() {}


    public DicToast(Context context) {
        super(context, R.layout.view_wordcard);
        initToastView();
    }

    private void initToastView() {
        mWindowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        //Display display =  mWindowManager.getDefaultDisplay();
        mWindowLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.CENTER;

        mWindowLayoutParams.windowAnimations = R.style.Animation_DicToast;
        mListViewWordCard = (ListView)findViewById(R.id.listViewWordCard);
        mListAdapter = new WordCardListAdapter(getContext());
        mListViewWordCard.setAdapter(mListAdapter);
    }

    public void setListViewWordCard(List<WordCard> wordCardList) {
        mListAdapter.clear();
        mListAdapter.addAll(wordCardList);
    }

    public void show(List<WordCard> wordCardList) {
        DicItemViewWrapper.recycleAll();
        setListViewWordCard(wordCardList);
        if(mIsAttached == false) {
            mIsAttached = true;
            mWindowManager.addView(getView(),mWindowLayoutParams);
            new Handler().postDelayed(() -> mWindowManager.removeView(getView()), 20000);
        }
        Toast toast;
    }

    /**
     * 에러 발생 존재하지 않음.
     * @return
     */
    public Observable<Hide> getHideEvent() {
        return  null;
    }

    /**
     * 에러 발생 존재하지 않음.
     * @return
     */
    public Observable<WordCard> getSelectWordEvent() {
        return null;
    }



    public class WordCardListAdapter extends ArrayAdapter<WordCard> {
        public WordCardListAdapter(Context context) {
            super(context, R.layout.item_wordcard);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DicItemViewWrapper dicItem = null;
            //if(convertView != null) {
            //} else {
                dicItem = DicItemViewWrapper.obtain(getContext());
                convertView = dicItem.getView();
                //convertView.setTag(dicItem);
            //}
            WordCard wordCard = getItem(position);
            dicItem.setWordCard(wordCard);
            return convertView;
        }
    }




}
