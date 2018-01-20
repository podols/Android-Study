package com.example.jklae.goal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CheckCreationGoalActivity extends AppCompatActivity {

    private final String TAG = "CheckCreationGoalActivity";

    SharedPreferences progressPref;
    SharedPreferences.Editor progressEditor;

    String goalTxt;
    String contentTxt;
    int importance;
    String goalYear;
    String goalMonth;
    String goalDay;
    Uri goalUri;
    Bitmap goalBitmap;

    private void setup(){
        progressPref = getSharedPreferences("progressList", MODE_PRIVATE);
        progressEditor = progressPref.edit();


        goalTxt = getIntent().getStringExtra("goalTxt");
        contentTxt = getIntent().getStringExtra("contentTxt");
        importance = getIntent().getIntExtra("importance", 0);
        goalYear = getIntent().getStringExtra("goalYear");
        goalMonth = getIntent().getStringExtra("goalMonth");
        goalDay = getIntent().getStringExtra("goalDay");
        goalUri = getIntent().getParcelableExtra("goalUri");
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

//        목표 제목 띄우기
        TextView goalTitleTxt = (TextView) findViewById(R.id.goal_title);
        goalTitleTxt.setText(goalTxt);
//        목표 내용 띄우기
        TextView goalContentTxt = (TextView) findViewById(R.id.goal_content);
        goalContentTxt.setText(contentTxt);
//        중요도 띄우기
        RatingBar importanceTxt = (RatingBar) findViewById(R.id.importance);
        importanceTxt.setRating((float) importance);
//        기간 띄우기
        TextView periodTxt = (TextView) findViewById(R.id.period);
        periodTxt.setText(goalYear+"년"+" "+goalMonth+"월"+" "+goalDay+"일");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_creation_goal);
        Log.i(TAG, "onCreate()");

        setup();

//        시작 버튼 온클릭
        Button nextBtn = (Button) findViewById(R.id.start_btn);
        nextBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                // 기간
                String period = goalYear+"."+goalMonth+"."+goalDay;

                // 디데이
                int dday= MainActivity.calculationDDay(Integer.parseInt(goalYear), Integer.parseInt(goalMonth)-1, Integer.parseInt(goalDay));

                // 등록 날짜
                SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ("yyyy.MM.dd HH:mm:ss", Locale.KOREA );
                Date currentTime = new Date();
                String insertTime = mSimpleDateFormat.format (currentTime);

                // 등록일 ~ 목표일 (max 값)
                int max = Util.calculationMax(insertTime, Integer.parseInt(goalYear), Integer.parseInt(goalMonth)-1, Integer.parseInt(goalDay));

                // 7개 저장
                MainListViewItem item = new MainListViewItem();
                item.setGoalTitle(goalTxt);
                item.setContent(contentTxt);
                item.setImportance(importance);
                item.setPeriod(period);
                item.setMax(max);       // 등록일~목표일 총 기간
                item.setInsertDate(insertTime);
                item.setDday(dday);
                item.setPercent(0);
                item.setGoalUri(goalUri);

                MainAdapter.progressList.add(item);

                int size = progressPref.getInt("size", 0);
                item.setKey("item"+size);

                Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriSerializer()).create();
                String json = gson.toJson(item);
                progressEditor.putString("item"+size, json);

                size++;
                progressEditor.putInt("size", size);
                progressEditor.apply();

                Util.reponseNotice();   // 공지 재할당

                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
        });
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

//    비트맵 -> 스트링
//    public static String bitMapToString(Bitmap bitmap){
//        if(bitmap == null){
//            return null;
//        }
//        else{
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//            byte[] bytes = baos.toByteArray();
//            String temp = Base64.encodeToString(bytes, Base64.DEFAULT);
//            return temp;
//        }
//    }

//    스트링 -> 비트맵
//    public static Bitmap stringToBitmap(String encodedString){
//        try{
//            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
//            return bitmap;
//        }
//        catch (Exception e){        // encodedString 값이 null 일 경우
//            e.getMessage();
//            Log.i("CheckCreationGoalActivity", "Exception 에러");
//            return null;
//        }
//    }