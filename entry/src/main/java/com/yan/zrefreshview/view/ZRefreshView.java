package com.yan.zrefreshview.view;

import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.*;
import ohos.agp.utils.Color;
import ohos.agp.utils.TextAlignment;
import ohos.app.Context;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Hitomis on 2016/3/2.
 */
public class ZRefreshView extends DirectionalLayout implements Component.LayoutRefreshedListener {

    /**
     * 下拉拖动的黏性比率
     */
    private static final float STICK_RATIO = .618f;

    /**
     * 下拉刷新的回调接口
     */
    private ZRefreshListener mListener;

    /**
     * 下拉头的View
     */
    private RefreshHeader header;


    /**
     * 下拉控件高度
     */
    private int hideHeaderHeight = UIUtil.vpToPx(mContext, 50);

    /**
     * 当前状态
     */
    private RefreshState currentStatus = RefreshState.IDLE_STATE;

    /**
     * 手指按下时屏幕纵坐标
     */
    private float preDownY;


    private int tempHeaderTopMargin;

    public ZRefreshView(Context context) {
        this(context, null);
    }

    public ZRefreshView(Context context, AttrSet attrSet) {
        this(context, attrSet, null);
    }

    public ZRefreshView(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
        setOrientation(VERTICAL);
        if (getChildCount() > 1) {
            throw new RuntimeException("FunGameRefreshView can only contain one View getChildCount=" + getChildCount());
        }
        initView(context, attrSet);
    }

    private void initView(Context context, AttrSet attrs) {
        header = new RefreshHeader(context, attrs);
        addComponent(header, 0);
        setLayoutRefreshedListener(this);
        setScrolledListener(new ScrolledListener() {
            @Override
            public void onContentScrolled(Component component, int i, int i1, int i2, int i3) {
                LogUtil.info(TAG, "onContentScrolled().i=" + i + ",i1=" + i1);
                LogUtil.info(TAG, "onContentScrolled().i2=" + i2 + ",i3=" + i3);
            }
        });
    }


    @Override
    public void onRefreshed(Component component) {
        hideHeaderHeight = -header.getHeight();
        header.setMarginTop(hideHeaderHeight);
        setDraggedListener(1, draggedListener);
        setLayoutRefreshedListener(null);

    }

    private static final String TAG = ZRefreshView.class.getSimpleName();

    private DraggedListener draggedListener = new DraggedListener() {
        @Override
        public void onDragDown(Component component, DragInfo dragInfo) {
        }

        @Override
        public void onDragStart(Component component, DragInfo dragInfo) {
            preDownY = dragInfo.startPoint.getPointYToInt();
        }


        @Override
        public void onDragUpdate(Component component, DragInfo dragInfo) {
            //下拉
            if (currentStatus == RefreshState.REFRESHING_STATE) {
                return;
            }
            float currY = dragInfo.updatePoint.getPointYToInt();
            float distance = currY - preDownY;
            if (distance > 0) {
                float offsetY = distance * STICK_RATIO;
                if (Math.abs(offsetY) > Math.abs(hideHeaderHeight) * 2) {
                    return;
                }
                LogUtil.info(TAG, "onDragUpdate().offsetY=" + offsetY + ",hideHeaderHeight=" + hideHeaderHeight);
                if (offsetY > Math.abs(hideHeaderHeight)) { // 头部全部被下拉出来的时候状态转换为释放刷新
                    currentStatus = RefreshState.RELEASE_TO_REFRESH;
                } else {
                    currentStatus = RefreshState.PULL_TO_REFRESH;
                }
                // 通过偏移下拉头的topMargin值，来实现下拉效果
                header.setMarginTop((int) (offsetY + hideHeaderHeight));
                header.changeWidgetState(currentStatus);
            }
        }

        @Override
        public void onDragEnd(Component component, DragInfo dragInfo) {
            float currY = dragInfo.updatePoint.getPointYToInt();
            float distance = currY - preDownY;
            if (distance > 0) {
                if (currentStatus == RefreshState.PULL_TO_REFRESH) {
                    rollbackHeader();
                } else if (currentStatus == RefreshState.RELEASE_TO_REFRESH) {
                    rollBack2Header();
                }
            } else {
                if (mListener != null) {
                    mListener.onLoadMore();
                }
            }
        }

        @Override
        public void onDragCancel(Component component, DragInfo dragInfo) {
        }
    };


    // 回滚到头部刷新控件的高度，并触发后台刷新任务
    private void rollBack2Header() {
        AnimatorValue rbToHeaderAnimator = new AnimatorValue();
        long duration = (long) (header.getMarginTop() * 1.1f) >= 0 ? (long) (header.getMarginTop() * 1.1f) : 0;
        rbToHeaderAnimator.setDuration(duration);
        rbToHeaderAnimator.setCurveType(Animator.CurveType.ACCELERATE_DECELERATE);
        int topMargin = header.getMarginTop();
        rbToHeaderAnimator.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float v) {
                int marginValue = (int) (topMargin * (1 - v));
                header.setMarginTop(marginValue);
            }
        });
        rbToHeaderAnimator.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
            }

            @Override
            public void onStop(Animator animator) {
            }

            @Override
            public void onCancel(Animator animator) {
            }

            @Override
            public void onEnd(Animator animator) {
                currentStatus = RefreshState.REFRESHING_STATE;
                header.changeWidgetState(currentStatus);
                if (mListener != null) {
                    mListener.onPullRefreshing();
                }
            }

            @Override
            public void onPause(Animator animator) {
            }

            @Override
            public void onResume(Animator animator) {
            }
        });
        rbToHeaderAnimator.start();
    }


    // 回滚下拉刷新头部控件
    private void rollbackHeader() {
        tempHeaderTopMargin = header.getMarginTop();
        AnimatorValue rbAnimator = new AnimatorValue();
        rbAnimator.setDuration(200);
        rbAnimator.setCurveType(Animator.CurveType.DECELERATE);
        rbAnimator.setValueUpdateListener(new AnimatorValue.ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float v) {
                int marginValue = (int) ((header.getHeight() + tempHeaderTopMargin) * v);
                header.setMarginTop(-marginValue + tempHeaderTopMargin);
            }
        });
        rbAnimator.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
            }

            @Override
            public void onStop(Animator animator) {
            }

            @Override
            public void onCancel(Animator animator) {
            }

            @Override
            public void onEnd(Animator animator) {
                if (currentStatus == RefreshState.PULL_TO_REFRESH || currentStatus == RefreshState.FINISHED_TO_REFRESH) {
                    currentStatus = RefreshState.IDLE_STATE;
                    return;
                }
                currentStatus = RefreshState.IDLE_STATE;
            }

            @Override
            public void onPause(Animator animator) {
            }

            @Override
            public void onResume(Animator animator) {
            }
        });
        rbAnimator.start();
    }

    /**
     * 给下拉刷新控件注册一个监听器。
     *
     * @param listener 监听器的实现。
     */
    public void setOnRefreshListener(ZRefreshListener listener) {
        mListener = listener;
    }

    private boolean isEmptyByText(String text) {
        return text == null || text.equals("");
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则将一直处于正在刷新状态。
     */
    public void finishRefreshing() {
        currentStatus = RefreshState.FINISHED_TO_REFRESH;
        header.changeWidgetState(currentStatus);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                rollbackHeader();
            }
        }, 500);
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则将一直处于正在刷新状态。
     */
    public void finishRefreshing(String loadingStr) {
        setLoadingText(loadingStr);
        finishRefreshing();
    }

    /**
     * 设置加载开始文字
     *
     * @param loadingText 加载文字
     */
    public void setLoadingText(String loadingText) {
        if (!isEmptyByText(loadingText)) {
            header.setHeaderLodingStr(loadingText);
        }
    }


    /**
     * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
     */
    public interface ZRefreshListener {
        /**
         * 刷新时回调方法
         */
        void onPullRefreshing();

        void onLoadMore();
    }
}
