package com.example.jklae.goal;


import android.net.Uri;

import java.io.Serializable;

/**
 * Created by jklae on 2017-10-01.
 */

public class MainListViewItem implements Serializable {
    private String goalTitle;
    private int importance;
    private String period;      // 목표 기간 (목표 지정일)   형식 : 2017.10.24
    private String content;
    private String insertDate;          // 형식 : 2017.10.22 18:37:33
    private String successDate;
    private String failDate;
    private String key;     // 쉐어드 저장 키값    형식 : item0, item1 ...
    private int dday;       // 형식 : -2, -3 ...
    private int max;    // progressbar max값 (등록일~목표일 총 기간)
    private int percent;    // progressbar 퍼센트 값 (하루가 지날때마다 +1씩 증가)
    private Uri goalUri;


    public Uri getGoalUri() {
        return goalUri;
    }

    public void setGoalUri(Uri goalUri) {
        this.goalUri = goalUri;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFailDate() {
        return failDate;
    }

    public void setFailDate(String failDate) {
        this.failDate = failDate;
    }

    public String getSuccessDate() {
        return successDate;
    }

    public void setSuccessDate(String successDate) {
        this.successDate = successDate;
    }

    public int getDday() {
        return dday;
    }

    public void setDday(int dday) {
        this.dday = dday;
    }

    public void setGoalTitle(String goalTitle){
        this.goalTitle = goalTitle;
    }
    public void setImportance(int importance){
        this.importance = importance;
    }
    public void setPeriod(String period){
        this.period = period;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setInsertDate(String insertDate) {this.insertDate = insertDate;}

    public String getGoalTitle(){
        return this.goalTitle;
    }
    public int getImportance(){
        return this.importance;
    }
    public String getPeriod(){
        return this.period;
    }
    public String getContent() {
        return content;
    }
    public String getInsertDate() {return insertDate; }

}
