package com.eduardo.graph.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BPMNDefinition {
    private String id;
    private String xml;

    @JsonCreator
    public BPMNDefinition(@JsonProperty("id") String id, @JsonProperty("bpmn20Xml") String xml) {
        this.id = id;
        this.xml = xml;
    }

    public String getId() {
        return id;
    }

    public String getXml() {
        return xml;
    }


}
