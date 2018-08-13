package sg.edu.rp.c346.smsapp;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    EditText edTo;
    EditText edContent;
    Button btnSend;
    Button btnSendApp;
    BroadcastReceiver br = new MessageReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        edTo = findViewById(R.id.editTextTo);
        edContent = findViewById(R.id.editTextContent);
        btnSend = findViewById(R.id.buttonSend);
        btnSendApp = findViewById(R.id.buttonSendApp);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmsManager smsManager = SmsManager.getDefault();
                String num = edTo.getText().toString();
                String numTo[] = num.split(", *");
                for (String number : numTo){
                    smsManager.sendTextMessage(number, null, edContent.getText().toString(), null, null);
                }
            }
        });

        btnSendApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 19) {
                    Uri smsUri = Uri.parse("smsto:" + edTo.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_SENDTO, smsUri);
                    intent.putExtra("sms_body", edContent.getText().toString());
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show(); //show error
                    }
                }
                else {
                    Uri smsUri = Uri.parse("tel:" + edTo.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
                    intent.putExtra("address", edTo.getText().toString());
                    intent.putExtra("sms_body", edContent.getText().toString());
                    intent.setType("vnd.android-dir/mms-sms");//here setType will set the previous data null.
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            }
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        this.registerReceiver(br, filter);
    }
    private void checkPermission() {
        int permissionSendSMS = ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS);
        int permissionRecvSMS = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS);
        if (permissionSendSMS != PackageManager.PERMISSION_GRANTED &&
                permissionRecvSMS != PackageManager.PERMISSION_GRANTED) {
            String[] permissionNeeded = new String[]{Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS};
            ActivityCompat.requestPermissions(this, permissionNeeded, 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(br);
    }
}
