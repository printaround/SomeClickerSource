package com.testing.someclicker;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button start_btn;
    private TextView point_info, final_info, time_and_phases_info;

    private short speed;
    private byte sec = 0, minute = 0; //Time variables
    private int points_count = 0, best_result = 0;
    private boolean game_over = true;

    private static final short defV = 25; //Default hp Value
    private final byte count_of_panels = 9;

    ImageView[] panel_num = new ImageView[count_of_panels];
    short[] hp = new short[count_of_panels]; //HP of panels
    byte[] canvas_num = new byte[count_of_panels]; //To determine the current canvas number on the panel (1-3), serves to reduce the frame rate.

    private static final String File_Name = "Best_record.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //Hide status bar
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // Fixed portrait

        start_btn = findViewById (R.id.startBtn);
        time_and_phases_info = findViewById (R.id.timeAndPhasesInfo);
        point_info = findViewById (R.id.pointInfo);
        final_info = findViewById (R.id.finalInfo);

        panel_num[0] = findViewById (R.id.panel_iV_1);
        panel_num[1] = findViewById (R.id.panel_iV_2);
        panel_num[2] = findViewById (R.id.panel_iV_3);
        panel_num[3] = findViewById (R.id.panel_iV_4);
        panel_num[4] = findViewById (R.id.panel_iV_5);
        panel_num[5] = findViewById (R.id.panel_iV_6);
        panel_num[6] = findViewById (R.id.panel_iV_7);
        panel_num[7] = findViewById (R.id.panel_iV_8);
        panel_num[8] = findViewById (R.id.panel_iV_9);

        best_result = Load();
        final_info.setText(getString(R.string.your_result_text) + " " + best_result);
    }

    @SuppressLint("SetTextI18n")
    public void onStartClick(View view) {
        if(game_over) {

            game_over = false;
            start_btn.setAlpha(0.2f);
            for (byte i = 0; i < count_of_panels; i++) panel_num[i].setImageResource(R.drawable.panel_image_11);
            for (byte i = 0; i < count_of_panels; i++) canvas_num[i] = 1;
            for (byte i = 0; i < count_of_panels; i++) hp[i] = defV;
            points_count = 0;
            point_info.setText(getString(R.string.point_info_points_text) + " " + points_count);
            speed = 1000;
            sec = 0;
            minute = 0;

            //Thread for time counting and speed upping
            new Thread((Runnable) () -> {
                while (!game_over) {
                    time_and_phases_info.setText(getString(R.string.time_text) + " " + minute + " " + getString(R.string.minute_text) + " " + sec + " " + getString(R.string.sec_text));
                    Sleep((short) 1000);
                    sec++;
                    if(sec >= 60) {minute++; sec = 0;}
                    if(speed > 100) speed-=2;
                }
            }).start();

            //Thread for Updating + Check for best(record) result
            new Thread((Runnable) () -> {
                while (!game_over) {
                    for (short i = 0; i < count_of_panels; i++) {

                        if (hp[i] < 5) { //If GAME OVER
                            game_over = true;
                            point_info.setText(getString(R.string.game_over_text) + " " + points_count + " " + getString(R.string.points_text));

                            if(best_result < points_count) {best_result = points_count; Save();}
                            final_info.setText(getString(R.string.your_result_text) + " " + best_result);
                            break;

                        } else if (hp[i] >= 20) {
                            if(canvas_num[i]!=1) panel_num[i].setImageResource(R.drawable.panel_image_11);
                            canvas_num[i] = 1;
                        }
                        else if (hp[i] >= 15) {
                            if(canvas_num[i]!=2) panel_num[i].setImageResource(R.drawable.panel_image_12);
                            canvas_num[i] = 2;
                        }
                        else {
                            if(canvas_num[i]!=3) panel_num[i].setImageResource(R.drawable.panel_image_13);
                            canvas_num[i] = 3;
                        }
                    }
                    Sleep((short) 100);
                }
                start_btn.setAlpha(1f); //Returning "Start" button
            }).start();

            //Threat for minus of points
            new Thread((Runnable) () -> {
                while (!game_over) {
                    for (short i = 0; i < hp.length; i++) {
                        hp[i]--;
                    }
                    Sleep(speed);
                }
            }).start();
        }
    }

    //Save current and max points count
    private void Save(){
        String text = String.valueOf(best_result);
        FileOutputStream f_o_s = null;
        try {
            f_o_s = openFileOutput(File_Name, MODE_PRIVATE);
            f_o_s.write(text.getBytes());
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally{
            if(f_o_s != null) {
                try {
                    f_o_s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Getting for last max points count
    private int Load(){
        FileInputStream f_i_s = null;
        try {
            f_i_s = openFileInput(File_Name);
            InputStreamReader i_s_r = new InputStreamReader(f_i_s);
            BufferedReader b_r = new BufferedReader(i_s_r);
            StringBuilder s_b = new StringBuilder();
            String text;

            while((text = b_r.readLine()) != null) {
                s_b.append(text).append("\n");
            }

            int int_best_rec = 0;
            for(byte i = 1; i < 6; i++) {
                text = String.valueOf(s_b.substring(0, i));
                try{
                    int_best_rec = Integer.valueOf(text);
                }catch(NumberFormatException e){
                    break;
                }
            }
            return int_best_rec;

        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (f_i_s != null){
                try {
                    f_i_s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    @SuppressLint("SetTextI18n")
    private void PointCounter(){
        if(!game_over) {
            points_count++;
            point_info.setText(getString(R.string.point_info_points_text) + " " + points_count);
        }
    }

    protected void Sleep(short spd){
        try {
            Thread.sleep(spd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onExit(View view){
        finish();
        System.exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.panel_iV_1: {hp[0]++; PointCounter(); break;}
            case R.id.panel_iV_2: {hp[1]++; PointCounter(); break;}
            case R.id.panel_iV_3: {hp[2]++; PointCounter(); break;}
            case R.id.panel_iV_4: {hp[3]++; PointCounter(); break;}
            case R.id.panel_iV_5: {hp[4]++; PointCounter(); break;}
            case R.id.panel_iV_6: {hp[5]++; PointCounter(); break;}
            case R.id.panel_iV_7: {hp[6]++; PointCounter(); break;}
            case R.id.panel_iV_8: {hp[7]++; PointCounter(); break;}
            case R.id.panel_iV_9: {hp[8]++; PointCounter(); break;}
            }
        }
}