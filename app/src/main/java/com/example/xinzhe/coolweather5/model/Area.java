package com.example.xinzhe.coolweather5.model;

/**
 * Created by Xinzhe on 2015/10/15.
 */
public class Area {
    protected int  id;
    protected String code="";
    protected String name;
    protected String parentCode;

    protected  enum level{province,city,county};

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }
}
