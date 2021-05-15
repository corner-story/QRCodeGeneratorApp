package com.lambdafate.qrcode.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.lambdafate.qrcode.MainActivity;
import com.lambdafate.qrcode.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {
    private static List<String> ECLSelector = new ArrayList<>();
    private static List<String> VersionSelector = new ArrayList<>();
    private static List<String> PixelSelector = new ArrayList<>();
    private static List<String> BorderSelector = new ArrayList<>();

    public static String REMOTE_IP = "http://39.107.83.159:1024";
    public static String CREATE_QRCODE_URL = REMOTE_IP + "/qrcode/";

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

        for (int i = 10; i < 16; i++) {
            PixelSelector.add(String.valueOf(i));
        }

        for (int i = 0; i <= 30; i++) {
            BorderSelector.add(String.valueOf(i));
        }
    }


    private HomeViewModel homeViewModel;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        // final TextView textView = root.findViewById(R.id.text_home);

        // init encodeData
        EditText encodeData = (EditText) root.findViewById(R.id.encodeData);
        encodeData.setText("Hello, World!");

        // init ecl
        Spinner ecl = (Spinner) root.findViewById(R.id.ECL);
        ecl.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, ECLSelector));

        // init versions
        Spinner versions = (Spinner) root.findViewById(R.id.version);
        versions.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, VersionSelector));

        // init finder color
        ((EditText)root.findViewById(R.id.FinderColor)).setText("#000000");
        // init data color
        ((EditText)root.findViewById(R.id.DataColor)).setText("#000000");

        // init Pixel selector
        ((Spinner) root.findViewById(R.id.PixelSelector)).setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, PixelSelector));
        // init Border selector
        ((Spinner) root.findViewById(R.id.BorderSelector)).setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, BorderSelector));


        root.findViewById(R.id.createQRCodeButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clickCreateQRCodeButton(v);
            }
        });

        root.findViewById(R.id.saveQRCodeButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clickSaveQRCodeButton(v);
            }
        });

        mainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == UPDATE_QRCODE) {
                    Glide.with(getActivity()).load(QRCODE_URL).into((ImageView) root.findViewById(R.id.QRCodeImage));
                }
                return;
            }
        };

        // 初始化二维码图片
        // clickCreateQRCodeButton(null);
        return root;
    }

    private void clickSaveQRCodeButton(View v){
        ImageView imageView = (ImageView) root.findViewById(R.id.QRCodeImage);
        //将ImageView中的图片转换成Bitmap
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        //将Bitmap 转换成二进制，写入本地
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/QRCode");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, "QRCode_" + System.currentTimeMillis()  + ".png");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArray, 0, byteArray.length);
            fos.flush();
            //用广播通知相册进行更新相册
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            getContext().sendBroadcast(intent);
            SAY("保存二维码成功: " + file.getName());
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void clickCreateQRCodeButton(View v) {
        final String encodeData = ((EditText) root.findViewById(R.id.encodeData)).getText().toString();
        final Long ecl = ((Spinner) root.findViewById(R.id.ECL)).getSelectedItemId();
        final Long version = ((Spinner) root.findViewById(R.id.version)).getSelectedItemId();
        final String FinderColor = ((EditText)root.findViewById(R.id.FinderColor)).getText().toString();
        final String DataColor = ((EditText)root.findViewById(R.id.DataColor)).getText().toString();
        final String PixelSize = PixelSelector.get(((Long)((Spinner) root.findViewById(R.id.PixelSelector)).getSelectedItemId()).intValue());
        final String BorderSize = BorderSelector.get(((Long)((Spinner) root.findViewById(R.id.BorderSelector)).getSelectedItemId()).intValue());

        SAY(encodeData + " " + ecl + " " + version);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String body = "{" +
                        "\"Data\": \"" + encodeData + "\"" +
                        ",\"QRVersion\": \"" + version + "\"" +
                        ",\"RSLevel\": \"" + ecl + "\"" +
                        ",\"FinderColor\": \"" + FinderColor + "\"" +
                        ",\"DataColor\": \"" + DataColor + "\"" +
                        ",\"pixelSize\": \"" + PixelSize + "\"" +
                        ",\"borderSize\": \"" + BorderSize + "\"" +
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
                            QRCODE_URL = REMOTE_IP + json.getString("url");
                        mainHandler.sendEmptyMessage(UPDATE_QRCODE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void SAY(String data) {
        Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();
    }
}
