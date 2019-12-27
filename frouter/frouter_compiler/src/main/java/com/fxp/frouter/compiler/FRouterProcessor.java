package com.fxp.frouter.compiler;

import com.fxp.frouter.annotation.FRouter;
import com.google.auto.service.AutoService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Title:       FRouterProcessor
 * <p>
 * Package:     com.fxp.frouter.compiler
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-26 15:08
 * <p>
 * Description:
 * <p>
 * <p>
 * Modification History:
 * <p>
 * Date       Author       Version      Description
 * -----------------------------------------------------------------
 * 2019-12-26    fxp       1.0         First Created
 * <p>
 * Github:  https://github.com/fangxiaopeng
 */

/**
 * 通过AutoService自动生成AutoService处理器，用来做注册，在目录下生成对应文件
 */
@AutoService(Processor.class)
/**
 * 需要支持的注解类型
 */
@SupportedAnnotationTypes({"com.fxp.frouter.annotation.FRouter"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
/**
 * 需要接收的apt参数
 */
@SupportedOptions("content")
public class FRouterProcessor extends AbstractProcessor {

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

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementsUtils = processingEnvironment.getElementUtils();
        typesUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        String content = processingEnvironment.getOptions().get("content");
        // 不能用 Diagnostic.Kind.ERROR，会报错
        messager.printMessage(Diagnostic.Kind.NOTE, content);
    }

    /**
     * @Description:  注解处理器的核心方法
     * 处理具体的注解，生成java文件。相当于main函数，开始处理注解
     *
     * @Author:  fxp
     * @Date:    2019-12-26   15:27
     * @param    set                使用了支持处理注解的节点集合（添加了注解的类）
     * @param    roundEnvironment   当前或之前的运行环境，可以通过该对象查找找到的注解
     * @return   boolean            返回true表示已处理完成，后续处理器不会再处理
     * @exception/throws
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) { return true; }

        // 获取项目中左右使用了FRouter注解的节点
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(FRouter.class);
        // 遍历所有的节点
        for (Element element : elements){
            // 获取到包节点
            String packageName = elementsUtils.getPackageOf(element).getQualifiedName().toString();

            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被注解的类：" + className);

            // 最终生成的类文件名，原类名 + "$$FRouter"
            String finalClassName = className + "$$FRouter";
            try {
                JavaFileObject javaFileObject = filer.createClassFile(packageName + "." + finalClassName);
                Writer writer = new BufferedWriter(javaFileObject.openWriter());
                // 获取FRouter注解的path值
                FRouter fRouter = element.getAnnotation(FRouter.class);
                /**
                 * 开始写文件
                 *
                 * package com.fxp.frouter;
                 * public class MainActivity$$FRouter {
                 *
                 *     public static Class<?> findTargetClass(String path){
                 *         if (path.equalsIgnoreCase("/app/MainActivity")){
                 *             return MainActivity.class;
                 *         }
                 *
                 *         return null;
                 *     }
                 * }
                 */
/*                writer.write("package " + packageName + ";\n");
                writer.write("public class " + finalClassName + " {\n");
                writer.write("public static Class<?> findTargetClass(String path){\n");
                writer.write("if(path.equalsIgnoreCase(\"" + fRouter.path() + "\")){\n");
                writer.write("return " + className + ".class;\n");
                writer.write("}\n");
                writer.write("return null;\n}\n");
                writer.write("}");*/
/*                writer.write("package com.fxp.frouter;\n");
                writer.write("public class MainActivity$$FRouter {\n");
                writer.write("public static Class<?> findTargetClass(String path){\n");
                writer.write("if (path.equalsIgnoreCase(\"/app/MainActivity\")){\n");
                writer.write("return MainActivity.class;\n");
                writer.write("}\n");
                writer.write("return null;\n}\n");
                writer.write("}");*/
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
