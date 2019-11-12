package com.hryniuk.dyndivrob;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hryniuk.dyndivrob.mqtt.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class FinishActivity extends AppCompatActivity {
    TextView ver_text;
    TextView data_text;

    ImageView logo_full;

    String dataString = "";

    MqttHelper mqttHelper;
    String mes_text = "";
    CountDownTimer waitTimer;
    boolean isRobotRestart;

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
                if (mqttMessage.toString().contains("here")) {
                    mes_text = mqttMessage.toString();


                }  else {
                    mes_text = "";
                    Log.i("infoFinishActivity", mqttMessage.toString());
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
        setContentView(R.layout.activity_finish);
        isRobotRestart = false;
        ver_text = findViewById(R.id.ver_text);
        data_text = findViewById(R.id.data_text);
        logo_full = findViewById(R.id.logo_full);
        Intent i = getIntent();
        dataString = i.getStringExtra("dataString");
        ver_text.setText("Robot Dummy-2\nv1.01.pre-a");
        data_text.setText(dataString);
        fullSCreen();

        overridePendingTransition(0, 0);

        startMqtt();
        restart_click();


    }

    @Override
    protected void onStart() {
        super.onStart();
        fullSCreen();
        restart_click();

    }

    public void onBackPressed() {

        //   super.onBackPressed();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void restart_click() {
        logo_full.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (logo_full.isEnabled()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Toast.makeText(FinishActivity.this, "{\"data\":\"ping1\"}", Toast.LENGTH_SHORT).show();
                        //TODO: RESTART
                        mqttHelper.publishToTopic("{\"data\":\"ping1\"}");
                        logo_full.setEnabled(false);
                        //
                        //TODO: WAIT RESTARTOK
                        waitTimer = new CountDownTimer(8000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                if (mes_text.contains("here")) {
                                    isRobotRestart = true;
                                    Toast.makeText(FinishActivity.this, "here", Toast.LENGTH_SHORT).show();
                                    if (waitTimer != null) {
                                        waitTimer.onFinish();
                                        waitTimer.cancel();
                                        waitTimer = null;
                                    }
                                } else {
                                    isRobotRestart = false;
                                }
                            }

                            public void onFinish() {

                                if (isRobotRestart) {
                                    mes_text = "";
                                    logo_full.setEnabled(false);
                                    Toast.makeText(FinishActivity.this, "here", Toast.LENGTH_SHORT).show();
                                    Intent theIntent = new Intent(getApplicationContext(), StartActivity.class);
                                    startActivity(theIntent);
                                    overridePendingTransition(0, 0);

                                } else {
                                    Toast.makeText(FinishActivity.this, "Please, try again...", Toast.LENGTH_SHORT).show();
                                    logo_full.setEnabled(true);
                                }

                                isRobotRestart = false;

                            }
                        }.start();
                    }
                }
                return true;
            }
        });
    }
}
