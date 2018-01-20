package com.example.jklae.goal;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jklae on 2017-09-29.
 */

public class MainAdapter extends BaseAdapter{
    SharedPreferences progressPref;
    SharedPreferences.Editor progressEditor;
    SharedPreferences successPref;
    SharedPreferences.Editor successEditor;
    SharedPreferences failPref;
    SharedPreferences.Editor failEditor;

    static String state  = "progress";
    public static ArrayList<MainListViewItem> progressList = new ArrayList<>();
    public static ArrayList<MainListViewItem> successList = new ArrayList<>();
    public static ArrayList<MainListViewItem> failList = new ArrayList<>();

    Context context;
    LayoutInflater inflater;

    boolean notifyDataSet = false;  // notifyDataSet이 될 때는 리스트뷰 스크롤 애니메이션을 미적용하기 위해
    int notifyDataSetCnt = 0;       // notifyDataSet이 될 때 최대 5개의 getView가 호출됨 (0이 되면 애니메이션 적용)

//    시작 화면에서 데이터 셋팅 생성자
    MainAdapter(Context context, boolean start){
        progressPref = context.getSharedPreferences("progressList", context.MODE_PRIVATE);
        progressEditor = progressPref.edit();
        successPref = context.getSharedPreferences("successList", context.MODE_PRIVATE);
        successEditor = successPref.edit();
        failPref = context.getSharedPreferences("failList", context.MODE_PRIVATE);
        failEditor = failPref.edit();

        Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriDeserializer()).create();

        int size = progressPref.getInt("size", 0);


        int cnt = 0;    // 각 목록 개수 체크용
        for(int i=0; i<size; i++){
            if(progressPref.contains("item"+i)){
                String json = progressPref.getString("item"+i, "");

                MainListViewItem item = gson.fromJson(json, MainListViewItem.class);

                // 디데이, 기간 퍼센트 업데이트 (앱 재실행시 마다)
                String period = item.getPeriod();
                String[] date = period.split("[.]");
                int year = Integer.parseInt(date[0]);
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[2]);

                int dday = MainActivity.calculationDDay(year, month-1, day);
                int max = item.getMax();    // 추가
                int percent = max+dday;

                item.setDday(dday);
                item.setPercent(percent);
                progressList.add(item);
                cnt++;
            }
        }
        Util.reponseNotice();        // 공지 할당


        cnt = 0;
        for(int i=0; i<size; i++){
            if(successPref.contains("item"+i)){
                String json = successPref.getString("item"+i, "");
                String successDate = successPref.getString("item"+i+"Time", "");
                MainListViewItem item = gson.fromJson(json, MainListViewItem.class);
                item.setSuccessDate(successDate);
                successList.add(item);
                cnt++;
            }
        }


        cnt = 0;
        for(int i=0; i<size; i++){
            if(failPref.contains("item"+i)){
                String json = failPref.getString("item"+i, "");
                String failDate = failPref.getString("item"+i+"Time", "");
                MainListViewItem item = gson.fromJson(json, MainListViewItem.class);
                item.setFailDate(failDate);
                failList.add(item);
                cnt++;
            }
        }
    }


//    리스트뷰 생성자
    MainAdapter(Context context){
        this.context = context;
        this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        notifyDataSet = true;
        notifyDataSetCnt = 5;
    }

    @Override
    public int getCount() {

        if(state.equals("progress")){
            return progressList.size();
        }
        else if(state.equals("success")){
            return successList.size();
        }
        else if (state.equals("fail")){
            return failList.size();
        }
        return 0;
    }

    @Override
    public MainListViewItem getItem(int position) {

        if(state.equals("progress")){
            return progressList.get(position);
        }
        else if(state.equals("success")){
            return successList.get(position);
        }
        else if (state.equals("fail")){
            return failList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // 처음 생성된 리스트뷰 일때, 해당 뷰를 inflate한다.
        if(convertView == null){

            convertView = inflater.inflate(R.layout.main_listview_item, parent, false);

            holder = new ViewHolder();

            // 각 뷰들을 홀더에 객체화
            holder.goalImg = (ImageView) convertView.findViewById(R.id.expansion_goal_img);
            holder.goalTitle = (TextView) convertView.findViewById(R.id.goal_title1);
            holder.importance = (RatingBar) convertView.findViewById(R.id.goal_importance1);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            holder.goalDday = (TextView) convertView.findViewById(R.id.dday);
            holder.insertDate = (TextView) convertView.findViewById(R.id.insert_date);
            holder.goalDate = (TextView) convertView.findViewById(R.id.goal_date);
            holder.changeDate = (TextView) convertView.findViewById(R.id.changeDate);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
            // 애니메이션 사용
            if(!notifyDataSet){
                ScaleAnimation itemAnim = new ScaleAnimation(1, 1, 0, 1);
                itemAnim.setDuration(200);
                convertView.startAnimation(itemAnim);
            }
            //
        }
        // 애니메이션을 notifyDataSet 될 때는 사용 안하기 위해서
        --notifyDataSetCnt;
        if(notifyDataSetCnt <= 0) {
            notifyDataSet = false;
            notifyDataSetCnt = 0;
        }

        MainListViewItem item = null;

        if(state.equals("progress")){
            item = progressList.get(position);
            holder.progressBar.setVisibility(holder.progressBar.VISIBLE);
            holder.goalDday.setVisibility(holder.goalDday.VISIBLE);
            holder.insertDate.setVisibility(holder.insertDate.VISIBLE);
            holder.goalDate.setVisibility(holder.goalDate.VISIBLE);
            holder.changeDate.setVisibility(holder.changeDate.GONE);
            if(item.getDday() >= 1){
                holder.goalDday.setText("D+"+item.getDday());
            }
            else if(item.getDday() < 0){
                holder.goalDday.setText("D"+item.getDday());
            }
            else{   // 0일 때
                holder.goalDday.setText("D-"+item.getDday());
            }
            String insertDate = item.getInsertDate().substring(0, item.getInsertDate().indexOf(" "));
            holder.insertDate.setText(insertDate);
            holder.goalDate.setText(item.getPeriod());
        }
        else if(state.equals("success")){
            item = successList.get(position);
            holder.progressBar.setVisibility(holder.progressBar.GONE);
            holder.goalDday.setVisibility(holder.goalDday.GONE);
            holder.insertDate.setVisibility(holder.insertDate.GONE);
            holder.goalDate.setVisibility(holder.goalDate.GONE);
            holder.changeDate.setVisibility(holder.changeDate.VISIBLE);
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(item.getSuccessDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            String dateFormat = new SimpleDateFormat("yyyy.MM.dd").format(date);
            holder.changeDate.setText(dateFormat+" 완료");
//            holder.period.setText(dateFormat);
        }
        else if (state.equals("fail")){
            item = failList.get(position);
            holder.progressBar.setVisibility(holder.progressBar.GONE);
            holder.goalDday.setVisibility(holder.goalDday.GONE);
            holder.insertDate.setVisibility(holder.insertDate.GONE);
            holder.goalDate.setVisibility(holder.goalDate.GONE);
            holder.changeDate.setVisibility(holder.changeDate.VISIBLE);

            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(item.getFailDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String dateFormat = new SimpleDateFormat("yyyy.MM.dd").format(date);
            holder.changeDate.setText(dateFormat+" 포기");
        }


        // 이미지
        Bitmap bitmap = null;
        if(item.getGoalUri() != null){
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), item.getGoalUri());
            } catch (IOException e) {
                e.printStackTrace();
            }
            holder.goalImg.setImageBitmap(bitmap);
        }
        else{
            holder.goalImg.setImageResource(R.drawable.create_goal_img);
        }
        //

        holder.goalTitle.setText(item.getGoalTitle());
        holder.importance.setRating((float) item.getImportance());

        // 프로그레스바 설정
        int percent = item.getPercent();
        int max = item.getMax();

        if(percent >= max){
           percent = max;
            if(percent == 0){
                percent = 1;
                max = 1;
            }
        }
        holder.progressBar.setProgress(percent);
        holder.progressBar.setMax(max);
        return convertView;
    }



    //    뷰 홀더 클래스
    private static class ViewHolder{
        ImageView goalImg;
        TextView goalTitle;
        RatingBar importance;
        ProgressBar progressBar;
        TextView goalDday;
        TextView insertDate;
        TextView goalDate;
        TextView changeDate;
    }



}


