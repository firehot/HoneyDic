package kr.re.dev.MoongleDic.UI;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import kr.re.dev.MoongleDic.Commons.ViewWrapper;
import kr.re.dev.MoongleDic.DicData.WordCard;
import kr.re.dev.MoongleDic.R;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * 사전에서 찾은 내용을 출력할 때 사용하는 토스트.
 * 첫 번째 버전에서 좌우 슬라이딩이 가능하다.
 * Created by ice3x2 on 15. 4. 15..
 */
public class DicToast extends ViewWrapper implements View.OnTouchListener{

    public enum HideDirection { Left, Right, NONE}
    enum Status {OnTouch, Show, Hide, OnAnimationEnd,OnAnimationCenter}
    private final static float THRESHOLD_MOVE_END = 0.5f;
    private final static float THRESHOLD_HIDE = 0.7f;
    private final static float THRESHOLD_VELOCITY = 2.0f;
    private final static int DEFAULT_ANIMATION_DURATIONS = 350;
    private final static int SHOW_ANIMATION_DURATIONS = 450;

    private WindowManager.LayoutParams mWindowLayoutParams;
    private ListView    mListViewWordCard;
    private WindowManager mWindowManager;
    private WordCardListAdapter mListAdapter;
    private boolean mIsAttached = false;
    private Status mStatus = Status.Hide;

    // 드래그와 애니메이션등 이동에 관련된 상태 값.
    private VelocityTracker mVelocityTracker;
    private AnimatorSet mCurrentAnimationSet;
    private HideDirection mHideDirection = HideDirection.NONE;
    private float mLeftMoveEndX = 0;
    private float mRightMoveEndX = 0;
    private float mMoveEndX = 0;
    private float mAlpha = 1.0f;
    private int mX = 0;
    private int mViewX = 0;
    private float mStartTouchX = 0;
    private float mT = 0;
    private PublishSubject<HideDirection> mHideEventSubject = PublishSubject.create();

    private void DicToast() {}


    public DicToast(Context context) {
        super(context, R.layout.view_wordcard);
        initToastView();
    }

    private void initToastView() {
        ((ViewGroup)getView()).setClipChildren(false);
        mWindowManager = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
        mWindowLayoutParams =  makeLayoutParams();
        mListViewWordCard = (ListView)findViewById(R.id.listViewWordCard);
        mListViewWordCard.setOnTouchListener(this);
        mListAdapter = new WordCardListAdapter(getContext());
        mListViewWordCard.setAdapter(mListAdapter);
    }

    private WindowManager.LayoutParams makeLayoutParams() {
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS ,
                PixelFormat.TRANSLUCENT);
        windowLayoutParams.gravity = Gravity.TOP | Gravity.CENTER;
        //windowLayoutParams.windowAnimations = R.style.Animation_DicToast;
        return windowLayoutParams;
    }



    public void setListViewWordCard(List<WordCard> wordCardList) {
        DicItemViewWrapper.recycleAll();
        mListAdapter.clear();
        // TODO : 리스트가 길어질 경우 스크롤을 하기 위한 터치를 어색하지 않게 처리해야한다.
        /*Collections.addAll(wordCardList, WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"),
                WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"),
                WordCard.newInstance("test"));*/
        mListAdapter.addAll(wordCardList);
    }

    public Observable<HideDirection> show(List<WordCard> wordCardList) {
        if(wordCardList.isEmpty()) {
            return makeEmptyHiddenEvent();
        }
        setListViewWordCard(wordCardList);
        cancelAnimation();
        addView();
        return getHideEvent();
    }



    @Override
    public void update() {

    }


    /**
     * 에러 발생 존재하지 않음.
     * @return
     */
    public Observable<HideDirection> getHideEvent() {
        return Observable.merge(mHideEventSubject, Observable.empty());
    }
    public Observable<HideDirection> makeEmptyHiddenEvent() {
        PublishSubject<HideDirection> emptySubject = PublishSubject.create();
        emptySubject.onCompleted();
        return emptySubject;
    }


    /**
     * 에러 발생 존재하지 않음.
     * @return
     */
    public Observable<String> getSelectWordEvent() {
        return mListAdapter.wordTouchEvent();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float touchX =  event.getRawX();
        if(event.getAction() == MotionEvent.ACTION_DOWN && mStatus == Status.Show) {
            initThresholdPoint();
            mStartTouchX = touchX;
            mStatus = Status.OnTouch;
        } else if(event.getAction() == MotionEvent.ACTION_DOWN && (mStatus == Status.OnAnimationEnd || Status.OnAnimationCenter == mStatus)) {
            mCurrentAnimationSet.cancel();
            mStartTouchX = touchX;
            mStatus = Status.OnTouch;
        }  else if(event.getAction() == MotionEvent.ACTION_MOVE && mStatus == Status.OnTouch) {
            float moved = touchX - mStartTouchX;
            mStartTouchX = touchX;
            if(mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();

            MotionEvent eventCompute =  MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), event.getRawX(), event.getRawY(), event.getMetaState());
            mVelocityTracker.addMovement(eventCompute);
            eventCompute.recycle();

            if(mViewX < mX) {
                mT = (mViewX  <= mLeftMoveEndX)?1.0f:1.0f - ((mViewX - mLeftMoveEndX) / (mX - mLeftMoveEndX));
                mMoveEndX = mLeftMoveEndX;
                mHideDirection = HideDirection.Left;
            } else {
                mT = (mViewX  >= mRightMoveEndX)?1.0f:1.0f - ((mRightMoveEndX - mViewX ) / (mRightMoveEndX - mX));
                mMoveEndX = mRightMoveEndX;
                mHideDirection = HideDirection.Right;
            }
            mAlpha = 1.0f - mT;
            mViewX += moved;
            mViewX = (mViewX < mLeftMoveEndX)? (int) mLeftMoveEndX :mViewX;
            mViewX= (mViewX > mRightMoveEndX)? (int) mRightMoveEndX :mViewX;
            setViewX(mViewX);
            setAlpha(mAlpha);

        } else if(mStatus == Status.OnTouch) {
            mStatus = Status.OnAnimationCenter;
            float velocity = 0.0f;
            int objX = mX;
            int duration = DEFAULT_ANIMATION_DURATIONS;
            float objAlpha = 1.0f;
            Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(), android.R.interpolator.accelerate_quad);

            if(mVelocityTracker != null) {
                mVelocityTracker.computeCurrentVelocity(1);
                velocity = mVelocityTracker.getXVelocity();
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            } else if(mVelocityTracker != null) {
                mVelocityTracker.recycle();
            }
            if(event.getAction() == MotionEvent.ACTION_UP) {
                // 단어 카드가 일정 위치를 벗어났거나 스와이프가 이루어졌을 때.
                if(mT > THRESHOLD_HIDE || Math.abs(velocity) > THRESHOLD_VELOCITY) {
                    objX = (int)mMoveEndX;
                    objAlpha = 0.0f;
                    interpolator = AnimationUtils.loadInterpolator(getContext(), android.R.interpolator.decelerate_quad);
                    mStatus = Status.OnAnimationEnd;
                }
            }
            startHideAnimation(objX, objAlpha, duration, interpolator);

        }
        return false;
    }


    private void initThresholdPoint() {
        View view = getView();
        WindowManager.LayoutParams layoutParams = ((WindowManager.LayoutParams)view.getLayoutParams());
        int width = view.getWidth();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(displaymetrics);
        mX = layoutParams.x;
        mViewX = mX;
        mAlpha = layoutParams.alpha;
        mMoveEndX = 0;
        mLeftMoveEndX = mX - (width * THRESHOLD_MOVE_END);
        mRightMoveEndX = mX + (width * THRESHOLD_MOVE_END);
    }

    private void cancelAnimation() {
        if(mStatus == Status.OnAnimationEnd || mStatus == Status.OnAnimationCenter) mStatus = Status.Show;
        if(mCurrentAnimationSet == null || !mCurrentAnimationSet.isStarted()) return;
        mCurrentAnimationSet.cancel();
    }

    private void addView() {
        if(!mIsAttached) {
            mHideEventSubject = PublishSubject.create();
            mWindowLayoutParams =  makeLayoutParams();
            mWindowLayoutParams.alpha = 0.0f;
            mWindowManager.addView(getView(), mWindowLayoutParams);
            final int START_SHOW_ANIMATION_DELAY_MS = 10;
            getView().postDelayed(this::startShowAnimation, START_SHOW_ANIMATION_DELAY_MS);
            mIsAttached = true;

        }
        mStatus = Status.Show;
    }

    private void removeView() {
        if(mIsAttached) {
            mWindowManager.removeView(getView());
            mIsAttached = false;
            mHideEventSubject = null;
        }
        mStatus = Status.Hide;
    }

    private void onHideEvent() {
        if(mHideEventSubject != null) {
            mHideEventSubject.onNext(mHideDirection);
            mHideEventSubject.onCompleted();
        }
    }


    private void setViewX(int x) {
        View view = getView();
        WindowManager.LayoutParams layoutParams = ((WindowManager.LayoutParams)view.getLayoutParams());
        layoutParams.x = x;
        mViewX = x;
        if(mIsAttached)
            mWindowManager.updateViewLayout(view, layoutParams);
    }

    private void setViewY(int y) {
        View view = getView();
        WindowManager.LayoutParams layoutParams = ((WindowManager.LayoutParams)view.getLayoutParams());
        layoutParams.y = y;
        if(mIsAttached)
            mWindowManager.updateViewLayout(view, layoutParams);
    }

    private void setAlpha(float alpha) {
        alpha = (alpha > 1.0f)?1.0f:alpha;
        alpha = (alpha < 0.0f)?0.0f:alpha;
        View view = getView();
        WindowManager.LayoutParams layoutParams = ((WindowManager.LayoutParams)view.getLayoutParams());
        layoutParams.alpha = alpha;
        mAlpha = alpha;
        if(mIsAttached)
            mWindowManager.updateViewLayout(view, layoutParams);
    }

    private void startHideAnimation(int objX, float objAlpha, int duration, Interpolator interpolator) {
        ValueAnimator moveAnimator =  ValueAnimator.ofInt(mViewX,objX);
        ValueAnimator alphaAnimator =  ValueAnimator.ofFloat(mAlpha, objAlpha);
        AnimatorSet animatorSet = new AnimatorSet();
        mCurrentAnimationSet = animatorSet;
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(interpolator);
        animatorSet.playTogether(moveAnimator, alphaAnimator);
        moveAnimator.addUpdateListener(animation -> setViewX((Integer) animation.getAnimatedValue()));
        alphaAnimator.addUpdateListener(animation -> setAlpha((Float) animation.getAnimatedValue()));
        animatorSet.addListener(new Animator.AnimatorListener() {
            boolean isCancled = false;
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isCancled) return;

                if (mStatus == Status.OnAnimationEnd) {
                    onHideEvent();
                    removeView();
                } else {
                    mStatus = Status.Show;
                    setViewX(mX);
                    setAlpha(1.0f);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
    }



    private void startShowAnimation() {
        int startY =  (getView().getWidth() / 2) * -1;
        int y =  ((WindowManager.LayoutParams)getView().getLayoutParams()).y;
        ValueAnimator moveAnimator =  ValueAnimator.ofInt(startY,y);
        ValueAnimator alphaAnimator =  ValueAnimator.ofFloat(0,0.1f,0.3f,0.4f,1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        mCurrentAnimationSet = animatorSet;
        animatorSet.setDuration(SHOW_ANIMATION_DURATIONS);
        Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(), android.R.interpolator.decelerate_quad);
        animatorSet.setInterpolator(interpolator);
        animatorSet.playTogether(moveAnimator, alphaAnimator);
        moveAnimator.addUpdateListener(animation -> setViewY((Integer) animation.getAnimatedValue()));
        alphaAnimator.addUpdateListener(animation -> setAlpha((Float) animation.getAnimatedValue()));
        animatorSet.start();
    }




    public class WordCardListAdapter extends ArrayAdapter<WordCard> {
        private PublishSubject<String> mWordTouchSubject = PublishSubject.create();

        public WordCardListAdapter(Context context) {
            super(context, R.layout.item_wordcard);
        }

        @Override
        public void notifyDataSetChanged() {
            DicItemViewWrapper.recycleAll();
            super.notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetInvalidated() {
            DicItemViewWrapper.recycleAll();
            super.notifyDataSetInvalidated();
        }
        public Observable<String> wordTouchEvent() {
           return mWordTouchSubject.asObservable();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WordCard wordCard = getItem(position);
            DicItemViewWrapper dicItem = DicItemViewWrapper.findWith(wordCard);
            if(dicItem == null) {
                dicItem = DicItemViewWrapper.obtain(getContext());
                dicItem.setWordCard(wordCard);
            }
            convertView = dicItem.getView();
            dicItem.setWordSubject(mWordTouchSubject);
            return convertView;
        }
    }





}
