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
import com.squareup.javapoet.WildcardTypeName;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
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
     * 临时存储路由 Group 中 Path 类对象,生成 Path 类文件时遍历
     */
    private Map<String, List<RouterBean>> tempPathMap = new HashMap<>();

    /**
     * 临时存储路由 Group 类对象,生成 Group 类文件时遍历
     */
    private Map<String, String> tempGroupMap = new HashMap<>();

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
            // 获取到包节点
            String packageName = elementsUtils.getPackageOf(element).getQualifiedName().toString();

            String className = element.getSimpleName().toString();

            // 最终生成的类文件名，原类名 + "$$FRouter"
            String finalClassName = className + "$$FRouter";

            messager.printMessage(Diagnostic.Kind.NOTE, "FRouter：\n 包名：" + packageName + "\n 原类名：" + className + "\n 新生成文件类名：" + finalClassName);

            // 获取FRouter注解，为了拿到 group、path 值
            FRouter fRouter = element.getAnnotation(FRouter.class);

            // 封装 RouterBean 对象
            RouterBean routerBean = new RouterBean.Builder().setElement(element).setGroup(fRouter.group()).setPath(fRouter.path()).build();

            // 通过 elementsUtils 获取 Activity 类节点信息
            TypeMirror activityMirror = elementsUtils.getTypeElement(Constants.ACTIVITY).asType();
            // 获取当前节点类信息
            TypeMirror elementTypeMirror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, "当前类节点信息：" + (elementTypeMirror != null ? elementTypeMirror.toString() : ""));

            // 判断当前被注解的类是否为 Activity
            if (typesUtils.isSubtype(elementTypeMirror, activityMirror)){
                routerBean.setType(RouterBean.Type.ACTIVITY);
            } else {
                throw new RuntimeException("FRouter 只能用来注解 Activity");
            }

            valueTempRouterMap(routerBean);

            try {
                TypeElement groupLoadType = elementsUtils.getTypeElement(Constants.FROUTER_GROUP);
                TypeElement pathLoadType = elementsUtils.getTypeElement(Constants.FROUTER_PATH);

                // 1，生成路由的Path类文件，如 FRouter$$Path$$app
                createPathFile(pathLoadType);

                // 2，生成路由的Group类文件， 如 FRouter$$Group$$app。须先生成 Path 类文件
                createGroupFile(groupLoadType, pathLoadType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    /**
     * @Description: 创建类文件示例
     *
     * 使用两种方式：javapoet、原生，生成如下类文件
     *
     * package com.fxp.frouter;
     * public class MainActivity$$FRouter {
     *     public static Class<?> findTargetClass(String path){
     *         return path.equalsIgnoreCase("/app/MainActivity") ? MainActivity.class : null;
     *     }
     * }
     *
     * @Author:  fxp
     * @Date:    2019-12-27   12:33
     * @param    element
     * @return   void
     * @exception/throws
     */
    private void createFileExample(Element element) throws IOException {
        FRouter fRouter = element.getAnnotation(FRouter.class);
        // 获取到包节点
        String packageName = elementsUtils.getPackageOf(element).getQualifiedName().toString();
        String className = element.getSimpleName().toString();
        // 最终生成的类文件名，原类名 + "$$FRouter"
        String finalClassName = className + "$$FRouter";

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
         */

        // 声明方法
        MethodSpec methodSpec = MethodSpec.methodBuilder("findTargetClass")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(Class.class)
                .addParameter(String.class, "path")
                .addStatement("return path.equals($S) ? $T.class : null", fRouter.path(), ClassName.get((TypeElement)element))
                .build();

        // 声明类，并将方法添加到类
        TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(methodSpec)
                .build();

        // 构建类文件，将类添加到指定包名下
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

        // 写文件
        javaFile.writeTo(filer);

        /**
         * 原生
         */
        JavaFileObject javaFileObject = filer.createClassFile(packageName + "." + finalClassName);
        Writer writer = new BufferedWriter(javaFileObject.openWriter());
        writer.write("package " + packageName + ";\n");
        writer.write("public class " + finalClassName + " {\n");
        writer.write("public static Class<?> findTargetClass(String path){\n");
        writer.write("return path.equalsIgnoreCase(\"" + fRouter.path() + "\") ? " + className + ".class : null;\n");
        writer.write("}\n");
        writer.write("}");
        writer.close();
    }


    /**
     * @Description:    生成 Path 类文件
     *
     * public class FRouter$$Path$$app implements FRouterLoadPath {
     *   @Override
     *   public Map<String, RouterBean> loadPath() {
     *     Map<String, RouterBean> pathMap = new HashMap<>();
     *     pathMap.put("/app/MainActivity", RouterBean.create(RouterBean.Type.ACTIVITY, MainActivity.class, "/app/MainActivity", "app"));
     *     return pathMap;
     *   }
     * }
     *
     * @Author:  fxp
     * @Date:    2019-12-27   20:06
     * @param    pathLoadType
     * @return   void
     * @exception/throws
     */
    private void createPathFile(TypeElement pathLoadType) throws IOException {
        if (EmptyUtils.isEmpty(tempPathMap)) return;

        /**
         * 方法的返回值，Map<String, RouterBean>
         */
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class)
        );

        /**
         * 遍历 tempPathMap，为每个分组都创建一个 Path 类文件
         */
        for (Map.Entry<String, List<RouterBean>> entry : tempPathMap.entrySet()){
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
             * pathMap.put("/app/MainActivity", RouterBean.create(RouterBean.Type.ACTIVITY, MainActivity.class, "/app/MainActivity", "app"));
             */
            List<RouterBean> pathList = entry.getValue();
            for (RouterBean bean : pathList){
                methodBuilder.addStatement(
                        "$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
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

            tempGroupMap.put(entry.getKey(), finalClassName);
        }
    }

    /**
     * @Description: 生成 Group 类文件
     *
     * public class FRouter$$Group$$app implements FRouterLoadGroup {
     *   @Override
     *   public Map<String, Class<? extends FRouterLoadPath>> loadGroup() {
     *     Map<String, Class<? extends FRouterLoadPath>> groupMap = new HashMap<>();
     *     groupMap.put("app", FRouter$$Path$$app.class);
     *     return groupMap;
     *   }
     * }
     *
     * @Author:  fxp
     * @Date:    2019-12-28   12:21
     * @param    groupLoadType
     * @param    pathLoadType
     * @return   void
     * @exception/throws
     */
    private void createGroupFile(TypeElement groupLoadType, TypeElement pathLoadType) throws IOException {
        if (EmptyUtils.isEmpty(tempGroupMap) || EmptyUtils.isEmpty(tempPathMap)) return;

        /**
         * 方法的返回值，Map<String, Class<? extends FRouterLoadPath>>
         * 第二个参数 Class<? extends FRouterLoadPath> 为 FRouterLoadPath 接口的实现类
         */
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(pathLoadType)))
        );

        /**
         * public Map<String, Class<? extends FRouterLoadPath>> loadGroup(){}
         */
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)      // 实现FRouterLoadPath接口，重新loadGroup方法
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturn);

        /**
         * Map<String, String> groupMap = new HashMap<>();
         */
        methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(pathLoadType))),
                Constants.GROUP_PARAMETER_NAME,
                ClassName.get(HashMap.class));

        for (Map.Entry<String, String> entry : tempGroupMap.entrySet()){
            /**
             * groupMap.put("app", FRouter$$Path$$app.class);
             */
            methodBuilder.addStatement("$N.put($S, $T.class)",
                    Constants.GROUP_PARAMETER_NAME,
                    entry.getKey(),
                    ClassName.get(pkgNameForAPT, entry.getValue()));    // 指定包名下类文件
        }

        methodBuilder.addStatement("return $N", Constants.GROUP_PARAMETER_NAME);

        String finalClassName = Constants.GROUP_FILE_NAME + moduleName;

        JavaFile.builder(pkgNameForAPT,
                TypeSpec.classBuilder(finalClassName)
                        .addSuperinterface(ClassName.get(groupLoadType))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(methodBuilder.build())
                        .build())
                .build()            // 完成java类构建
                .writeTo(filer);    // 文件生成器生成类文件
    }

    /**
     * @Description: 赋值 tempPathMap
     *
     * @Author:  fxp
     * @Date:    2019-12-27   19:18
     * @param    bean
     * @return   void
     * @exception/throws
     */
    private void valueTempRouterMap(RouterBean bean){
        if (checkRouterPath(bean)){
            List<RouterBean> routerBeans = tempPathMap.get(bean.getGroup());
            if (EmptyUtils.isEmpty(routerBeans)){
                routerBeans = new ArrayList<>();
                routerBeans.add(bean);
                tempPathMap.put(bean.getGroup(), routerBeans);
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
     * @Description: 检查RouterBean path 合法性
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
    private boolean checkRouterPath(RouterBean routerBean){
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
