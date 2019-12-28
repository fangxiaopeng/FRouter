package com.fxp.frouter.compiler;

import com.fxp.frouter.annotation.FRouter;
import com.fxp.frouter.annotation.model.RouterBean;
import com.fxp.frouter.compiler.utils.Constants;
import com.fxp.frouter.compiler.utils.EmptyUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

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
 * 注解处理器支持的注解类型
 */
@SupportedAnnotationTypes({Constants.FROUTER_ANNOTATION_TYPES})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
/**
 * 需要接收的apt参数
 */
@SupportedOptions({Constants.MODULE_NAME, Constants.APT_PACKAGE})
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

    /**
     * 模块名
     */
    private String moduleName;

    /**
     * 包名，用于存放APT生成的类文件
     */
    private String pkgNameForAPT;

    /**
     * 临时存储路由信息
     */
    private Map<String, List<RouterBean>> tempRouterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementsUtils = processingEnvironment.getElementUtils();
        typesUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        Map<String, String> optionsMap = processingEnvironment.getOptions();
        if (!EmptyUtils.isEmpty(optionsMap)){
            moduleName = optionsMap.get(Constants.MODULE_NAME);
            pkgNameForAPT = optionsMap.get(Constants.APT_PACKAGE);
            // 不能用 Diagnostic.Kind.ERROR，会报错
            messager.printMessage(Diagnostic.Kind.NOTE, "FRouter APT Parameters: \n moduleName : " + moduleName + "\n pkgNameForAPT : " + pkgNameForAPT);
        }
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
            // 通过 elementsUtils 获取Activity类型
            TypeElement typeElement = elementsUtils.getTypeElement(Constants.ACTIVITY);
            TypeMirror activityMirror = typeElement.asType();

            // 获取类信息
            TypeMirror elementTypeMirror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, "遍历的节点信息：" + (elementTypeMirror != null ? elementTypeMirror.toString() : ""));

            // 获取到包节点
            String packageName = elementsUtils.getPackageOf(element).getQualifiedName().toString();

            String className = element.getSimpleName().toString();

            // 最终生成的类文件名，原类名 + "$$FRouter"
            String finalClassName = className + "$$FRouter";

            messager.printMessage(Diagnostic.Kind.NOTE, "FRouter：\n 包名：" + packageName + "\n 原类名：" + className + "\n 新生成文件类名：" + finalClassName);

            // 获取FRouter注解的path值
            FRouter fRouter = element.getAnnotation(FRouter.class);

            RouterBean routerBean = new RouterBean.Builder().setElement(element).setGroup(fRouter.group()).setPath(fRouter.path()).build();
            if (typesUtils.isSubtype(elementTypeMirror, activityMirror)){
                routerBean.setType(RouterBean.Type.ACTIVITY);
            } else {
                throw new RuntimeException("FRouter 只能用来注解 Activity");
            }

            TypeElement groupLoadType = elementsUtils.getTypeElement(Constants.FROUTER_GROUP);
            TypeElement pathLoadType = elementsUtils.getTypeElement(Constants.FROUTER_PATH);

            valueTempRouterMap(routerBean);

            // 1，生成路由的Path类文件，如 FRouter$$Path$$app
            try {
                createPathFile(pathLoadType);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 2，生成路由的Group类文件， 如 FRouter$$Group$$app。须先生成 Path 类文件

            /**
             * 生成类文件
             * 下面提供两种方式：javapoet、原生
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

            /**
             * JavaPoet
             *
             * 8个常用类
             * * MethodSpec     用来声明构造函数或方法
             * * TypeSpec       用来声明类、接口、枚举
             * * FieldSpec      用来声明成员变量、字段
             * * JavaFile       顶级类的Java文件
             * * ParameterSpec  用来创建参数
             * * AnnotationSpec 用来创建注解
             * * ClassName      用来包装一个类
             * * TypeName       类型，如方法返回值类型TypeName.VOID
             *
             * 字符串格式化规则
             * * $L     字面量，如 "int value = $L", 10
             * * $S     字符串，如 $S, "abc"
             * * $T     类、接口，如 $T, MainActivity
             * * $N     变量，如 $N, user.$N, name
             *
             */
            try {
                MethodSpec methodSpec = MethodSpec.methodBuilder("findTargetClass")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .returns(Class.class)
                        .addParameter(String.class, "path")
                        .addStatement("return path.equals($S) ? $T.class : null", fRouter.path(), ClassName.get((TypeElement)element))
                        .build();

                TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(methodSpec)
                        .build();

                JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
                javaFile.writeTo(filer);
            } catch (IOException e){
                e.printStackTrace();
            }

/*            try {
                JavaFileObject javaFileObject = filer.createClassFile(packageName + "." + finalClassName);
                Writer writer = new BufferedWriter(javaFileObject.openWriter());
                writer.write("package " + packageName + ";\n");
                writer.write("public class " + finalClassName + " {\n");
                writer.write("public static Class<?> findTargetClass(String path){\n");
                writer.write("return path.equalsIgnoreCase(\"" + fRouter.path() + "\") ? " + className + ".class : null;\n");
                writer.write("}\n");
                writer.write("}");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }

        return false;
    }


    /**
     * @Description:    生成 Path 类文件
     *
     * @Author:  fxp
     * @Date:    2019-12-27   20:06
     * @param    pathLoadType
     * @return   void
     * @exception/throws
     */
    private void createPathFile(TypeElement pathLoadType) throws IOException {
        if (EmptyUtils.isEmpty(tempRouterMap)) return;

        /**
         * 方法的返回值，Map<String, RouterBean>
         */
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class)
        );

        /**
         * 遍历 tempRouterMap，为每个分组都创建一个 Path 类文件
         */
        for (Map.Entry<String, List<RouterBean>> entry : tempRouterMap.entrySet()){
            /**
             * public Map<String, RouterBean> loadPath(){}
             */
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.PATH_METHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturns);
            /**
             * Map<String, RouterBean> pathMap = new HashMap<>();
             */
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    Constants.PATH_PARAMETER_NAME,
                    ClassName.get(HashMap.class));

            /**
             * 循环，将所有 RouterBean put 到 pathMap
             */
            List<RouterBean> pathList = entry.getValue();
            for (RouterBean bean : pathList){
                methodBuilder.addStatement(
                        "$N.put($S, $T.create($T, $L, $T.class, $S, $S))",
                        Constants.PATH_PARAMETER_NAME,           // pathMap
                        bean.getPath(),                               // "/app/MainActivity"
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.Type.class),
                        bean.getType(),                               // 枚举类型 ACTIVITY
                        ClassName.get((TypeElement) bean.getElement()),             // MainActivity.class
                        bean.getPath(),
                        bean.getGroup());                             // "app"
            }

            methodBuilder.addStatement("return $N", Constants.PATH_PARAMETER_NAME);

            String finalClassName = Constants.PATH_FILE_NAME + entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "FRouter APT 生成的 Path 类文件为：" + pkgNameForAPT + "." + finalClassName);

            JavaFile.builder(pkgNameForAPT,                         // 添加包路径
                    TypeSpec.classBuilder(finalClassName)           // 添加类名
                    .addSuperinterface(ClassName.get(pathLoadType)) // 实现接口（FRouterLoadPath）
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodBuilder.build())               // 添加方法
                    .build())
                    .build().writeTo(filer);

        }
    }

    /**
     * @Description: 赋值 tempRouterMap
     *
     * @Author:  fxp
     * @Date:    2019-12-27   19:18
     * @param    bean
     * @return   void
     * @exception/throws
     */
    private void valueTempRouterMap(RouterBean bean){
        if (checkRouterBean(bean)){
            List<RouterBean> routerBeans = tempRouterMap.get(bean.getGroup());
            if (EmptyUtils.isEmpty(routerBeans)){
                routerBeans = new ArrayList<>();
                routerBeans.add(bean);
                tempRouterMap.put(bean.getGroup(), routerBeans);
            } else {
                for (RouterBean routerBean : routerBeans){
                    if (!routerBean.getPath().equalsIgnoreCase(bean.getGroup())){
                        routerBeans.add(routerBean);
                    }
                }
            }
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "FRouter 注解不符合规范");
        }
    }


    /**
     * @Description: 检查RouterBean合法性
     *
     * 借鉴阿里ARouter路由规范
     * group 规范格式：须与module名称一致
     * path 规范格式：/组名/类名，例如：/app/MainActivity
     *
     * @Author:  fxp
     * @Date:    2019-12-27   19:11
     * @param    routerBean
     * @return   boolean
     * @exception/throws
     */
    private boolean checkRouterBean(RouterBean routerBean){
        if (routerBean == null) return false;
        String group = routerBean.getGroup(), path = routerBean.getPath();

        /**
         * @FRouter 注解 path 值必须以"/"开头
         */
        if (EmptyUtils.isEmpty(path) || !path.startsWith("/")){
            return false;
        }

        /**
         * @FRouter 注解 path 值必须包含 moduleName 和 className，即包含两个"/"，标准格式示例 "/app/MainActivity"
         */
        if (path.lastIndexOf("/") == 0){
            // 只包含一个"/"
            return false;
        }

        /**
         * 从 path 中截取 组名，两个"/"之间部分
         */
        String groupInPath = path.substring(1, path.indexOf("/", 1));
        if (groupInPath.contains("/")){
            // 包含两个以上"/"，如 "app/XXX/YYY/MainActivity"
            return false;
        }

        /**
         * group 须与 moduleName 一致
         */
        if (!EmptyUtils.isEmpty(group) && !group.equals(moduleName)){
            // group 与 moduleName 不一致
            return false;
        } else {
            routerBean.setGroup(groupInPath);
        }

        return true;
    }
}
