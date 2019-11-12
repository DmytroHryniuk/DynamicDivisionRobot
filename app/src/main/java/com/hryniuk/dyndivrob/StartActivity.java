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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class StartActivity extends AppCompatActivity {
    ImageView logo_left;
    ImageView logo_right;
    MqttHelper mqttHelper;
    String mes_text = "";
    CountDownTimer waitTimer;
    boolean isRobotStart;

    protected void fullSCreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        logo_left = findViewById(R.id.logo_left);
        logo_right = findViewById(R.id.logo_right);
        isRobotStart = false;
        fullSCreen();
        overridePendingTransition(0, 0);
        startMqtt();
        start_click();
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
                if (mqttMessage.toString().contains("strtok")) {
                    mes_text = mqttMessage.toString();


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
    protected void onStart() {
        super.onStart();
        logo_left.getDrawable().setColorFilter(0x99FFFFFF, PorterDuff.Mode.SRC_ATOP);
        logo_left.invalidate();
        fullSCreen();
        start_click();

    }


    @Override
    public void onBackPressed() {

        //   super.onBackPressed();

    }


    @SuppressLint("ClickableViewAccessibility")
    private void start_click() {
        logo_right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (logo_right.isEnabled()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Toast.makeText(StartActivity.this, "{\"data\":\"start\"}", Toast.LENGTH_SHORT).show();
                        //TODO: START
                        mqttHelper.publishToTopic("{\"data\":\"start\"}");
                        logo_right.setEnabled(false);
                        //
                        //TODO: WAIT STARTOK
                        waitTimer = new CountDownTimer(8000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                if (mes_text.contains("strtok")) {
                                    isRobotStart = true;
                                    Toast.makeText(StartActivity.this, "strtok", Toast.LENGTH_SHORT).show();
                                    if (waitTimer != null) {
                                        waitTimer.onFinish();
                                        waitTimer.cancel();
                                        waitTimer = null;
                                    }
                                } else {
                                    isRobotStart = false;
                                }
                            }

                            public void onFinish() {

                                if (isRobotStart) {
                                    mes_text = "";
                                    logo_right.setEnabled(false);
                                    Toast.makeText(StartActivity.this, "strtok", Toast.LENGTH_SHORT).show();
                                    Intent theIntent = new Intent(getApplicationContext(), StopActivity.class);
                                    startActivity(theIntent);
                                    overridePendingTransition(0, 0);

                                } else {
                                    Toast.makeText(StartActivity.this, "Failed: Please, try again...", Toast.LENGTH_SHORT).show();
                                    logo_right.setEnabled(true);
                                }

                                isRobotStart = false;

                            }
                        }.start();
                    }
                }
                return true;
            }
        });
    }
}
