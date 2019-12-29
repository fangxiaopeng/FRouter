package com.fxp.frouter.compiler;

import com.fxp.frouter.annotation.Param;
import com.fxp.frouter.compiler.factory.ParamFactory;
import com.fxp.frouter.compiler.utils.Constants;
import com.fxp.frouter.compiler.utils.EmptyUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Title:       ParamProcessor
 * <p>
 * Package:     com.fxp.frouter.compiler
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-28 16:57
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
/**
 * 通过AutoService自动生成AutoService处理器，用来做注册，在目录下生成对应文件
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
/**
 * 注解处理器支持的注解类型
 */
@SupportedAnnotationTypes(Constants.PARAMETER_ANNOTATION_TYPES)
public class ParamProcessor extends AbstractProcessor {

    /**
     * 操作Element
     */
    private Elements elementsUtils;

    /**
     * type (类信息)
     */
    private Types typesUtils;

    /**
     * 用来输出警告、错误等日志
     */
    private Messager messager;

    /**
     * 文件生成器
     */
    private Filer filer;

    /**
     * 临时存储 被 @Param 注解的节点，生成 Param 类文件时遍历
     */
    private Map<TypeElement, List<Element>> tempParamMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementsUtils = processingEnvironment.getElementUtils();
        typesUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!EmptyUtils.isEmpty(set)){
            // 获取项目中左右使用了 @Param 注解的节点
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Param.class);
            if (!EmptyUtils.isEmpty(elements)){
                valueTempParamMap(elements);

                try {
                    createParamFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            }
        }

        return false;
    }


    /**
     * @Description: 赋值 tempParamMap
     *
     * @Author:  fxp
     * @Date:    2019-12-28   17:29
     * @param    elements
     * @return   void
     * @exception/throws
     */
    private void valueTempParamMap(Set<? extends Element> elements){
        for (Element element : elements){
            // 注解的属性，父节点是类节点
            TypeElement typeElement = (TypeElement)element.getEnclosingElement();
            if (tempParamMap.containsKey(typeElement)){
                // map 中已有此类节点，将节点添加到List
                tempParamMap.get(typeElement).add(element);
            } else {
                // map 中无此类节点，新建list，将节点添加到list，然后将list放到map
                List<Element> list = new ArrayList<>();
                list.add(element);
                tempParamMap.put(typeElement, list);
            }
        }
    }

    
    /**  
     * @Description:  生成 Param 类文件
     *
     * public class MainActivity$$Param implements ParameterLoad {
     *   @Override
     *   public void loadParam(Object target) {
     *     MainActivity t = (MainActivity)target;
     *     t.userId = t.getIntent().getStringExtra("userId");
     *     t.userAge = t.getIntent().getIntExtra("age", t.userAge);
     *     t.isHappy = t.getIntent().getBooleanExtra("isHappy", t.isHappy);
     *   }
     *
     * @Author:  fxp
     * @Date:    2019-12-28   19:23
     * @param
     * @return   void 
     * @exception/throws
     */  
    private void createParamFile() throws IOException {
        if (EmptyUtils.isEmpty(tempParamMap)) return;

        TypeElement paramType = elementsUtils.getTypeElement(Constants.PARAM_LOAD);

        // 参数体配置(Object target)
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, Constants.PARAM_NAME).build();
        for (Map.Entry<TypeElement, List<Element>> entry : tempParamMap.entrySet()){
            TypeElement typeElement = entry.getKey();
            // 获取类名
            ClassName className = ClassName.get(typeElement);

            // 最终生成的类文件名($$Param)
            String finalClassName = typeElement.getSimpleName() + Constants.PARAM_FILE_NAME;
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 生成跳转传参类文件：" + className.packageName() + "." + finalClassName);

            ParamFactory paramFactory = new ParamFactory.Builder(parameterSpec)
                    .setMessager(messager)
                    .setElementsUtils(elementsUtils)
                    .setTypeUtils(typesUtils)
                    .setClassName(className)
                    .build();

            /**
             * 构建方法体内容
             */
            paramFactory.addFirstStatement();
            for (Element fieldElement : entry.getValue()){
                paramFactory.buildStatement(fieldElement);
            }

            JavaFile.builder(className.packageName(),                   // 包名
                    TypeSpec.classBuilder(finalClassName)               // 类名
                        .addSuperinterface(ClassName.get(paramType))    // 实现ParamLoad接口
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(paramFactory.build())
                        .build())
                    .build()
                    .writeTo(filer);
        }
    }

}
