package com.example.jklae.goal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import java.util.Calendar;

public class CreateGoal3Activity extends AppCompatActivity {

    private final String TAG = "CreateGoal3Activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal3);
        Log.i(TAG, "onCreate()");

        // 다음 버튼 온클릭
        Button nextBtn = (Button) findViewById(R.id.next_btn);
        nextBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
            Intent intent = new Intent(getApplicationContext(), CheckCreationGoalActivity.class);
            DatePicker goalDate = (DatePicker) findViewById(R.id.date_picker);

            // 목표 날짜
            int goalYear = goalDate.getYear();
            int goalMonth = goalDate.getMonth()+1;
            int goalDay = goalDate.getDayOfMonth();


            // 현재 날짜
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(cal.YEAR);
            int currentMonth = cal.get(cal.MONTH)+1;
            int currentDay = cal.get(cal.DATE);

            if(goalYear < currentYear){
                Toast.makeText(CreateGoal3Activity.this, "날짜를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
            else if(goalYear == currentYear && goalMonth < currentMonth){
                Toast.makeText(CreateGoal3Activity.this, "날짜를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
            else if(goalYear == currentYear && goalMonth == currentMonth && goalDay < currentDay){
                Toast.makeText(CreateGoal3Activity.this, "날짜를 확인해주세요.", Toast.LENGTH_SHORT).show();
            }
            else{

                intent.putExtra("goalTxt", getIntent().getStringExtra("goalTxt"));
                intent.putExtra("goalUri", getIntent().getParcelableExtra("goalUri"));
                intent.putExtra("contentTxt", getIntent().getStringExtra("contentTxt"));
                intent.putExtra("importance", getIntent().getIntExtra("importance", 0));
                intent.putExtra("goalYear", String.valueOf(goalYear));
                intent.putExtra("goalMonth", String.valueOf(goalMonth));
                intent.putExtra("goalDay", String.valueOf(goalDay));
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
