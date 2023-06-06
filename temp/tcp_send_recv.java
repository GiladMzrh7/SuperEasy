<package com.example.yossi.setevent;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.yossi.setevent.SocketHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class tcp_send_recv extends AsyncTask<String,Handler,Void> {
    PrintWriter pw;
    String IP;
    final static int LEN_SIZE = 4;

    public tcp_send_recv(Handler mHandler,String IP) {
        if (IP != "")
            this.IP = IP;
        Socket sk = SocketHandler.getSocket();
        Handler h = SocketHandler.getHreceiver();
        if (h != mHandler)        {
            SocketHandler.setHreceiver(mHandler);
        }

    }


    @Override
    protected Void doInBackground(String... voids) {
        String TAG = "doInBackground ";
        Socket sk = SocketHandler.getSocket();

        if (sk == null) {

            try {
                Log.d(TAG, "Before Connect");
                sk = new Socket(IP, 11221);
                SocketHandler.setSocket(sk);

                Log.d(TAG, "connected");
                Thread listener = new Thread(new Listener(sk));
                listener.start();


            } catch (UnknownHostException e) {
                Log.e(TAG, "ERROR UnknownHostException socket");

            } catch (IOException e) {
                Log.e(TAG, "ERROR IOException socket " +e.toString());
            }
        }

        String data = voids[0];
        data = String.format("%04d", data.length()) + "|" + data;
        Log.d(TAG, "Before Send 1");
        try {

            pw = new PrintWriter(sk.getOutputStream());
            Log.d(TAG, "Before Send 2");
            pw.write(data);

            pw.flush();
            Log.d(TAG, "After Send:" + data);
            //pw.close();

        } catch (IOException e) {
            Log.e(TAG, "ERROR write " + e.getMessage());

            Handler mHandler = SocketHandler.getHreceiver();
            Message msg = mHandler.obtainMessage();
            msg.obj = "Socket Error";
            mHandler.sendMessage(msg);



        }
        return null;
    }


    class Listener implements Runnable {
        Socket skl;
        BufferedReader in;
        char[] cbuf;
        String TAG = "Listener";

        public Listener(Socket sk) {
            this.skl = sk;


            try {
                this.in = new BufferedReader(new InputStreamReader(this.skl.getInputStream()));
            } catch (IOException e) {
                Log.e(TAG, "ERROR buffer read " + e.getMessage());
                e.printStackTrace();
            }

            cbuf = new char[2000];
        }

        @Override
        public void run() {

            boolean ok = true;

            while (ok) {
                try {

                    if (in.ready()) {
                        int len_read =0;
                        char[] cbuflen = new char[LEN_SIZE];

                        while (len_read < LEN_SIZE){
                                len_read += in.read(cbuflen, 0, LEN_SIZE-len_read);
                        }
                        String received_len = new String(cbuflen, 0, LEN_SIZE);
                        int total_to_read = Integer.parseInt(received_len);

                        int len = in.read(cbuf, 0, total_to_read +1);
                        String received = received_len + new String(cbuf, 0, len) ;
                        len += LEN_SIZE;
                        if (len > 0) {
                            Log.d(TAG, " **** got data :" + received );

                            Handler mHandler = SocketHandler.getHreceiver();
                            if (mHandler == null)
                            {
                                try{
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mHandler = SocketHandler.getHreceiver();
                            }
                            if (mHandler == null)
                            {
                                try{
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mHandler = SocketHandler.getHreceiver();
                            }

                            if (mHandler != null){
                                Message msg = mHandler.obtainMessage();
                                msg.obj = received;
                                mHandler.sendMessage(msg);
                            }
                            else{
                                Log.e(TAG, "Handle = Null skipping msg=" + received );
                            }

                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "ERROR read line- " + e.getMessage() + ".");
                    e.printStackTrace();
                    ok = false;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "ERROR InterruptedException " + e.getMessage());
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "Login Listener finished ");
        }

    }
}
