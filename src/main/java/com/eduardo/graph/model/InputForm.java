package com.eduardo.graph.model;

public class InputForm {
    private String initialNode;
    private String finalNode;


    public InputForm(String initialNode, String finalNode) {
        this.initialNode = initialNode;
        this.finalNode = finalNode;
    }

    public String getInitialNode() {
        return initialNode;
    }

    public String getFinalNode() {
        return finalNode;
    }
}
