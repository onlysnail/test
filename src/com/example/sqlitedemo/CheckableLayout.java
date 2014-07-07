package com.example.sqlitedemo;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLayout extends LinearLayout implements Checkable {
	private boolean mChecked;

	public CheckableLayout(Context context) {
		super(context);
	}
	
	public CheckableLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public void setChecked(boolean checked) {
		mChecked = checked;
		setBackgroundDrawable(checked ? getResources().getDrawable(
				R.drawable.blue) : null);
	}

	public boolean isChecked() {
		return mChecked;
	}

	public void toggle() {
		setChecked(!mChecked);
	}

}