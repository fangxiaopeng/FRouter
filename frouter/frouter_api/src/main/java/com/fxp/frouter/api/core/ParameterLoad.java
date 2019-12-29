package com.fxp.frouter.api.core;

/**
 * Title:       ParameterLoad
 * <p>
 * Package:     com.fxp.frouter.api.core
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-28 16:56
 * <p>
 * Description:
 * <p>
 * <p>
 * Modification History:
 * <p>
 * Date       Author       Version      Description
 * -----------------------------------------------------------------
 * 2019-12-28    fxp       1.0         First Created
 * <p>
 * Github:  https://github.com/fangxiaopeng
 */
public interface ParameterLoad {

    /**
     * 目标对象(Activity)
     * target.getIntent().get...
     * @param target
     */
    void loadParam(Object target);

}
