package com.nicholasworkshop.ui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class InfinitePageView extends ViewPager implements OnPageChangeListener
{
	private int mCompensateModeCount = 0;

	private ImageView[] mCompensateImageViews;

	private PagerAdapter mPageViewAdapter;

	private int mDirection;
	private int mCurrent = 1;

	private final int LEFT = 0;
	private final int RIGHT = 2;
	private final String CACHE_DIR = Environment.getExternalStorageDirectory() + "/.nicholasworkshop/cache/";

	private LinkedList<String> mViewTitles = new LinkedList<String>(); // todo:
																		// use
																		// another
																		// type.
	private LinkedList<FrameLayout> mViews = new LinkedList<FrameLayout>();

	private InfinitePageListener mInfinitePageListener;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param attrs
	 */
	public InfinitePageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		super.setOnPageChangeListener(this); // add one in local
		this.setAdapter(mPageViewAdapter);
		this.setCurrentItem(1, false);
		this.mPageViewAdapter = new InfinitePagerAdapter(this);
		this.mCompensateImageViews = new ImageView[2];
	}

	public void setCompensateImageView(int index, ImageView view)
	{
		mCompensateImageViews[index] = view;
	}

	/**
	 * Function to add a view to this pager. For each view added to this pager,
	 * it will be wrapped by a FrameLayout.
	 * 
	 * @param title
	 * @param view
	 */
	public void addPage(String title, View view)
	{
		FrameLayout frameLayout = new FrameLayout(this.getContext());
		frameLayout.addView(view);
		mViews.add(frameLayout);
		mViewTitles.add(title);
		mPageViewAdapter.notifyDataSetChanged();
	}

	/**
	 * Add a view without a title. "Untitled" will be used instead.
	 * 
	 * @param view
	 */
	public void addPage(View view)
	{
		addPage("Untitled", view);
	}

	public FrameLayout getViewAt(int index)
	{
		return mViews.get(index);
	}

	public int getViewCount()
	{
		return mViews.size();
	}

	/**
	 * Return the list of titles.
	 * 
	 * @return
	 */
	@Deprecated
	public String[] getTitles()
	{
		return mViewTitles.toArray(new String[1]);
	}

	/**
	 * Set listener to this pager.
	 * 
	 * @param listener
	 */
	public void setListener(InfinitePageListener listener)
	{
		mInfinitePageListener = listener;
	}

	public void setCompensateModeCount(int count)
	{
		mCompensateModeCount = count;
	}

	// ===================================================
	// OnPageChangeListener
	// ===================================================

	/**
	 * Whenever a page is selected (scrolled to), updated the current direction.
	 */
	@Override
	public void onPageSelected(int position)
	{
		mDirection = position;
		mCurrent += (mDirection == LEFT) ? -1 : 1;
		if (mViews.size() != 0) mCurrent %= mViews.size();
		if (mInfinitePageListener != null) mInfinitePageListener.onPageChanged(mCurrent);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
	{

	}

	/**
	 * Handle the events when pager is scrolling or not.
	 */
	@Override
	public void onPageScrollStateChanged(int state)
	{
		if (state == ViewPager.SCROLL_STATE_IDLE) onPagerIdle();
		else if (state == ViewPager.SCROLL_STATE_DRAGGING) onPagerDragging();
	}

	private void onPagerIdle()
	{
		do {
			switch (mDirection) {
				case LEFT:
					View lastView = mViews.getLast().getChildAt(0);
					mViews.getLast().removeAllViews();
					for (int i = mViews.size() - 1; i > 0; i--) {
						View view = mViews.get(i - 1).getChildAt(0);
						mViews.get(i - 1).removeAllViews();
						mViews.get(i).addView(view);
					}
					mViews.getFirst().addView(lastView);
					break;
				case RIGHT:
					View firstView = mViews.getFirst().getChildAt(0);
					mViews.getFirst().removeAllViews();
					for (int i = 0; i < mViews.size() - 1; i++) {
						View view = mViews.get(i + 1).getChildAt(0);
						mViews.get(i + 1).removeAllViews();
						mViews.get(i).addView(view);
					}
					mViews.getLast().addView(firstView);
			}
		} while (mViews.get(1).getChildAt(0).getTag() != null);
		InfinitePageView.this.setCurrentItem(1, false);
	}

	private void onPagerDragging()
	{
		if (mCompensateModeCount <= 0) return;
		for (int i = 0; i < 2; i++) {
			Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
			mViews.get(i).getChildAt(0).layout(0, 0, display.getWidth(), display.getHeight());
			mViews.get(i).getChildAt(0).setDrawingCacheEnabled(true);
			mViews.get(i).getChildAt(0).buildDrawingCache();
			Bitmap b = mViews.get(i).getChildAt(0).getDrawingCache();
			String path = CACHE_DIR + i + ".jpg";
			try {
				b.compress(CompressFormat.JPEG, 95, new FileOutputStream(path));
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			mCompensateImageViews[i].setImageDrawable(Drawable.createFromPath(path));
		}
	}

}
