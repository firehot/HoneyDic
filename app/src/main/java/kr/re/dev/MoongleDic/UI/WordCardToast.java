package kr.re.dev.MoongleDic.UI;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
public class WordCardToast extends ViewWrapper implements View.OnTouchListener{

    public enum HideDirection { Left, Right, NONE}
    enum Status {OnTouch, Show, Hide, OnAnimationEnd,OnAnimationCenter}
    private final static float THRESHOLD_MOVE_END = 0.5f;
    private final static float THRESHOLD_HIDE = 0.7f;
    private final static float THRESHOLD_VELOCITY = 2.0f;
    private final static int DEFAULT_ANIMATION_DURATIONS = 350;
    private final static int SHOW_ANIMATION_DURATIONS = 450;
    private final static int START_SHOW_ANIMATION_DELAY_MS = 10;
    private Status mStatus = Status.Hide;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private ListView    mListViewWordCard;
    private WindowManager mWindowManager;
    private WordCardListAdapter mListAdapter;
    private FrameLayout mShadowView;
    private FrameLayout mLayoutShadowContent;
    private boolean mIsAttached = false;

    // 드래그와 애니메이션등 이동에 관련된 상태 값.
    private VelocityTracker mVelocityTracker;
    private AnimatorSet mCurrentAnimationSet;
    private HideDirection mHideDirection = HideDirection.NONE;
    private float mLeftMoveEndX = 0;
    private float mRightMoveEndX = 0;
    private float mMoveEndX = 0;
    private float mAlpha = 1.0f;
    private int mX = 0, mY = 0;
    private int mViewX = 0, mViewY = 0, mShadowViewX = 0, mShadowViewY = 0;
    private float mStartTouchX = 0;
    private float mT = 0;


    private float mLastViewX = 0;
    private float mLastViewY = 0;
    private Bitmap mCachedBitmap = null;
    private int mLastShadowViewWidth = 0;

    private PublishSubject<HideDirection> mHideEventSubject = PublishSubject.create();
    private PublishSubject<String> mWordTouchSubject = PublishSubject.create();
    private ImageView mImageViewCahced;

    private void DicToast() {}

    public static WordCardToast newInstance(Context context) {
        return new WordCardToast(context);
    }

    public WordCardToast(Context context) {
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
        if(isOverLollipop()) {
            getView().setBackground(null);
            mShadowView = (FrameLayout)((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_wordcard_shadow, null);
            mLayoutShadowContent = (FrameLayout)mShadowView.findViewById(R.id.frameLayoutSahdow);
            mImageViewCahced = (ImageView)mShadowView.findViewById(R.id.imageViewShadowWordCard);
            mShadowView.setClipChildren(true);
        }
    }





    private WindowManager.LayoutParams makeLayoutParams() {
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        windowLayoutParams.gravity = Gravity.TOP | Gravity.CENTER;
        return windowLayoutParams;
    }

    private WindowManager.LayoutParams makeShadowLayoutParams() {
        WindowManager.LayoutParams windowLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        windowLayoutParams.gravity = Gravity.TOP | Gravity.CENTER;
        return windowLayoutParams;
    }

    public void setListViewWordCard(List<WordCard> wordCardList) {
        mListAdapter.clean();
        // TODO : 리스트가 길어질 경우 스크롤을 하기 위한 터치를 어색하지 않게 처리해야한다.
//        Collections.addAll(wordCardList, WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"),
//                WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"), WordCard.newInstance("test"),
//                WordCard.newInstance("test"));
        mListAdapter.addAll(wordCardList);
    }

    public Observable<HideDirection> show(List<WordCard> wordCardList) {
        if(wordCardList.isEmpty()) {
            return makeEmptyHiddenEvent();
        }
        setListViewWordCard(wordCardList);
        cancelAnimation();
        releaseCachedBitmap();
        if(mIsAttached && isOverLollipop()) {
            getView().post(() -> {
                fitShadowViewSize();
                setViewY(mShadowViewY);
            });
        } else {
            addView();
        }
        return getHideEvent();
    }

    private void hide() {
        onHideEvent();
        removeView();
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
        return mWordTouchSubject.asObservable();
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float touchX =  event.getRawX();
        if(event.getAction() == MotionEvent.ACTION_DOWN && mStatus == Status.Show) {
            initThresholdPoint();
            mStartTouchX = touchX;
            mStatus = Status.OnTouch;
            cacheToShadow();
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
            String touchedWord = DicItemViewWrapper.flushThouchedWord();
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
                //Log.i("testio", "velocity : " + velocity + "   " + "T : " + mT);
                if(mT > THRESHOLD_HIDE || Math.abs(velocity) > THRESHOLD_VELOCITY) {
                    objX = (int)mMoveEndX;
                    objAlpha = 0.0f;
                    interpolator = AnimationUtils.loadInterpolator(getContext(), android.R.interpolator.decelerate_quad);
                    mStatus = Status.OnAnimationEnd;
                } else {
                    if(!touchedWord.isEmpty()) mWordTouchSubject.onNext(touchedWord);
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
        mY = layoutParams.y;
        mViewX = mX;
        mViewY = layoutParams.y;
        mLastViewX = mViewX;
        mLastViewY = mViewY;
        mAlpha = layoutParams.alpha;
        mT = 0;
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
            addShadowView();
            mWindowManager.addView(getView(), mWindowLayoutParams);
            getView().postDelayed(this::startShowAnimation, START_SHOW_ANIMATION_DELAY_MS);
            mIsAttached = true;
            if(isOverLollipop()) {
                getView().getViewTreeObserver().addOnGlobalLayoutListener(mOnWordCardGlobalLayoutListener);
                mShadowView.getViewTreeObserver().addOnGlobalLayoutListener(mOnShadowViewGlobalLayoutListener);
            }
        }
        mStatus = Status.Show;
    }


    private void addShadowView() {
        if(!isOverLollipop()) return;
        WindowManager.LayoutParams layoutParams =  makeShadowLayoutParams();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLayoutShadowContent.getLayoutParams();
        params.gravity = Gravity.CENTER;
        mLayoutShadowContent.setLayoutParams(params);
        mWindowManager.addView(mShadowView, layoutParams);
    }


    private void removeView() {
        if(mIsAttached) {
            if(isOverLollipop()) {
                getView().getViewTreeObserver().removeOnGlobalLayoutListener(mOnWordCardGlobalLayoutListener);
                mShadowView.getViewTreeObserver().removeOnGlobalLayoutListener(mOnShadowViewGlobalLayoutListener);
            }
            removeShadowView();

            mWindowManager.removeViewImmediate(getView());
            mIsAttached = false;
            mHideEventSubject = null;
            mListAdapter.clean();
            mListViewWordCard.setOnTouchListener(null);
            ((ViewGroup)getView()).removeAllViews();
        }
        mStatus = Status.Hide;
        releaseCachedBitmap();
    }

    private void removeShadowView() {
        if(!isOverLollipop()) return;
        mWindowManager.removeViewImmediate(mShadowView);
        mLayoutShadowContent.removeAllViews();
    }


    /**
     * 롤리팝 이상에서만 동작. 화면 회전시 쉐도우 뷰의 사이즈와 위치를 다시 설정한다.
     */
    private ViewTreeObserver.OnGlobalLayoutListener mOnWordCardGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            fitShadowViewSize();
            fitShadowViewPoint();
            int movedX = mViewX - mX;
            int movedY = mViewY - mY;
            mShadowViewX = (int) (movedX + mLayoutShadowContent.getX());
            mShadowViewY = movedY;
            //Log.i("testio", "shadowY : " + mShadowViewY + "   shadowX : " + mShadowViewX);
            mLayoutShadowContent.setX(mShadowViewX);
            mLayoutShadowContent.setY(mShadowViewY);
            mLayoutShadowContent.setAlpha(mAlpha);


        }
    };
    private ViewTreeObserver.OnGlobalLayoutListener mOnShadowViewGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if(mLastShadowViewWidth != mShadowView.getWidth()) {
                // 아, 이런 구현 ㅈㄴ 싫어하는데, 정말 어쩔 수 없다;;
                getView().postDelayed(()-> setViewX(mViewX),1);
                mLastShadowViewWidth = mShadowView.getWidth();
                //Log.i("testio", "rotation");
            }
        }
    };


    private void onHideEvent() {
        if(mHideEventSubject != null) {
            mHideEventSubject.onNext(mHideDirection);
            mHideEventSubject.onCompleted();
        }
        mWordTouchSubject.onCompleted();
    }

    private void setShadowViewXFrom(float moveX) {
        if(!isOverLollipop()) return;
        mShadowViewX += moveX;
        mLayoutShadowContent.setX(mShadowViewX);
    }

    private void setShadowViewYFrom(float moveY) {
        if(!isOverLollipop()) return;
        mShadowViewY += moveY;
        mLayoutShadowContent.setY(mShadowViewY);
    }
    private void setShadowViewAlpha(float alpha) {
        if(!isOverLollipop()) return;
        mLayoutShadowContent.setAlpha(alpha);
    }



    private boolean isOverLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    }


    private void setViewX(int x) {
        View view = getView();
        WindowManager.LayoutParams layoutParams = ((WindowManager.LayoutParams)view.getLayoutParams());
        layoutParams.x = x;
        float lastMoved = x - mLastViewX;
        mViewX = x;
        mLastViewX = x;
        if(mIsAttached)
            mWindowManager.updateViewLayout(view, layoutParams);
        //setShadowViewXFrom(lastMoved);

    }

    private void setViewY(int y) {
        View view = getView();
        WindowManager.LayoutParams layoutParams = ((WindowManager.LayoutParams)view.getLayoutParams());
        layoutParams.y = y;
        mViewY = y;
        mLastViewY = y;
        if(mIsAttached)
            mWindowManager.updateViewLayout(view, layoutParams);
        //setShadowViewYFrom(lastMoved);
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
        //setShadowViewAlpha(alpha);

    }

    private void startHideAnimation(int objX, float objAlpha, int duration, Interpolator interpolator) {
        ValueAnimator moveAnimator =  ValueAnimator.ofInt(mViewX, objX);
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
                uncacheFromShadow();
                if (mStatus == Status.OnAnimationEnd) {
                    hide();
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
        initThresholdPoint();
        AtomicInteger isChangedFit = new AtomicInteger(1);
        ValueAnimator moveAnimator =  ValueAnimator.ofInt(startY,y);
        ValueAnimator alphaAnimator =  ValueAnimator.ofFloat(0, 0.1f, 0.3f, 0.4f, 1.0f);
        AnimatorSet animatorSet = new AnimatorSet();
        mCurrentAnimationSet = animatorSet;
        animatorSet.setDuration(SHOW_ANIMATION_DURATIONS);
        Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(), android.R.interpolator.decelerate_quad);
        animatorSet.setInterpolator(interpolator);
        if(isOverLollipop()) {

        }

        animatorSet.playTogether(moveAnimator, alphaAnimator);
        moveAnimator.addUpdateListener(animation -> {
            setViewY((Integer) animation.getAnimatedValue());
            if(isOverLollipop()) {
                setShadowViewXFrom(0);
                if(isChangedFit.get() == 1)
                    isChangedFit.set(fitShadowViewSize());
            }
            //if (fitShadowViewSize() == 1) cacheToShadow();
        });
        alphaAnimator.addUpdateListener(animation -> setAlpha((Float) animation.getAnimatedValue()));
        fitShadowViewSize();
        //if(fitShadowViewSize() == 1) cacheToShadow();
        fitShadowViewPoint();
        setShadowViewAlpha(0.0f);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                //getView().post(WordCardToast.this::uncacheFromShadow);
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        animatorSet.start();
    }

    /**
     * 사용할 수 없으면 -1 <br/>
     * 이 메소드를 통하여 사이즈를 맞췄다면 1 <br/>
     * 이미 사이즈가 맞춰져 있다면 0 <br/>
     * @return
     */
    private int fitShadowViewSize() {
        if(!isOverLollipop()) return -1;
        if(mLayoutShadowContent.getWidth() != getView().getWidth() ||
                mLayoutShadowContent.getHeight() != getView().getHeight()) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLayoutShadowContent.getLayoutParams();
            params.width = getView().getWidth();
            params.height = getView().getHeight();
            mLayoutShadowContent.requestLayout();
            return 1;
        }
        return 0;
    }

    private void fitShadowViewPoint() {
        if(!isOverLollipop()) return;
        int width = getView().getWidth();
        int x = (mShadowView.getWidth() / 2) -  (width / 2);
        mShadowViewX = x;
        mLayoutShadowContent.setX(x);
        mLayoutShadowContent.requestLayout();
    }

    private void cacheToShadow() {
        if(!isOverLollipop()) return;
        cacheToShadowImmediately();
        mImageViewCahced.postDelayed(() -> getView().setVisibility(View.INVISIBLE), 100);
    }

    private void cacheToShadowImmediately() {
        if(!isOverLollipop()) return;
        if(getView().getVisibility() != View.VISIBLE) return;
        releaseCachedBitmap();
        if(mCachedBitmap == null || mCachedBitmap.isRecycled()) {
            Bitmap bitmap = Bitmap.createBitmap(mListViewWordCard.getWidth(), mListViewWordCard.getHeight(), Bitmap.Config.ARGB_4444);
            mCachedBitmap = bitmap;
        }
        mImageViewCahced.setVisibility(View.VISIBLE);
        Canvas canvas = new Canvas(mCachedBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        mListViewWordCard.draw(canvas);
        mImageViewCahced.setImageBitmap(mCachedBitmap);
    }

    private void uncacheFromShadow() {
        if(!isOverLollipop() || getView().getVisibility() == View.VISIBLE) return;
        mImageViewCahced.postDelayed(() -> {
            if (mStatus == Status.Show || mStatus == Status.Hide ) {
                mImageViewCahced.setImageBitmap(null);
                mImageViewCahced.setVisibility(View.INVISIBLE);
            }
        }, 100);
        getView().setVisibility(View.VISIBLE);
    }

    private void releaseCachedBitmap() {
        if(mCachedBitmap != null && !mCachedBitmap.isRecycled()) {
            mImageViewCahced.setImageBitmap(null);
            mCachedBitmap.recycle();
        }
    }

    public class WordCardListAdapter extends ArrayAdapter<WordCard> {
        public WordCardListAdapter(Context context) {
            super(context, R.layout.item_wordcard);
        }
        public List<DicItemViewWrapper> aliveDicItemViewWrapper = Lists.newArrayList();

        public void clean() {
            clear();
            for(DicItemViewWrapper dicItemViewWrapper : aliveDicItemViewWrapper) {
                dicItemViewWrapper.cleanMeanItems();
                dicItemViewWrapper.getView().setTag(null);
            }
            aliveDicItemViewWrapper.clear();
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            WordCard wordCard = getItem(position);
            DicItemViewWrapper dicItemViewWrapper;
            if(convertView == null || convertView.getTag() == null) {
                dicItemViewWrapper = new DicItemViewWrapper(getContext());
                aliveDicItemViewWrapper.add(dicItemViewWrapper);
                convertView = dicItemViewWrapper.getView();
                convertView.setTag(dicItemViewWrapper);
            } else {
                dicItemViewWrapper = (DicItemViewWrapper)convertView.getTag();
            }
            dicItemViewWrapper.setWordCard(wordCard);
            return convertView;
        }


    }




}
