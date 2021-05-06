package com.lambdafate.qrcode;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static List<String> ECLSelector = new ArrayList<>();
    private static List<String> VersionSelector = new ArrayList<>();
    private static String REMOTE_IP = "http://39.107.83.159:1024";
    private static String CREATE_QRCODE_URL = REMOTE_IP + "/qrcode/";

    public static Handler mainHandler;
    private static int UPDATE_QRCODE = 0x00;
    private static String QRCODE_URL = null;

    static {
        ECLSelector.add("L");
        ECLSelector.add("M");
        ECLSelector.add("Q");
        ECLSelector.add("H");

        VersionSelector.add("Auto");
        for (int i = 1; i <= 40; i++) {
            VersionSelector.add(String.valueOf(i));
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init encodeData
        EditText encodeData = (EditText) findViewById(R.id.encodeData);
        encodeData.setText("Hello, World!");

        // init ecl
        Spinner ecl = (Spinner) findViewById(R.id.ECL);
        ecl.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, ECLSelector));

        // init versions
        Spinner versions = (Spinner) findViewById(R.id.version);
        versions.setAdapter(new ArrayAdapter<String>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, VersionSelector));

        findViewById(R.id.createQRCodeButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createQRCode(v);
            }
        });

        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == UPDATE_QRCODE) {
                    Glide.with(MainActivity.this).load(MainActivity.QRCODE_URL).into((ImageView) findViewById(R.id.QRCodeImage));
                }
                return;
            }
        };

    }

    private void createQRCode(View v) {
        final String encodeData = ((EditText) findViewById(R.id.encodeData)).getText().toString();
        final Long ecl = ((Spinner) findViewById(R.id.ECL)).getSelectedItemId();
        final Long version = ((Spinner) findViewById(R.id.version)).getSelectedItemId();
        // SAY(encodeData + " " + ecl + " " + version);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String body = "{" +
                        "\"Data\": \"" + encodeData + "\"" +
                        ",\"QRVersion\": \"" + version + "\"" +
                        ",\"RSLevel\": \"" + ecl + "\"" +
                        "}";
                System.out.println("请求数据: " + body);
                Request request = new Request.Builder()
                        .url(CREATE_QRCODE_URL)
                        .post(RequestBody.create(MediaType.parse("application/json;charset=utf-8"), body))
                        .build();
                try {
                    Response response = new OkHttpClient().newCall(request).execute();
                    String jsonStr = response.body().string();
                    System.out.println("返回信息: " + jsonStr);
                    if (response != null) {
                        JSONObject json = new JSONObject(jsonStr);
                        if (json.getString("status").equals("success"))
                            MainActivity.QRCODE_URL = REMOTE_IP + json.getString("url");
                        MainActivity.mainHandler.sendEmptyMessage(UPDATE_QRCODE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void SAY(String data) {
        Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
    }
}
