package com.yan.zrefreshview.view;

import ohos.app.Context;

public class UIUtil {

    /**
     * @param context
     * @param vp
     * @return
     */
    public static int vpToPx(Context context, int vp) {
        int scale = context.getResourceManager().getDeviceCapability().screenDensity / 160;
        return scale * vp;
    }
}
