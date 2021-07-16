package com.yan.zrefreshview.slice;

import com.yan.zrefreshview.ResourceTable;
import com.yan.zrefreshview.view.ZRefreshView;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;

public class MainAbilitySlice extends AbilitySlice {
    private ZRefreshView refreshView;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        refreshView = (ZRefreshView) findComponentById(ResourceTable.Id_zrefresh_view);
        refreshView.setOnRefreshListener(new ZRefreshView.ZRefreshListener() {
            @Override
            public void onPullRefreshing() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        getUITaskDispatcher().asyncDispatch(new Runnable() {
                            @Override
                            public void run() {
                                refreshView.finishRefreshing("本次更新100条数据");
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onLoadMore() {

            }
        });
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
