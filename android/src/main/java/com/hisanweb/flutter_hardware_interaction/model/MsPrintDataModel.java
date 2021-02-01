package com.hisanweb.flutter_hardware_interaction.model;

import java.util.List;

public class MsPrintDataModel {
    
    private String type;
    
    private String data;
    
    private int lineFeed;

    private List<Integer> sheet;

    public List<Integer> getSheet() {
        return sheet;
    }

    public void setSheet(List<Integer> sheet) {
        this.sheet = sheet;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getLineFeed() {
        return lineFeed;
    }

    public void setLineFeed(int lineFeed) {
        this.lineFeed = lineFeed;
    }
}
