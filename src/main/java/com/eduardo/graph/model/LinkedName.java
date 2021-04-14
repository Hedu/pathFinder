package com.eduardo.graph.model;

public class LinkedName {

    private String name;
    private LinkedName linkedName;

    public LinkedName(String name, LinkedName linkedName) {
        this.name = name;
        this.linkedName = linkedName;
    }

    public String getName() {
        return name;
    }

    public LinkedName getLinkedName() {
        return linkedName;
    }
}
