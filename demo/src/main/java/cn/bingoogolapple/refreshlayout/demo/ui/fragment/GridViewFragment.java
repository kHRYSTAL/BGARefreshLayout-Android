package cn.bingoogolapple.refreshlayout.demo.ui.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.List;

import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildClickListener;
import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildLongClickListener;
import cn.bingoogolapple.refreshlayout.BGAMoocStyleRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import cn.bingoogolapple.refreshlayout.demo.R;
import cn.bingoogolapple.refreshlayout.demo.adapter.NormalAdapterViewAdapter;
import cn.bingoogolapple.refreshlayout.demo.model.RefreshModel;
import retrofit.Callback;
import retrofit.Response;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:15/7/10 17:45
 * 描述:
 */
public class GridViewFragment extends BaseFragment implements BGARefreshLayout.BGARefreshLayoutDelegate, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, BGAOnItemChildClickListener, BGAOnItemChildLongClickListener, View.OnClickListener {
    private BGARefreshLayout mRefreshLayout;
    private GridView mDataGv;
    private NormalAdapterViewAdapter mAdapter;
    private int mNewPageNumber = 0;
    private int mMorePageNumber = 0;

    private boolean mIsNetworkEnabled = false;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.fragment_gridview);
        mRefreshLayout = getViewById(R.id.rl_gridview_refresh);
        mDataGv = getViewById(R.id.lv_gridview_data);
    }

    @Override
    protected void setListener() {
        mRefreshLayout.setDelegate(this);

        mDataGv.setOnItemClickListener(this);
        mDataGv.setOnItemLongClickListener(this);
        mDataGv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                Log.i(TAG, "滚动状态变化");
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                Log.i(TAG, "正在滚动");
            }
        });

        mAdapter = new NormalAdapterViewAdapter(mApp);
        mAdapter.setOnItemChildClickListener(this);
        mAdapter.setOnItemChildLongClickListener(this);

        getViewById(R.id.beginRefreshing).setOnClickListener(this);
        getViewById(R.id.beginLoadingMore).setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
//        mRefreshLayout.setRefreshViewHolder(new BGANormalRefreshViewHolder(this, true));
        mRefreshLayout.setRefreshViewHolder(new BGAMoocStyleRefreshViewHolder(mApp, true));
//        mRefreshLayout.setRefreshViewHolder(new BGAStickinessRefreshViewHolder(this, true));

        mDataGv.setAdapter(mAdapter);
    }

    @Override
    protected void onUserVisible() {
        mNewPageNumber = 0;
        mMorePageNumber = 0;
        mEngine.loadInitDatas().enqueue(new Callback<List<RefreshModel>>() {
            @Override
            public void onResponse(Response<List<RefreshModel>> response) {
                mAdapter.setDatas(response.body());
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(final BGARefreshLayout refreshLayout) {
        if (mIsNetworkEnabled) {
            // 如果网络可用，则加载网络数据

            mNewPageNumber++;
            if (mNewPageNumber > 4) {
                mRefreshLayout.endRefreshing();
                showToast("没有最新数据了");
                return;
            }
            mEngine.loadNewData(mNewPageNumber).enqueue(new Callback<List<RefreshModel>>() {
                @Override
                public void onResponse(Response<List<RefreshModel>> response) {
                    mRefreshLayout.endRefreshing();
                    mAdapter.addNewDatas(response.body());
                }

                @Override
                public void onFailure(Throwable t) {
                    mRefreshLayout.endRefreshing();
                }
            });
        } else {
            // 网络不可用，结束下拉刷新
            showToast("网络不可用");
            mRefreshLayout.endRefreshing();
        }
        // 模拟网络可用不可用
        mIsNetworkEnabled = !mIsNetworkEnabled;
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        Log.i(TAG, "开始加载更多");

        if (mIsNetworkEnabled) {
            // 如果网络可用，则异步加载网络数据，并返回true，显示正在加载更多
            mMorePageNumber++;
            if (mMorePageNumber > 5) {
                mRefreshLayout.endLoadingMore();
                showToast("没有更多数据了");
                return false;
            }
            mEngine.loadMoreData(mMorePageNumber).enqueue(new Callback<List<RefreshModel>>() {
                @Override
                public void onResponse(Response<List<RefreshModel>> response) {
                    mRefreshLayout.endLoadingMore();
                    mAdapter.addMoreDatas(response.body());
                }

                @Override
                public void onFailure(Throwable t) {
                    mRefreshLayout.endLoadingMore();
                }
            });
            // 模拟网络可用不可用
            mIsNetworkEnabled = !mIsNetworkEnabled;
            return true;
        } else {
            // 模拟网络可用不可用
            mIsNetworkEnabled = !mIsNetworkEnabled;

            // 网络不可用，返回false，不显示正在加载更多
            showToast("网络不可用");
            return false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showToast("点击了条目 " + mAdapter.getItem(position).title);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        showToast("长按了" + mAdapter.getItem(position).title);
        return true;
    }

    @Override
    public void onItemChildClick(ViewGroup parent, View childView, int position) {
        if (childView.getId() == R.id.tv_item_normal_delete) {
            mAdapter.removeItem(position);
        }
    }

    @Override
    public boolean onItemChildLongClick(ViewGroup parent, View childView, int position) {
        if (childView.getId() == R.id.tv_item_normal_delete) {
            showToast("长按了删除 " + mAdapter.getItem(position).title);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.beginRefreshing) {
            mRefreshLayout.beginRefreshing();
        } else if (v.getId() == R.id.beginLoadingMore) {
            mRefreshLayout.beginLoadingMore();
        }
    }
}