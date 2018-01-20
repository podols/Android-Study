package com.example.jklae.goal;

import android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by jklae on 2017-10-21.
 */

public class Util {


    //    등록일 ~ 목표일(or현재일) 까지 몇 일인지 구하기 (max값, percent값 구하기)
    public static int calculationMax(String insertTime, int year, int month, int day){

        TimeZone tz = TimeZone.getTimeZone ("Asia/Seoul");
        Calendar insertDate = Calendar.getInstance(tz);      // 등록일
        Calendar goalDate = Calendar.getInstance(tz);       // 목표 날짜

        String insert = insertTime.substring(0, insertTime.indexOf(" "));           // insertTime 형식 : "yyyy.MM.dd HH:mm:ss"
        String[] date = insert.split("[.]"); // 받은 등록일

        insertDate.set(Integer.parseInt(date[0]), Integer.parseInt(date[1])-1, Integer.parseInt(date[2]));
        goalDate.set(year, month, day);

        long cnt_goal = goalDate.getTimeInMillis() / 86400000;
        long cnt_insert = insertDate.getTimeInMillis() / 86400000;
        long sub = cnt_goal - cnt_insert;

        return (int) sub;
    }

//    공지 배열 할당
    public static void reponseNotice(){

        ArrayList<String> noticeArr = new ArrayList<>();


        for(int i=0; i<MainAdapter.progressList.size(); i++){

            MainListViewItem item = MainAdapter.progressList.get(i);

            // 디데이, 기간 퍼센트 업데이트 (앱 재실행시 마다)
            String period = item.getPeriod();
            String[] date = period.split("[.]");
            int year = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]);
            int day = Integer.parseInt(date[2]);

            int dday = MainActivity.calculationDDay(year, month-1, day);
            int max = item.getMax();    // 추가
            int percent = max+dday;

            // 추가 (noticeArr 에 공지 할당)
            if(percent >= max){
                Log.i("reponseNotice", "기간만료 찍음");
                String noticePhrase = item.getGoalTitle()+", 기간이 만료되었습니다.";
                noticeArr.add(noticePhrase);
            }
            else if(percent >= max*0.7){
                Log.i("reponseNotice", "기간 남음 찍음");
                int remainder = max - percent;      // 100 - 70 = 30
                String noticePhrase = item.getGoalTitle()+", "+remainder+"일 남았습니다.";
                noticeArr.add(noticePhrase);
            }
            else if(dday >= -3){
                Log.i("reponseNotice", "디데이 3일 찍음");
                int remainder = max - percent;
                String noticePhrase = item.getGoalTitle()+", "+remainder+"일 남았습니다.";
                noticeArr.add(noticePhrase);
            }
        }
        MainActivity.noticeAr = noticeArr;      // 추가
    }
}
