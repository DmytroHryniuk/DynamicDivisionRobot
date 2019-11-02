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
import android.widget.TextView;
import android.widget.Toast;

import com.hryniuk.dyndivrob.mqtt.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class DockActivity extends AppCompatActivity {

    ImageView logo_left;
    ImageView logo_right;
    ImageView logo_stop;
    TextView mTextField;
    MqttHelper mqttHelper;
    String mes_text = "";
    boolean isRobotStop;
    CountDownTimer waitTimer;
    CountDownTimer animTimer;
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
                if (mqttMessage.toString().equals("pok")) {
                    mes_text = mqttMessage.toString();


                } else if (mqttMessage.toString().contains("docked")) {
                    mes_text = mqttMessage.toString();

                    /**/
                    Toast.makeText(DockActivity.this, "docked", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_dock);
        mTextField= findViewById(R.id.con_text);
        animTimer = new CountDownTimer(5000, 1000) {
            int i = 1;

            public void onTick(long millisUntilFinished) {
                if (i <= 3) {
                    String text = mTextField.getText().toString();
                    mTextField.setText(text + ".");
                } else {
                    i = 0;
                    mTextField.setText("Going back to the dock");
                }
                i++;
            }

            public void onFinish() {
                animTimer.start();
            }
        }.start();



        logo_left = findViewById(R.id.logo_left);
        logo_right = findViewById(R.id.logo_right);
        logo_stop = findViewById(R.id.logo_stop);
        isRobotStop = false;
        fullSCreen();
        overridePendingTransition(0, 0);
        startMqtt();
        stop_click();

    }

    @Override
    protected void onStart() {
        super.onStart();
        logo_right.getDrawable().setColorFilter(0x99FFFFFF, PorterDuff.Mode.SRC_ATOP);
        logo_right.invalidate();
        fullSCreen();
        stop_click();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void stop_click() {
        logo_stop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (logo_stop.isEnabled()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Toast.makeText(DockActivity.this, "pause1", Toast.LENGTH_SHORT).show();
                        //TODO: PAUSE
                        mqttHelper.publishToTopic("pause1");
                        logo_stop.setEnabled(false);
                        //
                        //TODO: WAIT PAUSEOK
                        waitTimer = new CountDownTimer(8000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                if (mes_text.equals("pok")) {
                                    isRobotStop = true;
                                    Toast.makeText(DockActivity.this, "pok", Toast.LENGTH_SHORT).show();
                                    if (waitTimer != null) {
                                        waitTimer.onFinish();
                                        waitTimer.cancel();
                                        waitTimer = null;
                                    }
                                } else {
                                    isRobotStop = false;
                                }
                            }

                            public void onFinish() {

                                if (isRobotStop) {
                                    mes_text = "";
                                    logo_stop.setEnabled(false);
                                    Toast.makeText(DockActivity.this, "pok", Toast.LENGTH_SHORT).show();
                                    animTimer.cancel();
                                    Intent theIntent = new Intent(getApplicationContext(), PauseActivity.class);
                                    startActivity(theIntent);
                                    overridePendingTransition(0, 0);

                                } else {
                                    Toast.makeText(DockActivity.this, "Please, try again...", Toast.LENGTH_SHORT).show();
                                    logo_stop.setEnabled(true);
                                }

                                isRobotStop = false;

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
