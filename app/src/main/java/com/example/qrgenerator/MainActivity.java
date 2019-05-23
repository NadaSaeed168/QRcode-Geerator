package com.example.qrgenerator;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.text.TimeZoneFormat;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.NetworkInterface;
import android.text.format.DateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
//        implements
//        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public EditText etInput;
    public Button btnCreateQr;
    public EditText share;
    public Button btnShare;
    public Button bt;
    public ImageView imageView;
    public Bitmap bitmap;
    LinearLayout idForSaveView;
    DatabaseReference databaseReference;
    String address;
    File pic;
//    Button pickfrom;
//    TextView resultfrom;
//    Button pickto;
//    TextView resultto;
    int day, month, year, hour,minute;
    int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;
    int fromto = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseReference = FirebaseDatabase.getInstance().getReference("/qrcode");
        idForSaveView=(LinearLayout)findViewById(R.id.idForSaveView);
        etInput = findViewById(R.id.etInput);
        share = findViewById(R.id.shareqr);
        btnCreateQr = findViewById(R.id.btnCreate);
        btnShare = findViewById(R.id.btnSend);
//        pickfrom = findViewById(R.id.btnPicker);
//        resultfrom = findViewById(R.id.from);
//        pickto = findViewById(R.id.btnPickerto);
//        resultto = findViewById(R.id.to);
        imageView = findViewById(R.id.imageView);
        //get mac address
        address = getMacAddr();
//        pickfrom.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fromto = 0;
//                Calendar c = Calendar.getInstance();
//                year = c.get(Calendar.YEAR);
//                month = c.get(Calendar.MONTH);
//                day = c.get(Calendar.DAY_OF_MONTH);
//                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,MainActivity.this,
//                        year,month,day);
//                datePickerDialog.show();
//            }
//        });
//        pickto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fromto = 1;
//                Calendar c = Calendar.getInstance();
//                year = c.get(Calendar.YEAR);
//                month = c.get(Calendar.MONTH);
//                day = c.get(Calendar.DAY_OF_MONTH);
//                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,MainActivity.this,
//                        year,month,day);
//                datePickerDialog.show();
//            }
//        });
            //make QR code
            btnCreateQr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text = etInput.getText().toString().trim();
                        if (text != null) {
                            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                            addqrcode();
                            try {
                                BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,
                                        500, 500);
                                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                imageView.setImageBitmap(bitmap);
                                try {
                                    File root = Environment.getExternalStorageDirectory();
                                    if (root.canWrite()){
                                        pic = new File(root.getAbsolutePath(), "pic.png");
                                        FileOutputStream out = new FileOutputStream(pic);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                        out.flush();
                                        out.close();
                                    }
                                } catch (IOException e) {
                                    Log.e("BROKEN", "Could not write file " + e.getMessage());
                                }
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                    }
                }
            });
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("image/*");
                    i.putExtra(Intent.EXTRA_EMAIL, new String []{share.getText().toString()});
                    i.putExtra(Intent.EXTRA_SUBJECT,"On The Job");
                    i.putExtra(Intent.EXTRA_STREAM, pic);
                    startActivity(Intent.createChooser(i,"Sending email..."));
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error occured. Please try again later.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    //add QR code into database
    public void addqrcode(){
        String qrcode = etInput.getText().toString();
        if(!TextUtils.isEmpty(qrcode)){
            //String id = databaseReference.push().getKey();
            databaseReference.child("name").setValue(qrcode);
            databaseReference.child("sender").setValue(address);
        }
        else{
            Toast.makeText(MainActivity.this,"Please type text to generate QR code",Toast.LENGTH_LONG).show();

        }
    }
    //get MAC address
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif: all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b: macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {}
        return "02:00:00:00:00:00";
    }
//    @Override
//    public void onDateSet(DatePicker view, int i, int i1, int i2) {
//        yearFinal = i;
//        monthFinal = i1+1;
//        dayFinal = i2;
//        Calendar c = Calendar.getInstance();
//        hour = c.get(Calendar.HOUR_OF_DAY);
//        minute = c.get(Calendar.MINUTE);
//        TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,MainActivity.this,
//                hour, minute, DateFormat.is24HourFormat(this));
//        timePickerDialog.show();
//    }
//    @Override
//    public void onTimeSet(TimePicker view, int i, int i1) {
//        hourFinal = i;
//        minuteFinal = i1;
//        if (fromto == 0) {
//            resultfrom.setText("year: " + yearFinal + " " +
//                    "month: " + monthFinal + " " +
//                    "day: " + dayFinal + " " +
//                    "hour: " + hourFinal + " " +
//                    "minute: " + minuteFinal);
//        } else {
//            resultto.setText("year: " + yearFinal + " " +
//                    "month: " + monthFinal + " " +
//                    "day: " + dayFinal + " " +
//                    "hour: " + hourFinal + " " +
//                    "minute: " + minuteFinal);
//        }
//    }
}
