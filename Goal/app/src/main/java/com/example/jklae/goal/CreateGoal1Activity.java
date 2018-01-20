package com.example.jklae.goal;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class CreateGoal1Activity extends AppCompatActivity implements TextWatcher {

    private final String TAG = "CreateGoal1Activity";
    Bitmap goalBitmap;
    Uri mImageCaptureUri;       // 갤러리나 카메라 촬영 결과로 나온 uri
    Uri cropUri;                // 이미지 크롭 후 나온 uri
    private EditText goalTitle;
    private ImageView image;
    private ImageView imgRemove;

    private void setup(){
        goalTitle = (EditText) findViewById(R.id.goal_txt);
        goalTitle.addTextChangedListener(this);     // 10자 이상 입력 못하게 하기
        image = (ImageView)findViewById(R.id.imageView1);
        imgRemove = (ImageView) findViewById(R.id.img_remove);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {    // Bundle : 꾸러미, 보따리, 묶음
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal1);
        Log.i(TAG, "onCreate()");

        setup();

        // 다음 버튼 온클릭 이벤트
        Button nextBtn = (Button) findViewById(R.id.next_btn);
        nextBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                // 입력 데이터 추출
                EditText goalTxtId = (EditText) findViewById(R.id.goal_txt);
                String goalTxt = goalTxtId.getText().toString();

                if(goalTxt.equals("")){
                    Toast.makeText(CreateGoal1Activity.this, "목표를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                else{
                    // 인텐트를 생성하고, 추출한 데이터를 담고, 인텐트를 실행
                    Intent intent = new Intent(getApplicationContext(), CreateGoal2Activity.class);
                    intent.putExtra("goalTxt", goalTxt);
                    intent.putExtra("goalUri", cropUri);

                    startActivity(intent);
                }
            }
        });
    }

//    목표 이미지 선택
    public void selectImgOnclick(View v){
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
        AlertDialog dialog = builder.create();      // 알림창 객체 생성
        dialog.show();      // 알림창 띄우기
    }

    final int PICK_FROM_GALLERY = 1;
    final int PICK_FROM_CAMERA = 2;
    final int CROP_FROM_CAMERA = 3;
//    갤러리
    public void gallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
//        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);      // Action이 수행될 대상 데이터
//        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FROM_GALLERY);
        startActivityForResult(intent, PICK_FROM_GALLERY);
    }

//    카메라
    public void camera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";      // 카메라로 찍은 이미지를 저장할 파일명 생성
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));  // url로 지정한 파일명으로 Uri를 만듬
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);    // Uri로 기기에 저장 (결과 데이터를 OUTPUT이 저장하는 옵션)
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }


//  카메라, 갤러리 결과 받기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case PICK_FROM_GALLERY: {
                mImageCaptureUri = data.getData();
//                Log.i("NR", mImageCaptureUri.getPath().toString());
                // 이후의 처리가 카메라 부분과 같아 break 없이 진행
            }
            case PICK_FROM_CAMERA: {
                String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";      // 크롭한 이미지를 저장할 파일명 생성
                cropUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));   //url로 지정한 파일명으로 Uri를 만듬

                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");
                intent.putExtra("aspectX", 10); // crop 박스의 x축 비율
                intent.putExtra("aspectY", 9); // crop 박스의 y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("output", cropUri);     // Uri로 기기에 저장 (결과 데이터를 output이 저장하는 옵션), (이건 기기에 저장이 안되는것 같음...)
                startActivityForResult(intent, CROP_FROM_CAMERA);
                break;
            }
            case CROP_FROM_CAMERA: {
                ActivityCompat.requestPermissions(CreateGoal1Activity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                try {
                    goalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri);
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
                if(cropFile.exists()){  // 크롭하여 저장된 파일이 존재하면 삭제
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
        mImageCaptureUri = null;
        cropUri = null;
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

    //    백업 데이터 복원 (백업한 데이터는 onCreate 메소드의 매개변수로도 전달된다.)
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        Log.i(TAG, "onRestoreInstanceState");

        EditText goalTxt = (EditText) findViewById(R.id.goal_txt);

        if(savedInstanceState.getString("goalTitle") != null){
            goalTxt.setText(savedInstanceState.getString("goalTitle"));
        }
        if(savedInstanceState != null && savedInstanceState.getString("goalUri") != null){
            try {
                cropUri = Uri.parse(savedInstanceState.getString("goalUri"));
                goalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), cropUri);

                //배치해놓은 ImageView에 set
                image.setImageBitmap(goalBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
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

    //    데이터 백업 (onStop 직전에 호출)
    @Override
    protected void onSaveInstanceState(Bundle outState){
        Log.i(TAG, "onSaveInstanceState()");

        EditText goalTxt = (EditText) findViewById(R.id.goal_txt);

        if(!(goalTxt.getText().toString().equals(""))){
            String goalTitle = goalTxt.getText().toString();
            outState.putString("goalTitle", goalTitle);
        }
        if(cropUri != null){
            outState.putString("goalUri", cropUri.toString());
        }
        super.onSaveInstanceState(outState);
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


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(s.length() >= 10){
            Toast.makeText(this, "제목은 10자 내로 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }
}
