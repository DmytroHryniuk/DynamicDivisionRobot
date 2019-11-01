package com.hryniuk.dyndivrob;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hryniuk.dyndivrob.mqtt.MqttHelper;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;




public class MainActivity extends AppCompatActivity {
    MqttHelper mqttHelper;
    TextView mTextField;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextField= findViewById(R.id.con_text);

        animTimer = new CountDownTimer(5000, 1000) {
            int i = 1;

            public void onTick(long millisUntilFinished) {
                if (i <= 3) {
                    String text = mTextField.getText().toString();
                    mTextField.setText(text + ".");
                } else {
                    i = 0;
                    mTextField.setText("Connecting");
                }
                i++;
            }

            public void onFinish() {
                animTimer.start();
            }
        }.start();


        fullSCreen();
        overridePendingTransition(0, 0);
        startMqtt();

    }

    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Debug","Connected");

                mqttHelper.publishToTopic("ping1");
                Toast.makeText(MainActivity.this, "ping1", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Debug",mqttMessage.toString());
                if (mqttMessage.toString().equals("here")){

                    Toast.makeText(MainActivity.this, "here", Toast.LENGTH_SHORT).show();
                    animTimer.cancel();
                    Intent theIntent = new Intent(getApplicationContext(), StartActivity.class);
                    startActivity(theIntent);
                    overridePendingTransition(0, 0);


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
        fullSCreen();
    }





}
