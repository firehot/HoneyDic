package kr.re.dev.MoogleDic.UI;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.List;

import kr.re.dev.MoogleDic.DicData.WordCard;
import rx.Observable;

/**
 * 사전에서 찾은 내용을 출력할 때 사용하는 토스트.
 * 첫 번째 버전에서 좌우 슬라이딩이 가능하다.
 * Created by ice3x2 on 15. 4. 15..
 */
public class DicToast {

    enum Hide { Left, Rigth}

    private Rect        mPosition;
    private Context     mContext;
    private FrameLayout mLayoutContent;
    private ListView    mListViewWordCard;

    private List<WordCard> mWordCardList;


    public void show() {

    }

    private void DicToast() {

    }

    public static void setWordCards(List<WordCard> wordCard) {

    }

    public void changeWordCards(List<WordCard> wordCard) {

    }

    public void setPosition(int top, int right, int width, int height) {

    }
    public void setWidth(int width) {

    }
    public void setHeigth(int height) {

    }
    public void setTop(int top) {

    }
    public void setRight(int right) {

    }
    public void setCenter(int center) {

    }

    public Observable<Hide> getHideEvent() {
        return  null;
    }

    public Observable<WordCard> getSelectWordEvent() {
        return null;
    }


    private void hide() {

    }



    public class WordCardListAdapter extends ArrayAdapter<WordCard> {
        public WordCardListAdapter(Context context, int resource, List<WordCard> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView != null) {
                DicItemViewWrapper dicItem = (DicItemViewWrapper)convertView.getTag();
            } else {
                DicItemViewWrapper dicItem = DicItemViewWrapper.obtain(getContext());
            }
            return convertView;
        }
    }




}
