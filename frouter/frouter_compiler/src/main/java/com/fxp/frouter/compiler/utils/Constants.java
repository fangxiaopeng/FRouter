package com.fxp.frouter.compiler.utils;

/**
 * Title:       Constants
 * <p>
 * Package:     com.fxp.frouter.compiler.utils
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-27 17:27
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
public class Constants {

    /**
     * 注解处理器支持的注解类型
     */
    public static final String FROUTER_ANNOTATION_TYPES = "com.fxp.frouter.annotation.FRouter";

    /**
     * module 名称
     */
    public static final String MODULE_NAME = "muduleName";

    /**
     * 包名，用于存放 APT 生成的类文件
     */
    public static final String APT_PACKAGE = "pkgNameForAPT";

    /**
     * Activity 全类名
     */
    public static final String ACTIVITY = "android.app.Activity";

    /**
     * String 全类名
     */
    public static final String STRING = "java.lang.String";

    /**
     * module api 包名前缀
     */
    public static final String MODULE_API_PACKAGE = "com.fxp.frouter.api";

    /**
     * 路由组 Group 加载
     */
    public static final String FROUTER_GROUP = MODULE_API_PACKAGE + ".core.FRouterLoadGroup";

    /**
     * 路由组 Group 中 Path 加载
     */
    public static final String FROUTER_PATH = MODULE_API_PACKAGE + ".core.FRouterLoadPath";

    /**
     * 路由 Group 中 Path 加载 方法名
     */
    public static final String PATH_METHOD_NAME = "loadPath";

    /**
     * 路由 Group 中 Path 参数名
     */
    public static final String PATH_PARAMETER_NAME = "pathMap";

    /**
     * 路由 Group 中 Path 文件名
     */
    public static final String PATH_FILE_NAME = "FRouter$$Path$$";

    /**
     * 路由 Group 加载 方法名
     */
    public static final String GROUP_METHOD_NAME = "loadGroup";

    /**
     * 路由 Group 中 Group 参数名
     */
    public static final String GROUP_PARAMETER_NAME = "groupMap";

    /**
     * 路由 Group 中 Group 类文件名
     */
    public static final String GROUP_FILE_NAME = "FRouter$$Group$$";

}
