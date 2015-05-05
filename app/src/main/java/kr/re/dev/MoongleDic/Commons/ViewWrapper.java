/* AndroidExtend
 * ViewController
 * Beom
 * 2011.11.11~ Ver.1.0a
 */

package kr.re.dev.MoongleDic.Commons;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public abstract class ViewWrapper {

	private View view = null;
	private Context mContext = null;
	private Object mTag;

	
	public Context getContext() {
		return mContext;
	}

	public ViewWrapper(Context context) {
		mContext = context;
		FrameLayout layout = new FrameLayout(mContext);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0); // (LayoutParams)layout.getLayoutParams();
		params.width = FrameLayout.LayoutParams.MATCH_PARENT;
		params.height = FrameLayout.LayoutParams.MATCH_PARENT;
		layout.setLayoutParams(params);
		view = layout;
	}
	
	public ViewWrapper(Context context, int ID_Of_Layout) {
		mContext = context;
		LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = vi.inflate(ID_Of_Layout, null);
		if(view == null) {
			throw new NullPointerException(ID_Of_Layout + " inflate fail.");
		}
	}

	public void setTag(Object tag) {
		mTag = tag;
	}
	public Object getTag() {
		return mTag;
	}

	public View getView() {	
		return view;
	}
	public View findViewById(int id) {
		return view.findViewById(id);
	}
	public View addView(ViewWrapper viewWrapper) {
		return addView(viewWrapper.getView());
	}

	public View addView(View child) {
		if(!(view instanceof ViewGroup)) return null;
		ViewGroup viewGroup = ((ViewGroup) view);
		int index = viewGroup.indexOfChild(child);
		if(index >= 0) {
			return viewGroup.getChildAt(index);
		}
		viewGroup.addView(child);
		return child;
	}
	public void removeView(ViewWrapper viewWrapper) {
		removeView(viewWrapper.getView());
	}
	public void removeView(View child) {
		if(!(view instanceof ViewGroup)) return;
		ViewGroup viewGroup = ((ViewGroup) view);
		if(viewGroup.indexOfChild(child) < 0) return;
		viewGroup.removeView(child);
	}


	public abstract void update();
}



