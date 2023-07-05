package com.cc.fileManage.entity;

import androidx.annotation.NonNull;

/**
 * 书签实体类
 * @time 2022年6月24日14:22:17
 */
public class BookMark {

    public enum Type{
        Path,Web
    }

    private int id;
    private String name;        //书签名
    private String path;        //书签路径
    private String describe;    //书签描述
    private Type type;          //0 路径标签 1 链接标签

    public BookMark() {}

    public BookMark(Type type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @NonNull
    @Override
    public String toString() {
        return "BookMark{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", describe='" + describe + '\'' +
                ", type=" + type +
                '}';
    }
}
