package com.example.jklae.goal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.IOException;

public class GoalDetailActivity extends AppCompatActivity {

    private final String TAG = "GoalDetailActivity";
    String goalTxt;
    Uri goalUri;
    Bitmap goalBitmap;
    String contentTxt;
    int importance;
    String period;
    int dday;
    String insertDate;
    String successDate;
    String failDate;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_detail);
        Log.i(TAG, "onCreate()");

        ImageButton editBtn = (ImageButton) findViewById(R.id.edit_btn);            // 에디트 버튼
        RelativeLayout titleBar = (RelativeLayout) findViewById(R.id.title_bar);       // 타이틀바

        id = getIntent().getIntExtra("id", 0);      // 리스트뷰 아이템 id (position)
        MainListViewItem item = null;
        if(MainAdapter.state.equals("progress")){
            item = MainAdapter.progressList.get(id);
        }
        else if(MainAdapter.state.equals("success")){
            item = MainAdapter.successList.get(id);
        }
        else if(MainAdapter.state.equals("fail")){
            item = MainAdapter.failList.get(id);
        }


        goalUri = item.getGoalUri();
        goalTxt = item.getGoalTitle();
        contentTxt = item.getContent();
        importance = item.getImportance();
        period = item.getPeriod();
        dday = item.getDday();
        insertDate = item.getInsertDate().substring(0, item.getInsertDate().indexOf(" "));


        //        수정 버튼
        if(MainAdapter.state.equals("progress")){
            editBtn.setVisibility(editBtn.VISIBLE);
            titleBar.setBackgroundColor(Color.rgb(140,159,52));
        }
        else if(MainAdapter.state.equals("success")){
            successDate = item.getSuccessDate().substring(0, item.getInsertDate().indexOf(" "));
            editBtn.setVisibility(editBtn.GONE);
            titleBar.setBackgroundColor(Color.rgb(67,116,217));
        }
        else if(MainAdapter.state.equals("fail")){
            failDate = item.getFailDate().substring(0, item.getInsertDate().indexOf(" "));
            editBtn.setVisibility(editBtn.GONE);
            titleBar.setBackgroundColor(Color.rgb(237,125,49));
        }

        // 목표 제목 삽입 (액션바?, 타이틀바?)
        TextView goalTitle = (TextView) findViewById(R.id.goal_title);
        goalTitle.setText(goalTxt);

        // 이미지 삽입
        ImageView goalImg = (ImageView) findViewById(R.id.goal_img);

        //        이미지 띄우기
        if(goalUri != null){
            try {
                goalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), goalUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            goalImg.setImageBitmap(goalBitmap);
        }
        else{
            goalImg.setImageResource(R.drawable.create_goal_img);
        }

//        중요도 삽입
        RatingBar ratingBar = (RatingBar) findViewById(R.id.importance);
        ratingBar.setRating((float) importance);

//        기간 삽입
        TextView goalPeriod = (TextView) findViewById(R.id.period);
        goalPeriod.setText(insertDate+" ~ "+period);

//        완료, 포기일
        TextView changeDate = (TextView) findViewById(R.id.change_date);
        if(MainAdapter.state.equals("progress")){
            changeDate.setVisibility(changeDate.GONE);
        }
        else if(MainAdapter.state.equals("success")){
            changeDate.setVisibility(changeDate.VISIBLE);
            changeDate.setText(successDate + " 완료");

        }
        else if (MainAdapter.state.equals("fail")){
            changeDate.setVisibility(changeDate.VISIBLE);
            changeDate.setText(failDate + " 포기");
        }

//        내용 삽입
        TextView goalContent = (TextView) findViewById(R.id.goal_content);
        goalContent.setText(contentTxt);
    }

//    목표 수정 버튼 온클릭
    public void updateGoal(View v){
        MainActivity.updateFlag = true; // 수정버튼을 누르는 순간부터 플래그 활성화

        Intent intent = new Intent(this, UpdateGoalActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
        overridePendingTransition(0,0); // 화면 전환 애니메이션 제거
    }

    //    back 키 이벤트 (애니메이션 제거)
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            super.onBackPressed();
            overridePendingTransition(0,0);
        }
        return true;
    }

    //    생명주기
    @Override
    protected void onRestart(){
        super.onRestart();
        Log.i(TAG, "onRestart() called");
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.i(TAG, "onStart() called");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG, "onResume() called");
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG, "onPause() called");
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i(TAG, "onStop() called------------");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
    }
}
