package com.example.chatbot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 123;
    BroadcastReceiver bReceiver;
    SmsManager smsManager;
    TextView text;
    String phoneNum;
    String msg;
    String userMsg;
    String display;
    boolean help;
    int times;
    int target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.id_text);
        msg = "";
        times = 0;
        target = 1;
        help = false;
        display = "Waiting for response";
        text.setText(display);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS}, PERMISSIONS_REQUEST);
        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS}, PERMISSIONS_REQUEST);{
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        bReceiver = new SMSReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(bReceiver, filter);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 123){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("TAG", "PERMISSION GRANTED");
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bReceiver);
    }
    public class SMSReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] smsMessages;
            Object[] pdus = (Object[]) bundle.get("pdus");

            if(bundle != null){
                smsMessages = new SmsMessage[pdus.length];
                for(int i = 0; i < pdus.length; i++){
                    smsMessages[i] = SmsMessage.createFromPdu((byte[])pdus[i], bundle.getString("format"));

                    phoneNum = smsMessages[i].getOriginatingAddress();
                    userMsg = smsMessages[i].getMessageBody();

                    Classify classify = new Classify(times, userMsg, help);
                    classify.classify();
                    times = classify.getStage();
                    msg = classify.getMsg();

                    if(times == 1)
                        display = "Greeting state";
                    else if(times == 2)
                        display = "Problem state";
                    else if(times == 3)
                        display = "Solution state";
                    else if(times == 0)
                        display = "Finishing state";
                    text.setText(display);
                    Log.d("TEXT", "Num: " + phoneNum + ", Msg:" + msg);
                }
            }
            smsManager = SmsManager.getDefault();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    sendMsg();
                }
            };
            runnable.run();
        }
        public class Classify{
            private int stage;
            private String uMsg;
            private String message;
            private boolean other;
            public Classify(int time, String msgU, boolean h){
                stage = time;
                uMsg = msgU.toLowerCase();
                other = h;
                message = "";
            }
            public void classify(){
                if(stage == 0){
                    if(!other) {
                        if (uMsg.contains("hi") ||  uMsg.contains("ello") || uMsg.contains("ey")) {
                            message = "Hi, how are you today?";
                            stage++;
                        } else{
                            message = "Sorry, can I help you?";
                        }
                    }
                    else if(other){
                        if(uMsg.contains("help") || uMsg.contains("ye"))
                            message = "Ok, how are you doing then?";
                        else
                            message = "Ok, you know what. Since you are on this server, I am assuming that you have some emotional problem. So how are you today?";
                        other = false;
                        stage++;
                    }
                }
                else if(stage == 1){
                    if(!other) {
                        if (uMsg.contains("ok") || uMsg.contains(" k ") || uMsg.contains("not") || uMsg.contains("bad")) {
                            message = "What seems to be the problem?";
                            stage++;
                        }
                        else if(uMsg.contains("good") || uMsg.contains("great")) {
                            message = "Then, what is the reason why you came here?";
                            stage++;
                        }
                        else {
                            message = "Sorry, I do not understand what you are trying to say. Can you repeat it in a simpler manner?";
                            other = true;
                        }
                    }
                    else if(other){
                        if (uMsg.contains("good") || uMsg.contains("great") || uMsg.contains("ok") || uMsg.contains(" k ") || uMsg.contains("not") || uMsg.contains("bad")){
                            message = "Sorry, what seems to be the problem?";
                        }
                        else
                            message = "I'm going to assume that you are in denial. So what seems to be the issue?";
                        stage++;
                        other = false;
                    }
                }
                else if(stage == 2){
                    if(uMsg.contains("pain") || uMsg.contains("sorr") || uMsg.contains("terr") || uMsg.contains("bad") || uMsg.contains("hea") || uMsg.contains("not")){
                        double rand = Math.random();
                        if(rand < 0.5)
                            message = "You know what. You are unique in your own way! Be yourself and, trust me, it will all work out in life.";
                        else
                            message = "Everyone in this world is different. Therefore, you are unique in your own way! Be happy with what you have within you and everything will work out fine.";
                    }
                    else {
                        double rand = Math.random();
                        if(rand < 0.5)
                            message = "I am going to assume that the denial is kicking in. Therefore, pretend your issue ain't there and be yourself because you are unique in your own way!";
                        else
                            message = "The denial must be kicking in. Ik it's hard to let go but that's the only way to be back to normal!";
                    }
                    stage++;
                }
                else if(stage == 3){
                    if(uMsg.contains("wow") || uMsg.contains("thx") || uMsg.contains("thank") || uMsg.contains("amaz")) {
                        double rand = Math.random();
                        if(rand < 0.5)
                            message = "You are amazing! Get out and embrace the world!";
                        else
                            message = "Ofc! Manas' Therapy Services are here 24/7";
                    }
                    else {
                        double rand = Math.random();
                        if(rand < 0.5)
                            message = "You are very welcome.";
                        else
                            message = "You are amazing.";
                    }

                    stage = 0;
                    target = 1;
                    other = false;
                }
                else
                    message = " ";
            }
            public int getStage(){
                return stage;
            }
            public String getMsg(){
                return message;
            }
        }
    }
    public void sendMsg(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if(times == target && !msg.equals(" ")) {
                        target++;
                        smsManager.sendTextMessage(phoneNum, null, msg, null, null);
                        help = false;
                    }
                    else if(times != target && !msg.equals(" ")) {
                        help = true;
                        smsManager.sendTextMessage(phoneNum, null, msg, null, null);
                    }
                    else
                        times--;
                }catch(Exception e){
                    Log.d("TAG", e.getMessage());
                }
            }
        }, 4000);
    }
}
