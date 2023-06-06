private static final boolean DEBUG = false;

public TCP(Handler handler, String ipAddress) {
    this.mHandler = handler;
    if (!this.IP.equals("")) {
        this.IP = ipAddress;
    }
    Socket sock = SocketHandler.getSocket();
    Handler h = SocketHandler.getHreciever();
    if (h != handler) {
        SocketHandler.setHreciever(handler);
    }
}

@Override
protected Void doInBackground(String... params) {
    final String TAG = "doInBackground";
    Socket sk = SocketHandler.getSocket();

    if (params.length == 0) {
        Log.e(TAG, "no params");
        return null;
    }

    if (sk == null) {
        try {
            Log.d(TAG, "before Connect");

            sk = new Socket(IP, 27391);
            SocketHandler.setSocket(sk);
            Log.d(TAG, "connected successfully");
            Thread listener = new Thread(new Listener(sk));
            listener.start();
        } catch (UnknownHostException e) {
            Log.e(TAG, "ERROR UnknownHostException");
            return null;
        } catch (IOException e) {
            Log.e(TAG, "ERROR ioException" + e.getMessage());
            return null;
        }
    }

    String data = params[0];
    if (data != null) {
        data = String.format("%04d", data.length()) + "|" + data;
    }
    Log.d(TAG, "Before send 1");
    try {
        this.pw = new PrintWriter(sk.getOutputStream());
        Log.d(TAG, "Before send 2");
        pw.write(data);
        pw.flush();
        Log.d(TAG, "after send:" + data);
    } catch (IOException e) {
        Log.e(TAG, "ERROR write" + e.getMessage());

        Handler mHandler = SocketHandler.getHreciever();

        if (mHandler == null) {
            Log.e(TAG, "mHandler is null");
            return null;
        }

        android.os.Message msg = mHandler.obtainMessage();
        msg.obj = "SocketError";
        mHandler.sendMessage(msg);

    }
    return null;
}
public class Listener implements Runnable {
    private Socket skl;
    private InputStream input;
    private byte[] cbuf;
    private static final String TAG = "Listener";
    private static final boolean DEBUG = true;

    public Listener(Socket sock) {
        try {
            this.skl = sock;
            this.input = sock.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "ERROR buffer read" + e.getMessage());
        }
        this.cbuf = new byte[1024];
    }

    @Override
    public void run() {
        boolean ok = true;
        while (ok) {
            try {
                if (DEBUG) {
                    Log.d(TAG, "starting recieve");
                }

                if (input.available() > 0) {
                    int len_read = 0;
                    byte[] cbuflen = new byte[4];

                    while (len_read < 4) {
                        len_read += input.read(cbuflen, len_read, 4 - len_read);
                    }

                    if (DEBUG) {
                        Log.d("LOGGEDDATA", new String(cbuflen, 0, len_read));
                    }

                    String received_len = new String(cbuflen, 0, len_read);
                    int final_len = Integer.parseInt(received_len);

                    Log.d(TAG, "received: " + received_len);

                    int len = input.read(cbuf, 0, final_len + 1);
                    String all_msg = received_len + new String(cbuf, 0, len);

                    if (DEBUG) {
                        Log.d(TAG, "len " + len);
                    }

                    if (len > 0) {
                        Log.d(TAG, "~~~~~GOT DATA ~~~~~~" + all_msg);

                        Handler mHandler = SocketHandler.getHreciever();
                        if (mHandler == null) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (mHandler == null) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (mHandler != null) {
                            Message msg = mHandler.obtainMessage();
                            msg.obj = all_msg;
                            mHandler.sendMessage(msg);
                        }

                    } else {
                        Log.e(TAG, "Handle was null SKIPPED:" + all_msg);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "ERROR " + e.getMessage());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "ERROR InterruptedException " + e.getMessage());
                e.printStackTrace();
            }
            Log.d(TAG, "no new messages");
        }
    }
}