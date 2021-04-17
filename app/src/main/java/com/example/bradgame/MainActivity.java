package com.example.bradgame;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    private TextView[] input = new TextView[4];
    private int[] inputRes =
            {R.id.main_input1,R.id.main_input2,R.id.main_input3,R.id.main_input4}; //找到input的id
    private LinkedList<Integer> inputValue = new LinkedList<>();   // 輸入數值陣列
    private View[] btnNumber = new View[10]; //在android裡任何一個View都可以被按下去(所以宣告為此)
    private int[] numberRes = {R.id.main_btn_0,R.id.main_btn_1,R.id.main_btn_2,
            R.id.main_btn_3,R.id.main_btn_4,R.id.main_btn_5,R.id.main_btn_6,
            R.id.main_btn_7,R.id.main_btn_8,R.id.main_btn_9}; //找到數字鍵的id
    private LinkedList<Integer> answer = new LinkedList<>();
    private int inputPoint;     // 輸入指標位置 0 - 3

    private ListView listView;
    private SimpleAdapter adapter;
    private String[] from = {"order","guess","result"};
    private int[] to = {R.id.item_order,R.id.item_guess,R.id.item_result};
    private LinkedList<HashMap<String,String>> hist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initGame();
        initListView();
    }

    // 初始化畫面
    private void initView() {
        for (int i=0; i<inputRes.length; i++){
            input[i] = findViewById(inputRes[i]); //透過迴圈把input值找到
        }
        for (int i=0; i<numberRes.length; i++){
            btnNumber[i] = findViewById(numberRes[i]); //也是透過迴圈把你按下哪個btn(數值)找到
        }
    }
    // 初始化一局遊戲
    private void initGame(){
        answer = createAnswer(); // 產生謎底
        clear(null); //產生謎底並重來的一局把東西清掉??
        Log.d("bradd", "" + answer );
    }
    private LinkedList<Integer> createAnswer(){
        LinkedList<Integer> ret = new LinkedList<>();
        HashSet<Integer> nums = new HashSet<>(); //不會產生相同的數字
        while (nums.size()<4){ //需要四個數字
            nums.add((int)(Math.random()*10)); //0~9 //nums是HashSet型別沒有順序性
        }
        for (Integer i : nums){
            ret.add(i);                              //但LinkedList有
        }
        Collections.shuffle(ret);                    //所以我們透過Collections.shuffle()重新洗牌
        return ret;
    }
    // 初始化 ListView(調變器的部分)
    private void initListView(){
        listView = findViewById(R.id.main_listview);
        hist = new LinkedList<>();
        adapter = new SimpleAdapter(this,hist,R.layout.item_round,from,to);
        listView.setAdapter(adapter);
    }

    //使用者互動區
    public void inputNumber(View view) {
        if (inputPoint == 4) return;    // 已按四個數字，此時只能 send or back or clear
        // 比對輸入鍵
        for (int i=0; i<btnNumber.length; i++){
            if (view == btnNumber[i]){
                // 輸入 i 鍵
                inputValue.set(inputPoint,i); //設定inputValue為(0,你按的數字鍵) (1,)以此類推
                input[inputPoint].setText("" + i); //設定input[0]=你按的數字鍵
                inputPoint++; //0 => 1
                btnNumber[i].setEnabled(false); //設定此btn不能按了><
                break;
            }
        }
    }
    public void back(View view) {
        if (inputPoint == 0) return; //什麼都沒有按 就不會back()

        inputPoint--;                   //1 => 0
        btnNumber[inputValue.get(inputPoint)].setEnabled(true); //設定此btn可以按
        inputValue.set(inputPoint, -1); //設定inputValue為(0,-1) -1可能是不顯示的意思??
        input[inputPoint].setText("");  //設定input[0]為空白
    }
    public void clear(View view) {
        inputPoint = 0;
        inputValue.clear();
        for (int i=0; i<4; i++){
            inputValue.add(-1);
        }
        for (int i = 0; i < input.length; i++) {
            input[i].setText("");
        }
        for(int i = 0; i<btnNumber.length; i++){
            btnNumber[i].setEnabled(true);
        }
    }
    public void send(View view) {
        if (inputPoint != 4) return; //還沒四個數字不能送出答案

        int a, b; a = b = 0; String guess = "";
        for (int i=0; i<inputValue.size(); i++){ //來算幾A幾B
            guess += inputValue.get(i);
            if (inputValue.get(i).equals(answer.get(i))){ //相同數字相同位置 A
                a++;
            }else if (answer.contains(inputValue.get(i))){ //相同數字不同位置 B
                b++;
            }
        }
        Log.d("brad", a + "A" + b + "B");
        clear(null);

        HashMap<String,String> row = new HashMap<>();
        row.put(from[0], "" + (hist.size()+1));
        row.put(from[1], guess);
        row.put(from[2], a + "A" + b + "B");
        hist.add(row);
        adapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(hist.size()-1);

        if (a == 4){
            // winner
            displayResult(true);
        }else if(hist.size() == 10){
            // loser
            displayResult(false);
        }
    }
    public void replay(View view) {
        initGame();
        hist.clear();
        adapter.notifyDataSetChanged();
    }

    // 顯示輸贏結果(彈出框)
    private void displayResult(boolean isWinner){
        AlertDialog.Builder builder = new AlertDialog.Builder(this); //建一個彈出框
        builder.setTitle("遊戲結果");

        StringBuffer ansString = new StringBuffer();
        for (int i=0; i<answer.size();i++) ansString.append(answer.get(i)); //答案放到ansString

        builder.setMessage(isWinner?"完全正確":"挑戰失敗\n" + "答案:" + ansString); //彈出框文字內容
        builder.setPositiveButton("開新局", new DialogInterface.OnClickListener() { //開新局的btn
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                replay(null);
            }
        });
        builder.create().show();
    }
}