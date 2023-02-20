package com.example.mqtt;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.decimal4j.util.DoubleRounder;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity {
    GraphView graphView;
    private String payload;
    private LineGraphSeries<DataPoint> series;
    int count = 0;
    double temp;
    String server = "tcp://13.212.218.255:1883";
    String clientID = "phone";
    private static final String TAG = "MainActivity";
    private MqttAndroidClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new MqttAndroidClient(this.getApplicationContext(), server, clientID);
        graphView = findViewById(R.id.graph);
        graphView.setTitle("Temperature From MQTT");
        graphView.getGridLabelRenderer().setVerticalAxisTitle("Temperature(C)");
        graphView.getGridLabelRenderer().setHorizontalAxisTitle("Counts(Times)");
        series = new LineGraphSeries<>();
        graphView.addSeries(series);


        connect();



    }

    private void connect() {
        try{
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess...");
                    sub();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "onFailure...");
                }
            });
        }catch(MqttException e){
           e.printStackTrace();
        }
    }
    private void sub(){
        try {
            client.subscribe("/sensor/temp", 0);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
//                    Log.d(TAG, "" + new String(message.getPayload()));
                    payload = new String(message.getPayload());
//                    Log.d(TAG, "Payload: " + payload);
                    temp = Double.parseDouble(payload);
                    temp = DoubleRounder.round(temp,2);
//                    Log.d(TAG, "Temp: " + temp);
                    count += 1;
                    series.appendData(new DataPoint(count, temp),false, 40);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

}