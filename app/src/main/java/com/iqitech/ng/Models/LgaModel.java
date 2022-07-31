package com.iqitech.ng.Models;

public class LgaModel {
    private int id;
    private String name;
    private  String StateName;

    public LgaModel(int id, String name, String StateName) {
        this.id = id;
        this.name = name;
        this.StateName = StateName;
    }

    public LgaModel(int id, String name) {
        this.id = id;
        this.name = name;
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

    public String getStateName() {
        return StateName;
    }

    public void setStateName(String stateName) {
        StateName = stateName;
    }

    @Override
    public String toString() {
        return name;
    }
}
