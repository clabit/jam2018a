package com.novato.jam.common;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;


/**
 * Created by Administrator on 2016-01-13.
 */
public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 0; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private int current_page = 1;

    private RecyclerView.LayoutManager mLinearLayoutManager;


    private RecyclerView.OnScrollListener mAdditionalOnScrollListener;
//    private JazzyHelper mHelper;
    private boolean isZaZZy = false;

    public EndlessRecyclerOnScrollListener(RecyclerView.LayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
//        mHelper = new JazzyHelper();
    }

    public void setInit()
    {
        previousTotal = 0;
        loading = true;
        visibleThreshold = 0;
        current_page = 1;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);


        if(isZaZZy)onScrolledAnimation(recyclerView, dx, dy);

        try {

            visibleItemCount = recyclerView.getChildCount();

            boolean mReverse = false;

            if(mLinearLayoutManager instanceof LinearLayoutManager)
                mReverse = ((LinearLayoutManager) mLinearLayoutManager).getReverseLayout();

            if(mLinearLayoutManager instanceof GridLayoutManager)
                mReverse = ((GridLayoutManager) mLinearLayoutManager).getReverseLayout();

            if(mLinearLayoutManager instanceof StaggeredGridLayoutManager)
                mReverse = ((StaggeredGridLayoutManager) mLinearLayoutManager).getReverseLayout();





            if (mLinearLayoutManager == null) {
                Log.e("mun", "LinearLayoutManager null x anr err warning");
            }

            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0));//((LinearLayoutManager) mLinearLayoutManager).findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }


            if (mReverse) {
                //거꾸로기 때문에 스크롤이 맨 위일때 more 호출
                if (!loading && (visibleItemCount + firstVisibleItem) >= totalItemCount && firstVisibleItem > 0) {
                    // End has been reached

                    // Do something
                    current_page++;

                    onLoadMore(current_page);

                    loading = true;
                }
            } else {
                //스크롤이 맨 밑일때 more 호출


                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached

                    // Do something
                    current_page++;

                    onLoadMore(current_page);
                    loading = true;
                }
            }
        }catch (Exception e){
//            MLogger.e("munx","onScrolled:"+e.toString());
        }
    }

    public abstract void onLoadMore(int current_page);










    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

        try {
            if(isZaZZy) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_SETTLING: // fall through
                    case RecyclerView.SCROLL_STATE_DRAGGING:
//                        mHelper.setScrolling(true);
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
//                        mHelper.setScrolling(false);
                        break;
                    default:
                        break;
                }
                notifyAdditionalOnScrollStateChangedListener(recyclerView, newState);
            }
        }catch (Exception e){
//            MLogger.e("munx","onScrollStateChanged:"+e.toString());
        }
    }











    public void onScrolledAnimation(RecyclerView recyclerView, int dx, int dy){

        try {
            int firstVisibleItem = recyclerView.getChildAdapterPosition(recyclerView.getChildAt(0));//((LinearLayoutManager) mLinearLayoutManager).findFirstVisibleItemPosition();//recyclerView.getChildPosition(recyclerView.getChildAt(0));
            int visibleItemCount = recyclerView.getChildCount();
            int totalItemCount = mLinearLayoutManager.getItemCount();//recyclerView.getAdapter().getItemCount();

//            mHelper.onScrolled(recyclerView, firstVisibleItem, visibleItemCount, totalItemCount);

            notifyAdditionalOnScrolledListener(recyclerView, dx, dy);

        }catch (Exception e){
//            MLogger.e("munx","onScrolledAnimation:"+e.toString());
        }


    }

    public void setOnScrollListener(RecyclerView.OnScrollListener l) {
        // hijack the scroll listener setter and have this list also notify the additional listener
        mAdditionalOnScrollListener = l;
    }

    /**
     * Notifies the OnScrollListener of an onScroll event, since JazzyListView is the primary listener for onScroll events.
     */
    private void notifyAdditionalOnScrolledListener(RecyclerView recyclerView, int dx, int dy) {
        if (mAdditionalOnScrollListener != null) {
            mAdditionalOnScrollListener.onScrolled(recyclerView, dx, dy);
        }
    }

    /**
     * Notifies the OnScrollListener of an onScrollStateChanged event, since JazzyListView is the primary listener for onScrollStateChanged events.
     */
    private void notifyAdditionalOnScrollStateChangedListener(RecyclerView recyclerView, int newState) {
        if (mAdditionalOnScrollListener != null) {
            mAdditionalOnScrollListener.onScrollStateChanged(recyclerView, newState);
        }
    }


}
