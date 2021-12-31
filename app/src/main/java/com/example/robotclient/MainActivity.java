package com.example.robotclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {
    private Socket socket= null;
    private DataOutputStream dout;
    private Switch aSwitch;
    private Switch bSwitch;
    private int clickCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aSwitch = findViewById(R.id.switch1);
        bSwitch = findViewById(R.id.switch2);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ExecutorService executorService = Executors.newFixedThreadPool(1);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(isChecked) {
                                dout.writeUTF("mouse");
                                dout.flush();
                            }else{
                                dout.writeUTF("message");
                                dout.flush();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                executorService.shutdown();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dout.writeUTF("stop");
                    dout.flush();
                    dout.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.shutdown();
    }

    public void connectButtonClicked(View v){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("192.168.137.1",6666);
                    dout=new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.shutdown();
    }

    public void sendButtonClicked(View v){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    if(!aSwitch.isChecked()) {
                        EditText editText = findViewById(R.id.editText);
                        dout.writeUTF("Message: "+editText.getText().toString());
                        dout.flush();
                    }
                   //

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.shutdown();
    }



    public void disconnectButtonClicked(View v){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dout.writeUTF("stop");
                    dout.flush();
                    dout.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        executorService.shutdown();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_MOVE){
            int historySize = event.getHistorySize();
            float lastSeenX = 0;
            float lastSeenY = 0;
            if(historySize>0){
                lastSeenX = event.getHistoricalX(historySize-1);
                lastSeenY = event.getHistoricalY(historySize-1);
            }

            float totalXDistance = event.getX()-lastSeenX;
            float totalYDistance = event.getY()-lastSeenY;

            ExecutorService executorService = Executors.newFixedThreadPool(1);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(aSwitch.isChecked()) {
                            dout.writeUTF( (int)totalXDistance +"," +(int)totalYDistance);
                            dout.flush();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            executorService.shutdown();
        }else if(event.getAction() == MotionEvent.ACTION_DOWN){
            clickCount++;
            if(clickCount>2){
                ExecutorService executorService = Executors.newFixedThreadPool(1);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(aSwitch.isChecked() && bSwitch.isChecked()) {
                                dout.writeUTF("double");
                            }else{
                                dout.writeUTF("single");
                            }
                            dout.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                executorService.shutdown();
                clickCount=0;
            }
        }
        return super.onTouchEvent(event);
    }

    public static int getWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }
}