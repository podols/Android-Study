package com.example.jklae.goal;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    SharedPreferences progressPref;
    SharedPreferences.Editor progressEditor;
    SharedPreferences successPref;
    SharedPreferences.Editor successEditor;
    SharedPreferences failPref;
    SharedPreferences.Editor failEditor;

    static boolean updateFlag = false;      // 상세보기화면에서 수정후에 메인으로 돌아오면 그 때만 리스트뷰를 notifyDataSet 하기 위한 변수
    static boolean memory = true;       // 메모리에 프로세스가 살아있을 때만 디데이 체크 스레드 활동
    static ArrayList<String> noticeAr;  // 앱 실행시 어댑터 생성자에 noticeAr에 데이터 할당

    private ListView listView;
    ArrayList<MainListViewItem> itemList = null;        // progressList, successList, failList 세 개 모두가 될 수 있다.
    MainAdapter adapter = null;

    private boolean isSecond = false;   // 뒤로 두번 눌렀는지 확인
    private Timer timer;
    Calendar cal;
    Handler handler;
    Menu menu = null;   // 팝업 메뉴

    int progressSort = 1;       // 정렬 할 값 (1=디데이, 2=중요도, 3=등록, 4=완료일, 5=포기일)
    int successSort = 4;
    int failSort = 5;

    boolean useNotice = true;

    TextView sentence;      // 리스트뷰에 데이터가 없을 때 나타내는 문구 (목표를등록해주세요!)
    TextView notice;
    RelativeLayout title_bar;
    ImageButton insertGoal;
    ImageView foldNotice;
    ImageView unfoldNotice;

    void setup(){
        progressPref = getSharedPreferences("progressList", MODE_PRIVATE);
        progressEditor = progressPref.edit();

        successPref = getSharedPreferences("successList", MODE_PRIVATE);
        successEditor = successPref.edit();

        failPref = getSharedPreferences("failList", MODE_PRIVATE);
        failEditor = failPref.edit();


        sentence = (TextView) findViewById(R.id.sentence);
        listView = (ListView) findViewById(R.id.listview);
        notice = (TextView) findViewById(R.id.notice);
        title_bar = (RelativeLayout) findViewById(R.id.title_bar);
        insertGoal = (ImageButton) findViewById(R.id.insert_goal);
        foldNotice = (ImageView) findViewById(R.id.fold_notice);
        unfoldNotice = (ImageView) findViewById(R.id.unfold_notice);

        useNotice = progressPref.getBoolean("notice", true);        // 공지를 보여주는지 안보여주는지 저장한 값

        handler = new Handler();

        checkEmpty();

        adapter = new MainAdapter(this);
        listView.setAdapter(adapter);       // getCount 호출

        if(progressPref.contains("sort")){
            progressSort = progressPref.getInt("sort", 0);
            Log.i("진행 목록 정렬", "솔트값:"+progressSort);
        }
        sort(progressSort);
    }

//    디데이 체크 스레드
    private void updatePeriod(){
        if(memory){
            Thread th2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        cal = Calendar.getInstance();
                        int hour = cal.get(cal.HOUR_OF_DAY);
                        int min = cal.get(cal.MINUTE);
                        int sec = cal.get(cal.SECOND);

                        if(hour == 0 && min == 0){
                            Log.i(TAG, "디데이 변경 시작"+hour+":"+min+":"+sec);

                            ArrayList<String> noticeArr = new ArrayList<>();    // 추가

                            for(int i=0; i<MainAdapter.progressList.size(); i++){
                                MainListViewItem item = MainAdapter.progressList.get(i);
                                int percent = item.getPercent();
                                int max = item.getMax();            // 추가

                                percent++;

                                // 추가 (하루가 지나면서 기간을 체크하고 공지 배열에 추가)
                                if(percent >= max){
                                    String noticePhrase = item.getGoalTitle()+", 기간이 만료되었습니다.";
                                    noticeArr.add(noticePhrase);
                                }
                                else if(percent >= max*0.7){
                                    int remainder = max - percent;      // 100 - 70 = 30
                                    String noticePhrase = item.getGoalTitle()+", "+remainder+"일 남았습니다.";
                                    noticeArr.add(noticePhrase);
                                }
                                //


                                // 하루가 지나면서 바뀐 디데이와 퍼센트를 업데이트
                                String period = item.getPeriod();
                                String[] date = period.split("[.]");
                                int year = Integer.parseInt(date[0]);
                                int month = Integer.parseInt(date[1]);
                                int day = Integer.parseInt(date[2]);
                                int dday = MainActivity.calculationDDay(year, month-1, day);

                                MainAdapter.progressList.get(i).setDday(dday);
                                MainAdapter.progressList.get(i).setPercent(percent);
                            }
                            noticeAr = noticeArr;   // 추가

                            // 업데이트된 퍼센트와 디데이를 다시 그려주기
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();

                                }
                            });

                            Log.i(TAG, "디데이 변경 끝"+hour+":"+min+":"+sec);

                            try {
                                Thread.sleep(61000);    // 61초간 재움 (중복되지 않기위해)
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            th2.start();
        }
    }

    Thread th3;
    boolean exceptionFlag = false;
//    공지 스레드 (onResume때 작동)
    private void notice(){
        if(useNotice){
            th3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        if(noticeAr.size() >= 1){
                            for(int i=0; i<noticeAr.size(); i++){
                                final int cnt = i;

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        notice.setVisibility(notice.VISIBLE);
                                        try{
                                            notice.setText(noticeAr.get(cnt));
                                        }
                                        catch (IndexOutOfBoundsException e){
                                            e.printStackTrace();
                                            exceptionFlag = true;  // exception에 걸리면 sleep안하고 continue함
                                        }
                                    }
                                });
                                if(exceptionFlag){
                                    exceptionFlag = false;
                                    continue;
                                }
                                else{
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            }
                        }
                        else{
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                }
            });
            th3.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate()");

        setup();
        goalInsertAnim(1);
        updatePeriod();

        memory = false;


//        리스트 온클릭 (상세보기)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "온클릭");
                Intent intent = new Intent(getApplicationContext(), GoalDetailActivity.class);
                intent.putExtra("id", position);
                startActivity(intent);
                overridePendingTransition(0,0); // 화면 이동 애니메이션 제거
            }
        });

//        리스트 롱클릭 (다이얼로그)
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id){
                String[] items = null;
                if(MainAdapter.state.equals("progress")){
                    itemList = MainAdapter.progressList;
                    items = new String[4];
                    items[0] = "삭제";
                    items[1] = "공유";
                    items[2] = "완료";
                    items[3] = "포기";
                }
                else if(MainAdapter.state.equals("success")){
                    itemList = MainAdapter.successList;
                    items = new String[3];
                    items[0] = "삭제";
                    items[1] = "공유";
                    items[2] = "되돌리기";
                }
                else if(MainAdapter.state.equals("fail")){
                    itemList = MainAdapter.failList;
                    items = new String[3];
                    items[0] = "삭제";
                    items[1] = "공유";
                    items[2] = "되돌리기";
                }

                // 다이얼로그 생성 (삭제, 공유, 완료, 포기, 되돌리기)
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(itemList.get(position).getGoalTitle()).setItems(items, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0){         // 삭제

                            // 정말 삭제하시겠습니까? 다이얼로그
                            final AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                            builder1.setTitle(itemList.get(position).getGoalTitle());
                            builder1.setMessage("정말 삭제하시겠습니까?");

                            builder1.setNegativeButton("예", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(MainAdapter.state.equals("progress")){
                                        String key = MainAdapter.progressList.get(position).getKey();       // 추가
                                        MainAdapter.progressList.remove(position);
                                        progressEditor.remove(key);     // 추가
                                        progressEditor.apply();
                                        Util.reponseNotice();        // 공지 재할당
                                    }
                                    else if(MainAdapter.state.equals("success")){
                                        String key = MainAdapter.successList.get(position).getKey();       // 추가
                                        MainAdapter.successList.remove(position);
                                        successEditor.remove(key);      // 추가
                                        successEditor.apply();
                                    }
                                    else if (MainAdapter.state.equals("fail")){
                                        String key = MainAdapter.failList.get(position).getKey();   // 추가
                                        MainAdapter.failList.remove(position);
                                        failEditor.remove(key);     // 추가
                                        failEditor.apply();
                                    }
                                    checkEmpty();
                                    viewAnim(view);     // 애니메이션 적용 + notifyDataSet
                                    Toast.makeText(MainActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });

                            builder1.setPositiveButton("아니오", new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            builder1.show();
                            //
                        }   // which == 0 끝



                        else if(which == 1){    // 공유
//                                shareTxt(position);
                            shareImg(view);
                        }
                        else if(which == 2){    // 완료, 되돌리기
                            if(MainAdapter.state.equals("progress")){
                                // 완료 날짜
                                SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ("yyyy.MM.dd HH:mm:ss", Locale.KOREA );
                                Date currentTime = new Date();
                                String insertTime = mSimpleDateFormat.format (currentTime);

                                MainAdapter.progressList.get(position).setSuccessDate(insertTime);

                                MainAdapter.successList.add(MainAdapter.progressList.get(position));
                                //
                                // 완료목록에 추가
                                String key = MainAdapter.progressList.get(position).getKey();       // 추가

                                successEditor.putString(key, progressPref.getString(key,""));        // 여기
                                successEditor.putString(key+"Time", insertTime);     // 완료일 추가
                                successEditor.apply();

                                progressEditor.remove(key);     // 추가
                                progressEditor.apply();

                                MainAdapter.progressList.remove(position);
                                //
                            }
                            else if(MainAdapter.state.equals("success")){   // 성공일 때 되돌리기
                                MainAdapter.successList.get(position).setSuccessDate("");

                                MainAdapter.progressList.add(MainAdapter.successList.get(position));

                                String key = MainAdapter.successList.get(position).getKey();    // 추가
                                progressEditor.putString(key, successPref.getString(key,""));
                                progressEditor.apply();
                                successEditor.remove(key);      // 추가
                                successEditor.apply();

                                MainAdapter.successList.remove(position);
                            }
                            else if (MainAdapter.state.equals("fail")){     // 포기일 때 되돌리기
                                MainAdapter.failList.get(position).setFailDate("");
                                MainAdapter.progressList.add(MainAdapter.failList.get(position));
                                // 진행목록에 추가
                                String key = MainAdapter.failList.get(position).getKey();   // 추가
                                progressEditor.putString(key, failPref.getString(key,""));
                                progressEditor.apply();

                                failEditor.remove(key); //추가
                                failEditor.apply();

                                MainAdapter.failList.remove(position);
                                //
                            }

                            Util.reponseNotice();        // 공지 재할당
                            checkEmpty();
                            viewAnim(view);     // 애니메이션 적용 + notifyDataSet
                        }// which==2 끝


                        else if(which == 3){    // 포기
                            // 포기 날짜
                            SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat ("yyyy.MM.dd HH:mm:ss", Locale.KOREA );
                            Date currentTime = new Date();
                            String insertTime = mSimpleDateFormat.format (currentTime);

                            MainAdapter.progressList.get(position).setFailDate(insertTime);
                            MainAdapter.failList.add(MainAdapter.progressList.get(position));

                            String key = MainAdapter.progressList.get(position).getKey();       // 추가!!
                            failEditor.putString(key, progressPref.getString(key,""));       // 여기
                            failEditor.putString(key+"Time", insertTime);     // 포기일 추가
                            failEditor.apply();

                            progressEditor.remove(key);     // 추가!!
                            progressEditor.apply();

                            MainAdapter.progressList.remove(position);
                            Util.reponseNotice();        // 공지 재할당

                            checkEmpty();
                            viewAnim(view);     // 애니메이션 적용 + notifyDataSet
                        }
                        listView.clearChoices();            // 리스트뷰의 모든    선택(Checked) 상태를 초기화
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;     // true로 하면 롱클릭이랑 온클릭이랑 겹치지 않는다.
            }
        });

//        목표 추가 온클릭
        ImageButton startBtn = (ImageButton) findViewById(R.id.insert_goal);
        startBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), CreateGoal1Activity.class);
                startActivity(intent);
                overridePendingTransition(0,0); // 화면 이동 애니메이션 제거
            }
        });


    }
//    onCreate() 종료

//    목표 삭제, 완료, 포기, 되돌리기 애니메이션
    private void viewAnim(View view){

        ScaleAnimation itemAnim = new ScaleAnimation(
                1, 1, 1, 0,
                Animation.RELATIVE_TO_SELF, 0f,     // x축 pivot = 0%
                Animation.RELATIVE_TO_SELF, 0.5f);  // y축 pivot = 50%
        itemAnim.setDuration(150);
        view.startAnimation(itemAnim);

        // 애니메이션 이벤트 리스너
        itemAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                adapter.notifyDataSetChanged();     // 리스트뷰 갱신
            }
        });
    }

//    리스트뷰 이미지 공유
    private LinearLayout container;
    public void shareImg(View view){
        container = (LinearLayout) view.findViewById(R.id.list_container);

// true : drawing cache에 캡처한 이미지 저장 (이유: 뷰 수정 시에 새로운 뷰가 공유되어야 해서)
        container.setDrawingCacheEnabled(true);
// 뷰 이미지를 drawwing cache에 저장한다. (이 한줄을 안적어도 되지만, 안적으면 bitmap을 계속 재생성하여 실행가능한 메모리에러가 난다.)
        container.buildDrawingCache();
        Bitmap captureView = container.getDrawingCache();

        String adress = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/Screenshots" + "/test.jpeg";

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(adress);
            captureView.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Uri uri = Uri.fromFile(new File(adress));

        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent,"공유"));

        container.setDrawingCacheEnabled(false);        // false : drawing cache에 캡처한 이미지 삭제
    }


    //    back 키 이벤트 (뒤로가기 2번 종료)
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(isSecond == false) { // 첫번째인 경우
                Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT).show();
                isSecond = true;
                //Back키가 2초내에 두번 눌렸는지 감지
                TimerTask second = new TimerTask() {
                    @Override
                    public void run() {
                        timer.cancel();
                        timer = null;
                        isSecond = false;
                    }
                };
                if(timer != null){
                    timer.cancel();
                    timer = null;
                }
                timer = new Timer();
                timer.schedule(second, 2000);
            }else{
                super.onBackPressed();
            }
        }
        return true;
    }

    //    디데이 구하기 (현재일 ~ 목표일)
    public static int calculationDDay(int goalYear, int goalMonth, int goalDay){
        TimeZone tz = TimeZone.getTimeZone ("Asia/Seoul");
        Calendar today = Calendar.getInstance(tz);      // 현재 날짜 (지금 등록한 거라면 현재날짜 = 등록날짜)
        Calendar dday = Calendar.getInstance(tz);       // 목표 날짜

        dday.set(goalYear, goalMonth, goalDay);

        long cnt_dday = dday.getTimeInMillis() / 86400000;
        long cnt_today = today.getTimeInMillis() / 86400000;
        long sub = cnt_today - cnt_dday;

        return (int) sub;
    }


//    등록 버튼 애니메이션
    private void goalInsertAnim(int choice){
        AnimationSet set = new AnimationSet(true);
        set.setInterpolator(new AccelerateInterpolator());

        TranslateAnimation translateAnim;
        AlphaAnimation alphaAnim;

        if(choice == 1){
            translateAnim = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 1.5f,
                    Animation.RELATIVE_TO_SELF, 0);
            alphaAnim = new AlphaAnimation(0.0f, 1.0f);
        }
        else{
            translateAnim = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 1.5f);
            alphaAnim = new AlphaAnimation(1.0f, 0.0f);
        }
        translateAnim.setDuration(300);
        alphaAnim.setDuration(300);

        set.addAnimation(translateAnim);
        set.addAnimation(alphaAnim);

        insertGoal.startAnimation(set);

    }

//   옵션 팝업 메뉴
    public void optionMenuOnclick(final View v){
        PopupMenu popupMenu = new PopupMenu(this, v);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                TextView titleTxt = (TextView) findViewById(R.id.title_txt);

                if(item.getItemId() == 1){  // 진행목록(전체목록)
                    if(!MainAdapter.state.equals("progress")){
                        goalInsertAnim(1);
                    }
                    MainAdapter.state = "progress";
                    titleTxt.setText("Bucket List");
                    title_bar.setBackgroundColor(Color.rgb(140,159,52));
                    insertGoal.setVisibility(insertGoal.VISIBLE);

                    if(progressPref.contains("sort")){
                        progressSort = progressPref.getInt("sort", 0);
                    }
                    sort(progressSort);
                }
                else if(item.getItemId() == 2){  // 완료목록
                    if(MainAdapter.state.equals("progress")){
                        goalInsertAnim(2);
                    }

                    MainAdapter.state = "success";
                    titleTxt.setText("Success");
                    title_bar.setBackgroundColor(Color.rgb(67,116,217));

                    insertGoal.setVisibility(insertGoal.GONE);

                    if(successPref.contains("sort")){
                        successSort = successPref.getInt("sort", 0);
                    }
                    sort(successSort);
                }
                else if(item.getItemId() == 3){  // 포기목록
                    if(MainAdapter.state.equals("progress")){
                        goalInsertAnim(3);
                    }

                    MainAdapter.state = "fail";
                    titleTxt.setText("Fail");
                    title_bar.setBackgroundColor(Color.rgb(237,125,49));


                    insertGoal.setVisibility(insertGoal.GONE);

                    if(failPref.contains("sort")){
                        failSort = failPref.getInt("sort", 0);
                    }
                    sort(failSort);
                }
                adapter.notifyDataSetChanged();
                checkEmpty();
                return false;
            }
        });
        menu = popupMenu.getMenu();
        menu.add(0, 1, 0, "진행 목록");
        menu.add(0, 2, 0, "완료 목록");
        menu.add(0, 3, 0, "포기 목록");
        popupMenu.show();
    }



//    정렬 팝업 메뉴
    public void sortMenuOnclick(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId() == 1){  // 디데이순 오름, 완료순 내림, 포기순 내림
                    switch (MainAdapter.state){
                        case "progress" :
                            progressSort = 1;
                            progressEditor.putInt("sort", 1);
                            sort(progressSort);
                            break;
                        case "success" :
                            successSort = 4;
                            successEditor.putInt("sort", 4);
                            sort(successSort);
                            break;
                        case "fail" :
                            failSort = 5;
                            failEditor.putInt("sort", 5);
                            sort(failSort);
                            break;
                    }
                }
                else if(item.getItemId() == 2){ // 중요도순 내림
                    switch (MainAdapter.state){
                        case "progress" :
                            progressSort = 2;
                            progressEditor.putInt("sort", 2);
                            break;
                        case "success" :
                            successSort = 2;
                            successEditor.putInt("sort", 2);
                            break;
                        case "fail" :
                            failSort = 2;
                            failEditor.putInt("sort", 2);
                            break;
                    }
                    sort(2);
                }
                else if(item.getItemId() == 3){ // 등록순 내림
                    switch (MainAdapter.state){
                        case "progress" :
                            progressSort = 3;
                            progressEditor.putInt("sort", 3);
                            break;
                        case "success" :
                            successSort = 3;
                            successEditor.putInt("sort", 3);
                            break;
                        case "fail" :
                            failSort = 3;
                            failEditor.putInt("sort", 3);
                            break;
                    }
                    sort(3);
                }
                adapter.notifyDataSetChanged();

                progressEditor.apply();
                successEditor.apply();
                failEditor.apply();

                return false;
            }
        });
        menu = popupMenu.getMenu();
        if(MainAdapter.state.equals("progress")){
            menu.add(0, 1, 0, "디데이순");       // 등록순
        }
        else if(MainAdapter.state.equals("success")){
            menu.add(0, 1, 0, "완료순");       // 완료순
        }
        else if (MainAdapter.state.equals("fail")){
            menu.add(0, 1, 0, "포기순");       // 포기순
        }
        menu.add(0, 2, 0, "중요도순");      // 디데이순
        menu.add(0, 3, 0, "등록순");      // 중요도순
        popupMenu.show();
    }

//    정렬
    public void sort(int key){  // key==1 디데이 내림, key==2 중요도 오름, key==3 등록 내림, key==4 완료 내림, key==5 포기 내림
        // 디데이 내림차순
        if(key == 1){
            Comparator<MainListViewItem> ddayAsc = new Comparator<MainListViewItem>(){
                @Override
                public int compare(MainListViewItem item1, MainListViewItem item2) {
                    int dday1 = item1.getDday();
                    int dday2 = item2.getDday();

                    int importance1 = item1.getImportance();
                    int importance2 = item2.getImportance();

                    if(dday1 == dday2){ // 디데이가 같으면 중요도순
                        if(importance1 == importance2){ // 중요도도 같으면 등록순
                            return item2.getInsertDate().compareTo(item1.getInsertDate());
                        }
                        else{   // 중요도순
                            return importance2 - importance1;
                        }
                    }
                    else{
                        return dday2 - dday1;
                    }
                }
            };
            Collections.sort(MainAdapter.progressList, ddayAsc);
        }
        // 중요도 내림차순
        else if(key == 2){
            Comparator<MainListViewItem> importanceDesc = new Comparator<MainListViewItem>(){
                @Override
                public int compare(MainListViewItem item1, MainListViewItem item2) {

                    int importance1 = item1.getImportance();
                    int importance2 = item2.getImportance();

                    int dday1 = item1.getDday();
                    int dday2 = item2.getDday();

                    if(importance1 == importance2){ // 중요도가 같으면 디데이순
                        if(dday1 == dday2){ // 디데이도 같으면 등록순
                            return item2.getInsertDate().compareTo(item1.getInsertDate());
                        }
                        else{
                            return dday2 - dday1;
                        }
                    }
                    else{
                        return importance2 - importance1;
                    }
                }
            };
            switch (MainAdapter.state){
                case "progress" : Collections.sort(MainAdapter.progressList, importanceDesc); break;
                case "success" : Collections.sort(MainAdapter.successList, importanceDesc); break;
                case "fail" : Collections.sort(MainAdapter.failList, importanceDesc); break;
            }
        }
        // 등록 내림차순
        else if(key == 3){
            Comparator<MainListViewItem> dateDesc = new Comparator<MainListViewItem>(){
                @Override
                public int compare(MainListViewItem item1, MainListViewItem item2) {

                    return item2.getInsertDate().compareTo(item1.getInsertDate());
                }
            };
            switch (MainAdapter.state){
                case "progress" : Collections.sort(MainAdapter.progressList, dateDesc); break;
                case "success" : Collections.sort(MainAdapter.successList, dateDesc); break;
                case "fail" : Collections.sort(MainAdapter.failList, dateDesc); break;
            }
        }

        // 완료일 내림차순
        else if(key == 4){
            Comparator<MainListViewItem> successDateDesc = new Comparator<MainListViewItem>(){
                @Override
                public int compare(MainListViewItem item1, MainListViewItem item2) {

                    return item2.getSuccessDate().compareTo(item1.getSuccessDate());
                }
            };
            Collections.sort(MainAdapter.successList, successDateDesc);
        }

        // 포기일 내림차순
        else if(key == 5){
            Comparator<MainListViewItem> failDateDesc = new Comparator<MainListViewItem>(){
                @Override
                public int compare(MainListViewItem item1, MainListViewItem item2) {

                    return item2.getFailDate().compareTo(item1.getFailDate());
                }
            };
            Collections.sort(MainAdapter.failList, failDateDesc);
        }

    }

//    목록이 비어있을 때 디자인 셋팅
    public void checkEmpty(){    // 삭제 부분에 추가하기

//        if(pref.contains("progressList")){  // 등록한 아이템이 있으면
        if(MainAdapter.state.equals("progress") && MainAdapter.progressList.size() == 0){
            sentence.setVisibility(sentence.VISIBLE);
            sentence.setText("목표를 생성해주세요.");
        }
        else if(MainAdapter.state.equals("success") && MainAdapter.successList.size() == 0){
            sentence.setVisibility(sentence.VISIBLE);
            sentence.setText("완료한 목표가 없습니다.");
        }
        else if(MainAdapter.state.equals("fail") && MainAdapter.failList.size() == 0){
            sentence.setVisibility(sentence.VISIBLE);
            sentence.setText("포기한 목표가 없습니다.");
        }
        else{
            sentence.setVisibility(sentence.GONE);
        }

        if(noticeAr.size() == 0){
            notice.setVisibility(notice.GONE);
            foldNotice.setVisibility(foldNotice.GONE);
            unfoldNotice.setVisibility(unfoldNotice.GONE);
        }
        else if(useNotice){
            notice.setVisibility(notice.VISIBLE);
            foldNotice.setVisibility(foldNotice.VISIBLE);
            unfoldNotice.setVisibility(unfoldNotice.GONE);
        }
        else if(!useNotice){
            notice.setVisibility(notice.GONE);
            foldNotice.setVisibility(foldNotice.GONE);
            unfoldNotice.setVisibility(unfoldNotice.VISIBLE);
        }
    }

//    공지 접기 버튼 온클릭
    public void foldNoticeOnclick(View v){
        progressEditor.putBoolean("notice", false); //  false은 미사용, true은 공지 사용
        progressEditor.apply();
        useNotice = false;
        adapter.notifyDataSetCnt = 5;   // 공지를 접기, 펴기할 때 getView가 새로 그려지는데, 애니메이션을 미적용시키기 위해
        adapter.notifyDataSet = true;   // 공지를 접기, 펴기할 때 getView가 새로 그려지는데, 애니메이션을 미적용시키기 위해

        AlphaAnimation foldAlphaAnim = new AlphaAnimation(1.0f, 0.0f);
        foldAlphaAnim.setDuration(300);
        notice.startAnimation(foldAlphaAnim);
        notice.setVisibility(notice.GONE);
        foldNotice.setVisibility(foldNotice.GONE);

        unfoldNotice.setVisibility(unfoldNotice.VISIBLE);

        th3.interrupt();    // 공지 스레드 정지
    }

//    공지 펴기 버튼 온클릭
    public void unfoldNoticeOnclick(View v){
        progressEditor.putBoolean("notice", true); //  false은 미사용, true은 공지 사용
        progressEditor.apply();
        useNotice = true;
        adapter.notifyDataSetCnt = 5;   // 공지를 접기, 펴기할 때 getView가 새로 그려지는데, 애니메이션을 미적용시키기 위해
        adapter.notifyDataSet = true;   // 공지를 접기, 펴기할 때 getView가 새로 그려지는데, 애니메이션을 미적용시키기 위해

        notice.setVisibility(notice.VISIBLE);
        foldNotice.setVisibility(foldNotice.VISIBLE);
        AlphaAnimation foldAlphaAnim = new AlphaAnimation(0.0f, 1.0f);
        foldAlphaAnim.setDuration(300);
        notice.startAnimation(foldAlphaAnim);

        unfoldNotice.setVisibility(unfoldNotice.GONE);

        notice();   // 공지 스레드 실행
    }

    //    생명주기
    @Override
   protected void onRestart(){
        super.onRestart();
        Log.i(TAG, "onRestart() called");

        if(MainAdapter.state.equals("progress")){   // 목표 수정 후 목록으로 돌아올 때 정렬
            sort(progressSort);
        }

        if(updateFlag){     // 목표 수정 후 목록으로 돌아올 때만 data set함
            adapter.notifyDataSetChanged();
            updateFlag = false;
        }

        checkEmpty();   // 목표를 수정하고 왔을 때, 공지 TextView를 GONE 해야하는지 체크
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
        notice();   // 공지쓰레드 실행
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.i(TAG, "onPause() called");
        if(useNotice){  // 공지가 사용중일 때만 종료
            th3.interrupt();    // 공지쓰레드 종료
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.i(TAG, "onStop() called------------");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        MainAdapter.state = "progress";
        Log.i(TAG, "onDestroy() called");
    }



    //    리스트뷰 텍스트 공유하기 (되지만 현재 사용 안해서 뺐음)
//    public void shareTxt(int position){
//        List targetedShareIntents = new ArrayList<>();
//        MainListViewItem item = MainAdapter.itemList.get(position);
//
////      카카오톡
//        Intent kakaotalkIntent = getShareIntent("kakao.talk", item.getGoalTitle(), item.getDifficulty(), item.getPeriod(), item.getDday());
//        if(kakaotalkIntent != null){
//            targetedShareIntents.add(kakaotalkIntent);
//        }
//
//
////      페이스북
//        Intent facebookIntent = getShareIntent("facebook", item.getGoalTitle(), item.getDifficulty(), item.getPeriod(), item.getDday());
//        if(facebookIntent != null){
//            targetedShareIntents.add(facebookIntent);
//        }
//
//        Intent chooser = Intent.createChooser((Intent) targetedShareIntents.remove(0), "공유하기");
//        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
//        startActivity(chooser);
//    }
//
//
//    //    공유 앱 필터링
//    private Intent getShareIntent(String name, String goalTxt, String difficulty, String period, String dday) {
//        boolean found = false;
//
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("text/plain");
//
//        // gets the list of intents that can be loaded.
//        List<ResolveInfo> resInfos = getPackageManager().queryIntentActivities(intent, 0);
//
//
//        if (resInfos == null || resInfos.size() == 0)
//            return null;
//
//        for (ResolveInfo info : resInfos) {
//            if (info.activityInfo.packageName.toLowerCase().contains(name) || info.activityInfo.name.toLowerCase().contains(name)) {
//                String shareFormat = "목표: "+goalTxt+"\n난이도: "+difficulty+"\n기간: "+period+"(D-"+dday+")";
//                intent.putExtra(Intent.EXTRA_TEXT, shareFormat);
//
//                intent.setPackage(info.activityInfo.packageName);
//                found = true;
//                break;
//            }
//        }
//
//        if(found)
//            return intent;
//
//        return null;
//    }
}






