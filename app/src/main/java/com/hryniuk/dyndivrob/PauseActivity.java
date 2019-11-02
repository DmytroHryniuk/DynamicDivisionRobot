package com.hryniuk.dyndivrob;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hryniuk.dyndivrob.mqtt.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class PauseActivity extends AppCompatActivity {
    ImageView logo_left;
    ImageView logo_right;
    ImageView logo_pause;
    MqttHelper mqttHelper;
    String mes_text = "";
    boolean isRobotResume;
    boolean isRobotDocked;
    CountDownTimer waitTimer;

    protected void fullSCreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


    private void startMqtt() {
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Debug", "Connected");

            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug", mqttMessage.toString());
                if (mqttMessage.toString().equals("rsmok")) {
                    mes_text = mqttMessage.toString();


                } else if (mqttMessage.toString().equals("gdock")) {
                    mes_text = mqttMessage.toString();


                } else if (mqttMessage.toString().contains("docked")) {
                    mes_text = mqttMessage.toString();

                    /**/
                    Toast.makeText(PauseActivity.this, "docked", Toast.LENGTH_SHORT).show();
                    Intent theIntent = new Intent(getApplicationContext(), FinishActivity.class);
                    theIntent.putExtra("dataString", mes_text);
                    startActivity(theIntent);
                    overridePendingTransition(0, 0);
                    /**/

                } else {
                    mes_text = "";
                    Log.i("infoT", mqttMessage.toString());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pause);
        logo_left = findViewById(R.id.logo_left);
        logo_right = findViewById(R.id.logo_right);
        logo_pause = findViewById(R.id.logo_pause);
        isRobotResume = false;
        isRobotDocked = false;
        fullSCreen();
        overridePendingTransition(0, 0);
        startMqtt();
        resume_click();
        dock_click();
    }

    @Override
    protected void onStart() {
        super.onStart();
        logo_pause.getDrawable().setColorFilter(0x99FFFFFF, PorterDuff.Mode.SRC_ATOP);
        logo_pause.invalidate();
        fullSCreen();
        resume_click();
        dock_click();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void resume_click() {
        logo_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (logo_right.isEnabled()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Toast.makeText(PauseActivity.this, "rsm", Toast.LENGTH_SHORT).show();
                        //TODO: RESUME
                        mqttHelper.publishToTopic("rsm");
                        logo_right.setEnabled(false);
                        logo_left.setEnabled(false);
                        //
                        //TODO: WAIT RESUMEOK
                        waitTimer = new CountDownTimer(8000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                if (mes_text.equals("rsmok")) {
                                    isRobotResume = true;
                                    Toast.makeText(PauseActivity.this, "rsmok", Toast.LENGTH_SHORT).show();
                                    if (waitTimer != null) {
                                        waitTimer.onFinish();
                                        waitTimer.cancel();
                                        waitTimer = null;
                                    }
                                } else if (mes_text.equals("gdock")) {
                                    isRobotDocked = true;
                                    Toast.makeText(PauseActivity.this, "gdock", Toast.LENGTH_SHORT).show();
                                    if (waitTimer != null) {
                                        waitTimer.onFinish();
                                        waitTimer.cancel();
                                        waitTimer = null;
                                    }
                                } else {
                                    isRobotResume = false;
                                    isRobotDocked = false;
                                }
                            }

                            public void onFinish() {

                                if (isRobotResume) {
                                    mes_text = "";
                                    logo_right.setEnabled(false);
                                    logo_left.setEnabled(false);
                                    Toast.makeText(PauseActivity.this, "rsmok", Toast.LENGTH_SHORT).show();
                                    Intent theIntent = new Intent(getApplicationContext(), StopActivity.class);
                                    startActivity(theIntent);
                                    overridePendingTransition(0, 0);


                                } else if (isRobotDocked) {
                                    mes_text = "";
                                    logo_right.setEnabled(false);
                                    logo_left.setEnabled(false);
                                    Toast.makeText(PauseActivity.this, "gdock", Toast.LENGTH_SHORT).show();
                                    Intent theIntent = new Intent(getApplicationContext(), DockActivity.class);
                                    startActivity(theIntent);
                                    overridePendingTransition(0, 0);


                                } else {
                                    Toast.makeText(PauseActivity.this, "Please, try again...", Toast.LENGTH_SHORT).show();
                                    logo_right.setEnabled(true);
                                    logo_left.setEnabled(true);
                                }

                                isRobotResume = false;
                                isRobotDocked = false;
                            }
                        }.start();
                    }
                }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void dock_click() {
        logo_left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (logo_left.isEnabled()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Toast.makeText(PauseActivity.this, "dock", Toast.LENGTH_SHORT).show();
                        //TODO: GDOCK
                        mqttHelper.publishToTopic("dock");
                        logo_left.setEnabled(false);
                        logo_right.setEnabled(false);
                        //
                        //TODO: WAIT GDOCKOK
                        waitTimer = new CountDownTimer(8000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                if (mes_text.equals("gdock")) {
                                    isRobotDocked = true;
                                    Toast.makeText(PauseActivity.this, "gdock", Toast.LENGTH_SHORT).show();
                                    if (waitTimer != null) {
                                        waitTimer.onFinish();
                                        waitTimer.cancel();
                                        waitTimer = null;
                                    }
                                } else {

                                    isRobotDocked = false;
                                }
                            }

                            public void onFinish() {

                                if (isRobotDocked) {
                                    mes_text = "";
                                    logo_left.setEnabled(false);
                                    logo_right.setEnabled(false);
                                    Toast.makeText(PauseActivity.this, "gdock", Toast.LENGTH_SHORT).show();
                                    Intent theIntent = new Intent(getApplicationContext(), DockActivity.class);
                                    startActivity(theIntent);
                                    overridePendingTransition(0, 0);


                                } else {
                                    Toast.makeText(PauseActivity.this, "Please, try again...", Toast.LENGTH_SHORT).show();
                                    logo_left.setEnabled(true);
                                    logo_right.setEnabled(true);
                                }

                                isRobotDocked = false;
                            }
                        }.start();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {

        //   super.onBackPressed();

    }

}
