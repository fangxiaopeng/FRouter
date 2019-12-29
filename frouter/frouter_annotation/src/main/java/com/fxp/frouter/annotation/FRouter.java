package com.fxp.frouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Title:       FRouter
 * <p>
 * Package:     com.fxp.frouter.annotation
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-26 10:48
 * <p>
 * Description:
 *
 * java中元注解有四个： @Retention @Target @Document @Inherited
 *  * @Retention：注解的保留位置
 *  *      @Retention(RetentionPolicy.SOURCE)   //注解仅存在于源码中，在class字节码文件中不包含
 *  *
 *  * 　　　@Retention(RetentionPolicy.CLASS)    // 默认的保留策略，注解会在class字节码文件中存在，但运行时无法获得，
 *  *
 *  * 　　　@Retention(RetentionPolicy.RUNTIME)  // 注解会在class字节码文件中存在，在运行时可以通过反射获取到
 *  *
 *  * @Target 注解的作用目标
 *  * 　　　@Target(ElementType.TYPE)      //接口、类、枚举、注解
 *  *
 *  * 　　　@Target(ElementType.FIELD)     //字段、枚举的常量
 *  *
 *  * 　　　@Target(ElementType.METHOD)    //方法
 *  *
 *  * 　　　@Target(ElementType.PARAMETER) //方法参数
 *  *
 *  * 　　　@Target(ElementType.CONSTRUCTOR)       //构造函数
 *  *
 *  * 　　　@Target(ElementType.LOCAL_VARIABLE)    //局部变量
 *  *
 *  * 　　　@Target(ElementType.ANNOTATION_TYPE)   //注解（使用在另一个注解上）
 *  *
 *  * 　　　@Target(ElementType.PACKAGE)           //包  
 *  *
 *  * @Document：说明该注解将被包含在javadoc中
 *  *
 *  * @Inherited：说明子类可以继承父类中的该注解
 *
 * 生命周期：SOURCE < CLASS < RUNTIME
 *  * 如果需要在运行时动态获取注解信息，用 RUNTIME 注解
 *  * 如果需要在编译时进行一些预处理操作，如 ButterKnife，用 ClASS 注解。注解信息会在class文件中存在，在运行时会被丢弃
 *  * 如果需要做一些检查性的工作，如 @Override、@NotNull等，用 SOURCE 源码注解。注解仅存在源码中，在编译成class字节码时会被丢弃
 *
 * Modification History:
 * <p>
 * Date       Author       Version      Description
 * -----------------------------------------------------------------
 * 2019-12-26    fxp       1.0         First Created
 * <p>
 * Github:  https://github.com/fangxiaopeng
 */
@Target(ElementType.TYPE)           // 作用目标为接口、类、枚举、注解
@Retention(RetentionPolicy.CLASS)   // 注解保留在class字节码中
public @interface FRouter {

    /**
     * 必填
     * @return
     */
    String path();

    /**
     * 选填
     * @return
     */
    String group() default "";
}
