package com.lambdafate.qrcode.ui.dashboard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.lambdafate.qrcode.QrCodeUtil;
import com.lambdafate.qrcode.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;


public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        root.findViewById(R.id.UploadLogoButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClickUploadButton(v, 2);
            }
        });

        root.findViewById(R.id.CreateAndSaveButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ClickCreateAndSaveButton(v);
            }
        });

        ((EditText)root.findViewById(R.id.InputData)).setText("河北工业大学!");

        //createQRCode();
        return root;
    }

    private void ClickUploadButton(View view, int code) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        this.startActivityForResult(intent, code);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Uri uri = data.getData();//图片url
            System.out.println("上传图片URL: " + uri + "  code: " + requestCode);
            ((ImageView) getActivity().findViewById(R.id.QRCodeImage)).setImageURI(uri);
        } else if (requestCode == 2) {
            Uri uri = data.getData();//图片url
            System.out.println("上传图片URL: " + uri + "  code: " + requestCode);
            ((ImageView) getActivity().findViewById(R.id.LogoImage)).setImageURI(uri);
        }
    }


    private void ClickCreateAndSaveButton(View v) {
        try {
            System.out.println("创建并保存带logo的二维码.....");
            Bitmap logo = getBitMap(R.id.LogoImage);
            String content = ((EditText)getActivity().findViewById(R.id.InputData)).getText().toString();
            System.out.println("QRCode data: " + content);
            Bitmap bitmap = QrCodeUtil.createQRCodeWithLogo(content, 500, logo);
            ((ImageView)getActivity().findViewById(R.id.QRCodeLogoImage)).setImageBitmap(bitmap);
            SaveQRCode(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createQRCode(){
        try {
            System.out.println("创建并保存带logo的二维码.....");
            Bitmap logo = getBitMap(R.id.LogoImage);
            String content = ((EditText)getActivity().findViewById(R.id.InputData)).getText().toString();
            System.out.println("QRCode data: " + content);
            Bitmap bitmap = QrCodeUtil.createQRCodeWithLogo(content, 500, logo);
            ((ImageView)getActivity().findViewById(R.id.QRCodeLogoImage)).setImageBitmap(bitmap);
//            SaveQRCode(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitMap(int id) {
        ImageView imageView = (ImageView) getActivity().findViewById(id);
        //Bitmap image = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        imageView.buildDrawingCache();
        Bitmap bmap = imageView.getDrawingCache();
        return bmap;
    }

    private Bitmap getBitMapFromURI(Uri uri){
        Bitmap bitmap = null;
        try{
            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }

    private String DecodeQRCode(Bitmap bitmap){
        try
        {
            if (bitmap == null)
            {
                Log.e(String.valueOf(getContext()), "uri is not a bitmap,");
                return null;
            }
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
//            bitmap.recycle();
            bitmap = null;
            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
            BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultiFormatReader reader = new MultiFormatReader();
            try
            {
                Result result = reader.decode(bBitmap);
                return result.getText();
            }
            catch (Exception e)
            {
                Log.e(String.valueOf(getActivity()), "decode exception", e);
                return null;
            }
        }
        catch (Exception e)
        {
            Log.e(String.valueOf(getActivity()), "can not open file", e);
            return null;
        }
    }


    private void SaveQRCode(Bitmap bitmap){
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

    private void SAY(String data) {
        Toast.makeText(getContext(), data, Toast.LENGTH_SHORT).show();
    }

}