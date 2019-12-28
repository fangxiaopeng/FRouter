package com.fxp.frouter.api.core;

import java.util.Map;

/**
 * Title:       FRouterLoadGroup
 * <p>
 * Package:     com.fxp.frouter.api.core
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-27 17:24
 * <p>
 * Description:
 * <p>
 * <p>
 * Modification History:
 * <p>
 * Date       Author       Version      Description
 * -----------------------------------------------------------------
 * 2019-12-27    fxp       1.0         First Created
 * <p>
 * Github:  https://github.com/fangxiaopeng
 */
public interface FRouterLoadGroup {

    /**
     * 加载路由Group
     * @return
     */
    Map<String, Class<? extends FRouterLoadPath>> loadGroup();

}
