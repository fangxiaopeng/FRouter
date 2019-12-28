package com.fxp.frouter.annotation.model;

import javax.lang.model.element.Element;

/**
 * Title:       RouterBean
 * <p>
 * Package:     com.fxp.frouter.annotation.model
 *
 * @Author: fxp
 * <p>
 * Create at:   2019-12-27 16:56
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
public class RouterBean {

    public enum Type {
        ACTIVITY
    }

    private Type type;

    private Element element;

    private Class<?> clazz;

    private String path;

    private String group;

    public void setType(Type type) {
        this.type = type;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Type getType() {
        return type;
    }

    public Element getElement() {
        return element;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String getPath() {
        return path;
    }

    public String getGroup() {
        return group;
    }

    private RouterBean(Builder builder){
        this.element = builder.element;
        this.group = builder.group;
        this.path = builder.path;
    }

    private RouterBean(Type type, Class<?> clazz,  String group, String path){
        this.type = type;
        this.clazz = clazz;
        this.group = group;
        this.path = path;
    }

    public static RouterBean create(Type type, Class<?> clazz, String group, String path){
        return new RouterBean(type, clazz, group, path);
    }

    public final static class Builder {

        private Element element;

        private String path;

        private String group;

        public Builder setElement(Element element){
            this.element = element;
            return this;
        }

        public Builder setPath(String path){
            this.path = path;
            return this;
        }

        public Builder setGroup(String group){
            this.group = group;
            return this;
        }

        public RouterBean build(){
            if (path == null || path.trim().length() == 0){
                throw new IllegalArgumentException("path 不能为空");
            }

            return new RouterBean(this);
        }
    }

}
