package com.fxp.frouter.compiler.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Title:       EmptyUtils
 * <p>
 * Package:     com.fxp.frouter.compiler.utils
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-27 18:19
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
public class EmptyUtils {

    public static boolean isEmpty(CharSequence charSequence){
        return charSequence == null || charSequence.length() == 0;
    }

    public static boolean isEmpty(Collection<?> collection){
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map){
        return map == null || map.isEmpty();
    }
}
