package com.example.jklae.goal;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

public class CreateGoal2Activity extends AppCompatActivity {

    private final String TAG = "CreateGoal2Activity";
    int importance = 0;
    EditText contentTxtId;

    private void setup(){
        contentTxtId = (EditText) findViewById(R.id.content_txt);

        // 중요도 데이터 추출
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                Log.i(TAG, "레이팅 점수:"+rating);
                importance = (int) rating;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal2);
        Log.i(TAG, "onCreate()");

        setup();

        // 다음 버튼 온클릭
        Button nextBtn = (Button) findViewById(R.id.next_btn);
        nextBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){

            // 받은 인텐트에 데이터를 추출
            String goalTxt = getIntent().getStringExtra("goalTxt");     // 목표 제목
            Uri goalUri = getIntent().getParcelableExtra("goalUri");

            // 내용 데이터 추출
            String contentTxt =  contentTxtId.getText().toString();

            // 유효성 테스트
            if(contentTxt.equals("")){
                Toast.makeText(CreateGoal2Activity.this, "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            }
            else if(importance == 0){
                Toast.makeText(CreateGoal2Activity.this, "중요도를 선택하세요.", Toast.LENGTH_SHORT).show();
            }
            else{
                // 인텐트 적재
                Intent intent = new Intent(getApplicationContext(), CreateGoal3Activity.class);
                intent.putExtra("goalTxt", goalTxt);
                intent.putExtra("goalUri", goalUri);
                intent.putExtra("contentTxt", contentTxt);
                intent.putExtra("importance", importance);

                startActivity(intent);
            }
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
