package com.fxp.frouter.api.core;

import com.fxp.frouter.annotation.model.RouterBean;

import java.util.Map;

/**
 * Title:       FRouterLoadPath
 * <p>
 * Package:     com.fxp.frouter.api.core
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-27 17:19
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
public interface FRouterLoadPath {

    /**
     * 加载路由Group中的Path
     * @return
     */
    Map<String, RouterBean> loadPath();

}
