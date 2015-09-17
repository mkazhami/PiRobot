package com.remotecontrol;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;



public class Controls extends ActionBarActivity {

    public final String server = "192.168.99.1";
    public final int port = 3030;

    public ConcurrentLinkedQueue<String> q;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        q = new ConcurrentLinkedQueue<String>();

        new SendData().execute();

        Button upButton = (Button) findViewById(R.id.upButton);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("REMOTE CONTROL", "adding 1 to queue");
                q.add("1");
                //new SendData().execute("1");
            }
        });
        Button downButton = (Button) findViewById(R.id.downButton);
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("REMOTE CONTROL", "adding 2 to queue");
                q.add("2");
                //new SendData().execute("2");
            }
        });
        Button leftButton  = (Button) findViewById(R.id.leftButton);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("REMOTE CONTROL", "adding 3 to queue");
                q.add("3");
                //new SendData().execute("3");
            }
        });
        Button rightButton  = (Button) findViewById(R.id.rightButton);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("REMOTE CONTROL", "adding 4 to queue");
                q.add("4");
                //new SendData().execute("4");
            }
        });
        Button stopButton  = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("REMOTE CONTROL", "adding 5 to queue");
                q.add("5");
                //new SendData().execute("5");
            }
        });
        Button closeButton  = (Button) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("REMOTE CONTROL", "adding q to queue");
                q.add("q");
                finish();
            }
        });
        Button restartButton  = (Button) findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("REMOTE CONTROL", "adding r to queue");
                q.add("r");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_controls, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class SendData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                InetAddress serverAddr = InetAddress.getByName(server);
                Log.d("REMOTE CONTROL", "CONNECTING TO PI");
                Socket socket = new Socket(serverAddr, port);
                socket.setTcpNoDelay(true);
                OutputStream out = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(out);
                Log.d("REMOTE CONTROL", "ENTERING LOOP");

                while(true) {
                    if(!q.isEmpty()) {
                        String s = q.poll();
                        Log.d("REMOTE CONTROL", "Getting " + s + " from queue");
                        if(s.equals("q")) {
                            pw.println(s);
                            pw.flush();
                            out.flush();
                            Thread.sleep(100);
                            pw.close();
                            out.close();
                            socket.close();
                            Log.d("REMOTE CONTROL", "CLOSING SOCKET");
                            return "success";
                        }
                        else if(s.equals("r")) {
                            pw.close();
                            out.close();
                            socket.close();
                            Thread.sleep(50); // just in case
                            socket = new Socket(serverAddr, port);
                            socket.setTcpNoDelay(true);
                            out = socket.getOutputStream();
                            pw = new PrintWriter(out);
                        }
                        else {
                            Log.d("ROMOTE CONTROL", "SENDING " + s + " TO SERVER");
                            pw.println(s);
                            pw.flush();
                            out.flush();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("REMOTE CONTROL", "FAILED TO CONNECT");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "failed";
        }
    }
}
