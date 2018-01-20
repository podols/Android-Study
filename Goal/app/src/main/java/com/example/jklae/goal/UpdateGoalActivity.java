package com.example.jklae.goal;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UpdateGoalActivity extends AppCompatActivity implements TextWatcher {

    SharedPreferences progressPref;
    SharedPreferences.Editor progressEditor;

    private final String TAG = "UpdateGoalActivity";

    String goalTitle;
    Uri goalUri;
    String content;
    int importance;
    String period;
    int dday;
    Bitmap goalBitmap;
    String insertDate;
    String successDate;
    String failDate;
    int id; // position 값
    String key; // 쉐어드 저장 키값
    int max;
    int percent;
    Uri mImageCaptureUri;

    int updateYear=0;
    int updateMonth=0;
    int updateDay=0;

    ImageView image;
    ImageView imgRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_goal);
        Log.i(TAG, "onCreate");

        // 뷰 객체 생성
        image = (ImageView) findViewById(R.id.goal_img);
        imgRemove = (ImageView) findViewById(R.id.img_remove);
        EditText goalTxt = (EditText) findViewById(R.id.goal_title);
        goalTxt.addTextChangedListener(this);     // 10자 이상 입력 못하게 하기
        EditText contentTxt = (EditText) findViewById(R.id.goal_content);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.importance);
        TextView periodTxt = (TextView) findViewById(R.id.period);

        progressPref = getSharedPreferences("progressList", MODE_PRIVATE);
        progressEditor = progressPref.edit();

        id = getIntent().getIntExtra("id", 0);      // 리스트뷰 아이템 id (position)

        MainListViewItem item = MainAdapter.progressList.get(id);

        goalUri = item.getGoalUri();
        goalTitle = item.getGoalTitle();
        content = item.getContent();
        importance = item.getImportance();
        period = item.getPeriod();
        dday = item.getDday();
        insertDate = item.getInsertDate();
        successDate = item.getSuccessDate();
        failDate = item.getFailDate();
        key = item.getKey();        // 쉐어드 저장 키값

        max = item.getMax();
        percent = item.getPercent();

        // 뷰에 삽입
        goalTxt.setText(goalTitle);

        // 이미지 띄우기
        if(goalUri != null){
            imgRemove.setVisibility(imgRemove.VISIBLE);
            try {
                goalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), goalUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            image.setImageBitmap(goalBitmap);
        }
        else{
            image.setImageResource(R.drawable.create_goal_img);
        }
        contentTxt.setText(content);
        ratingBar.setRating((float) importance);
        periodTxt.setText(period);
    }
    // onCreate() 끝


    //    이미지 수정 팝업 온클릭
    public void updateImgOnclick(View v){

        final String[] items = {"갤러리", "카메라", "구글 이미지 검색"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("미디어 선택").setItems(items, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){         // 갤러리 선택
                    gallery();
                }
                else if(which == 1){    // 카메라 선택
                    camera();
                }
                else if(which == 2){    // 구글 이미지 검색 선택
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://images.google.com/?gws_rd=ssl"));
                    // intent.setPackage("com.android.chrome");   // 브라우저가 여러개 인 경우 콕 찍어서 크롬을 지정할 경우
                    startActivity(intent);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    final int PICK_FROM_GALLERY = 1;
    final int PICK_FROM_CAMERA = 2;
    final int CROP_FROM_CAMERA = 3;
//    갤러리
    public void gallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FROM_GALLERY);
    }

//    카메라
    public void camera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";      // 카메라로 찍은 이미지를 저장할 파일명 생성
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));  // url로 지정한 파일명으로 Uri를 만듬
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);    // Uri로 기기에 저장 (결과 데이터를 OUTPUT이 저장하는 옵션)
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

//    카메라, 갤러리 결과 받기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case PICK_FROM_GALLERY: {
                mImageCaptureUri = data.getData();
                // 이후의 처리가 카메라 부분과 같아 break 없이 진행
            }
            case PICK_FROM_CAMERA: {
                String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";      // 크롭한 이미지를 저장할 파일명 생성
                goalUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));   //url로 지정한 파일명으로 Uri를 만듬

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");
                intent.putExtra("aspectX", 10); // crop 박스의 x축 비율
                intent.putExtra("aspectY", 9); // crop 박스의 y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("output", goalUri);     // Uri로 기기에 저장 (결과 데이터를 output이 저장하는 옵션), (이건 기기에 저장이 안되는것 같음...)
                startActivityForResult(intent, CROP_FROM_CAMERA);
                break;
            }

            case CROP_FROM_CAMERA: {

                ActivityCompat.requestPermissions(UpdateGoalActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                try {
                    goalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), goalUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.setImageBitmap(goalBitmap);
                //

                // 카메라로 찍은 임시파일과 크롭한 임시파일을 삭제 (Log찍어보면 경로가 일반 갤러리 파일 경로가 아닌듯)
                File cameraFile = new File(mImageCaptureUri.getPath());     // 카메라로 찍어 저장된 이미지 파일
                File cropFile = new File(mImageCaptureUri.getPath());       // 크롭하여 저장된 이미지 파일
                if (cameraFile.exists()) {  // 카메라로 찍어 저장된 파일이 존재하면 삭제
                    Log.i("CROP_FROM_CAMERA", "cameraFile f.exists");
                    cameraFile.delete();
                }
                if (cropFile.exists()) {  // 크롭하여 저장된 파일이 존재하면 삭제
                    Log.i("CROP_FROM_CAMERA", "cropFile f.exists");
                    cropFile.delete();
                }
                imgRemove.setVisibility(imgRemove.VISIBLE);
                break;
            }

        }

    }

    //    이미지 제거
    public void imgRemoveOnclick(View v){
        image.setImageResource(R.drawable.create_goal_img);
        imgRemove.setVisibility(imgRemove.GONE);
        goalUri = null;
        goalBitmap = null;
    }



//    기간 수정 온클릭
    public void updatePeriodOnclick(View v){
        String[] date = period.split("[.]");        // .은 점으로 인식안하고 특별한 문자로 인식하기 때문에 구분해줘야한다.

        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]);
        int day = Integer.parseInt(date[2]);

        Context context = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog);

        DatePickerDialog dialog = new DatePickerDialog(context, listener, year, month-1, day);
        dialog.show();
    }

//    데이트피커에서 선택한 목표 날짜 값
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            monthOfYear = monthOfYear+1;

            // 현재 날짜
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(cal.YEAR);
            int currentMonth = cal.get(cal.MONTH)+1;
            int currentDay = cal.get(cal.DATE);

            // 날짜 유효성 테스트
            if(year < currentYear){
                Toast.makeText(UpdateGoalActivity.this, "날짜를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
            else if(year == currentYear && monthOfYear < currentMonth){
                Toast.makeText(UpdateGoalActivity.this, "날짜를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
            else if(year == currentYear && monthOfYear == currentMonth && dayOfMonth < currentDay){
                Toast.makeText(UpdateGoalActivity.this, "날짜를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                updateYear = year;
                updateMonth = monthOfYear;
                updateDay = dayOfMonth;

                TextView periodTxt = (TextView) findViewById(R.id.period);
                period = year+"."+monthOfYear+"."+dayOfMonth;
                periodTxt.setText(period);
                return;
            }

            updatePeriodOnclick(view);
        }
    };


//    수정 완료 온클릭
    public void completionOnclick(View v){
        Intent intent = new Intent(getApplicationContext(), GoalDetailActivity.class);

        EditText goalTxt = (EditText) findViewById(R.id.goal_title);
        EditText goalContent = (EditText) findViewById(R.id.goal_content);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.importance);

        goalTitle = String.valueOf(goalTxt.getText());
        content = String.valueOf(goalContent.getText());
        importance = (int) ratingBar.getRating();

        // 디데이 (현재일 ~ 수정된 목표일) 수정, max값, percent값 구하기
        if(updateYear != 0 && updateMonth != 0 && updateDay != 0){      // 날짜를 수정했을때
            Log.i(TAG, "updateMonth 값 : " + updateMonth);
            dday = MainActivity.calculationDDay(updateYear, updateMonth-1, updateDay);
            max = Util.calculationMax(insertDate, updateYear, updateMonth-1, updateDay);

            Calendar cal = Calendar.getInstance();
            int year = cal.get(cal.YEAR);
            int month = cal.get(cal.MONTH)+1;
            int day = cal.get(cal.DATE);

            percent = Util.calculationMax(insertDate, year, month-1, day);

        }


        // 수정한 내용을 리스트에서 update
        MainListViewItem item = new MainListViewItem();
        item.setGoalUri(goalUri);
        item.setGoalTitle(goalTitle);
        item.setContent(content);
        item.setImportance(importance);
        item.setPeriod(period);
        item.setDday(dday);
        item.setInsertDate(insertDate);
        item.setSuccessDate(successDate);
        item.setFailDate(failDate);
        item.setKey(key);
        item.setMax(max);       // 등록일 ~ 수정된 목표일까지 일 수
        item.setPercent(percent);   // 등록일 ~ 오늘날짜까지 지난 일 수
        MainAdapter.progressList.set(id, item);

        Util.reponseNotice();       // 공지 재할당

        Gson gson = new GsonBuilder().registerTypeAdapter(Uri.class, new UriSerializer()).create();
        String json = gson.toJson(item);
        progressEditor.putString(key, json);
        progressEditor.apply();

        intent.putExtra("id", id);

        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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


//    텍스트 워처 (목표 제목 텍스트뷰에 10글자 이상 입력 시 toast 띄우기)
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(s.length() >= 10){
            Toast.makeText(this, "제목은 10자 내로 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
