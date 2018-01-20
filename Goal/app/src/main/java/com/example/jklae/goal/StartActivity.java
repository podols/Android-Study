package com.example.jklae.goal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class StartActivity extends AppCompatActivity {

    private final String TAG = "StartActivity";
    static boolean memory = true;       // 어댑터 생성자로 쉐어드값 할당시키는 스레드 컨트롤
    Handler handler;
    SharedPreferences progressPref;
    SharedPreferences.Editor progressEditor;
    ImageView introTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Log.i(TAG, "onCreate()");

        setup();
        dataSetting();  //    쉐어드에서 데이터 셋팅 스레드
        changeActivity();   //    메인 화면으로 전환 스레드
        anim();     // 애니메이션
    }

    private void setup(){
        introTxt = (ImageView) findViewById(R.id.intro_txt);

        progressPref = getSharedPreferences("progressList", MODE_PRIVATE);
        progressEditor = progressPref.edit();
        handler = new Handler();
    }

//    애니메이션
    private void anim(){
        Animation introAnim = AnimationUtils.loadAnimation(this, R.anim.intro_anim);
        introTxt.startAnimation(introAnim);
    }

//    쉐어드에서 데이터 셋팅 스레드
    private void dataSetting(){
        if(memory){
            Thread th1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    new MainAdapter(getApplicationContext(), true);
                }
            });
            th1.start();
        }
        memory = false;
    }

//    메인 화면으로 전환 스레드
    private void changeActivity(){
        // 메인 스레드에게 run에 있는 코드를 2초뒤에 보내고, 메인 스레드는 2초뒤에 run에 있는 코드를 실행한다.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
        Log.i("StartActivity", "changeActivity, 화면전환 핸들러 끝");

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
