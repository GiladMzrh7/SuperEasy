package com.example.yossi.setevent;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;


public class MainActivity extends AppCompatActivity {
    public static String TAG;

    private EditText readerTxt;
    private EditText IPTxt;
    private EditText userTxt;
    private EditText passTxt;

    int cnt = 0;
    Handler mHandler;
    String login_username = "";
    PrintWriter pw;

    boolean ok = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TAG = this.getLocalClassName();
        readerTxt = (EditText) findViewById(R.id.readerText);
        IPTxt = (EditText) findViewById(R.id.IpText);
        userTxt = (EditText) findViewById(R.id.userText);
        passTxt = (EditText) findViewById(R.id.passText);
        com.example.yossi.setevent.SessionData.setUsername("");

        Log.d(TAG, "Start");
        String text = "VER 2.00";
        Toast.makeText(getApplication(), text,
                Toast.LENGTH_LONG).show();

        Button closeBtn = (Button) findViewById(R.id.closeBtn);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    SocketHandler.getSocket().close();
                }catch ( IOException e) {
                    e.printStackTrace();
                }


                finish();
                System.exit(0);


            }
        });

        Button loginBtn = (Button) findViewById(R.id.sendBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SessionData.setUsername("");
                send_login(v);
            }
        });

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                String reply = (String) message.obj;
                String rep_command = "";

                if (reply.length() >= 8)
                    rep_command = reply.substring(5, 8);
                Log.d("!!!", "rep_command = " + rep_command);
                readerTxt.append(reply.substring(5) + "\n");
                if (rep_command.equals("LGS")) {
                    String[] parts = reply.split("~");
                    login_username = parts[1] + ", " + parts[2];
                    com.example.yossi.setevent.SessionData.setUsername(parts[1]);
                    Intent evOp = new Intent(MainActivity.this, EventOptions.class);
                    evOp.putExtra("EXTRA_username", login_username);
                    startActivity(evOp);
                } else {
                    readerTxt.append(reply.substring(5)+ "\n");
                }
            }
        };
    }

    public void send_login(View v) {
        Socket sk = SocketHandler.getSocket();

        if (sk != null) {
                try {

                    sk.close();
                    SocketHandler.setSocket(null);

                } catch (IOException e) {
                    Log.e(TAG, "ERROR IOException clse socket");
                }
        }
        if (userTxt.getText().toString() == "" ||
             IPTxt.getText().toString() == "" ||
                passTxt.getText().toString() == "") {
            readerTxt.setText("You must fill login information");
            return;
        }
        String data = "LOG~" + userTxt.getText().toString() + "~";

        data = data + sha256(passTxt.getText().toString());

        tcp_send_recv bg = new tcp_send_recv(mHandler,IPTxt.getText().toString());
        bg.execute(data);
    }



    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }



}
