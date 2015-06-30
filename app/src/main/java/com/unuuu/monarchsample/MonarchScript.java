package com.unuuu.monarchsample;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kashima on 15/07/01.
 */
public class MonarchScript {
    private List<String> up;

    public MonarchScript() {
        this.up = new ArrayList<>();
    }

    public List<String> getUp() {
        return up;
    }

    public void setUp(List<String> up) {
        this.up = up;
    }
}
