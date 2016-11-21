package org.wso2telco.analytics.report.generator.extension;

import java.util.List;

/**
 * Created by anushkas on 7/26/16.
 */
public class BeanWithList {

    private List<String> count;
    private String date;

    public BeanWithList(List<String> count, String date) {
        this.count = count;
        this.date = date;
    }

    public List<String> getCount() {
        return count;
    }

    public String getDate() {
        return date;
    }
}