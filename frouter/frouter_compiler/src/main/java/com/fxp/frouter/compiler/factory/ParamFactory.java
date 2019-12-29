package com.fxp.frouter.compiler.factory;

import com.fxp.frouter.annotation.Param;
import com.fxp.frouter.compiler.utils.Constants;
import com.fxp.frouter.compiler.utils.EmptyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Title:       ParamFactory
 * <p>
 * Package:     com.fxp.frouter.compiler.factory
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-28 17:33
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
public class ParamFactory {

    /**
     * 方法体构建
     */
    private MethodSpec.Builder methodBuilder;

    /**
     * 用来输出警告、错误等日志
     */
    private Messager messager;

    /**
     * type (类信息)工具类，提供用来操作 TypeMirror 的方法
     */
    private Types typesUtils;

    /**
     * 获取元素接口信息
     */
    private TypeMirror callMirror;

    /**
     * 用来包装一个类
     */
    private ClassName className;

    private ParamFactory(Builder builder){
        this.messager = builder.messager;
        this.className = builder.className;
        typesUtils = builder.typeUtils;

        /**
         * 构建方法体
         * public void loadParam(Object target){}
         */
        methodBuilder = MethodSpec.methodBuilder(Constants.PARAM_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);
    }

    public void addFirstStatement(){
        /**
         * 格式转换
         * MainActivity t = (MainActivity)target;
         */
        methodBuilder.addStatement("$T t = ($T)target", className, className);
    }

    public MethodSpec build(){
        return methodBuilder.build();
    }

    /**
     * 构建方法内容
     *     t.userId = t.getIntent().getStringExtra("userId");
     *     t.userAge = t.getIntent().getIntExtra("age", t.userAge);
     *     t.isHappy = t.getIntent().getBooleanExtra("isHappy", t.isHappy);
     *
     * @param element 被注解的属性元素
     */
    public void buildStatement(Element element){

        // 获取属性名
        String fieldName = element.getSimpleName().toString();

        // 获取注解的值
        String annotationValue = element.getAnnotation(Param.class).name();

        /**
         * 注解值为空时，使用节点属性值
         * 例如
         * @Param
         * String userId;
         * 因注解值 name 为空，所以将取 userId
         */
        annotationValue = EmptyUtils.isEmpty(annotationValue) ? fieldName : annotationValue;

        // 最终拼接的前缀
        String finalValue = "t." + fieldName;

        // t.s = t.getIntent()
        String methodContent = finalValue + " = t.getIntent().";

        // 获取 TypeKind 枚举类型的序列号
        TypeMirror typeMirror = element.asType();
        int type = typeMirror.getKind().ordinal();
        if (type == TypeKind.INT.ordinal()){
            methodContent += "getIntExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.BOOLEAN.ordinal()){
            methodContent += "getBooleanExtra($S, " + finalValue + ")";
        } else {
            if (typeMirror.toString().equalsIgnoreCase(Constants.STRING)){
                // 为 String
                methodContent += "getStringExtra($S)";
            } else {
                // TODO int、String、boolean 之外类型参数

            }
        }

        if (methodContent.endsWith(")")){
            /**
             * 代码合乎规范
             */
            methodBuilder.addStatement(methodContent, annotationValue);
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "只支持int、String、boolean型参数");
        }
    }

    public static class Builder{

        private Messager messager;

        private Elements elementsUtils;

        private Types typeUtils;

        private ClassName className;

        private ParameterSpec parameterSpec;

        public Builder(ParameterSpec parameterSpec){
            this.parameterSpec = parameterSpec;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setElementsUtils(Elements elementsUtils) {
            this.elementsUtils = elementsUtils;
            return this;
        }

        public Builder setTypeUtils(Types typeUtils) {
            this.typeUtils = typeUtils;
            return this;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public ParamFactory build(){
            if (parameterSpec == null){
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if (className == null){
                throw new IllegalArgumentException("className为空");
            }

            if (messager == null){
                throw new IllegalArgumentException("messager为空");
            }

            return new ParamFactory(this);
        }
    }
}
