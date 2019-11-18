package com.free.cloud;


import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DriveServiceHelper driveServiceHelper;
    String content = "";
    Button btnManualSync;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, BackgroundService.class));


        RxPermissions rxPermissions = new RxPermissions(this); // where this is an Activity instance // Must be done during an initialization phase like onCreate
        rxPermissions
                .request(Manifest.permission.READ_SMS)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        // I can control the camera now
                    } else {
                        // Oups permission denied
                    }
                });

        rxPermissions
                .request(Manifest.permission.INTERNET)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        // I can control the camera now
                    } else {
                        // Oups permission denied
                    }
                });

        rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        // I can control the camera now
                    } else {
                        // Oups permission denied
                    }
                });
//
//        rxPermissions
//                .request(Manifest.permission.GET_ACCOUNTS)
//                .subscribe(granted -> {
//                    if (granted) { // Always true pre-M
//                        // I can control the camera now
//                    } else {
//                        // Oups permission denied
//                    }
//                });




        btnManualSync = findViewById(R.id.btnManualSync);
        btnManualSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WORK_FLOW();
            }
        });


        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        try{
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

            //if you want to use your common space of G drive
            driveServiceHelper = new DriveServiceHelper(DriveServiceHelper.getGoogleDriveService(getApplicationContext(), account, "CloudSMS"));

        }catch (Exception e) {
            //signIn();
        }

        try{
            startService(new Intent(this, BackgroundService.class));
        }
        catch (Exception e)
        {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(new Intent(this, BackgroundService.class));
    }

    private void signIn() {

        GoogleSignInClient mGoogleSignInClient = buildGoogleSignInClient();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), 400);
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .requestEmail()
                        .build();
        return GoogleSignIn.getClient(getApplicationContext(), signInOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 400:

                if (resultCode == RESULT_OK) {
                    handleSignInIntent(data);
                }else
                {
                    Toast.makeText(this,"Sign in fail. Checking your connection",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }



    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        GoogleAccountCredential credential = GoogleAccountCredential
                                .usingOAuth2(MainActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));

                        credential.setSelectedAccount(googleSignInAccount.getAccount());

                        Drive googleDriveServices = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential)
                                .setApplicationName("CloudSMS")
                                .build();

                        driveServiceHelper = new DriveServiceHelper(googleDriveServices);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }



    public  void createTextFile(String content)
    {



        driveServiceHelper.createTextFile("CloudSMS",content,null)
                .addOnSuccessListener(new OnSuccessListener<GoogleDriveFileHolder>() {
                    @Override
                    public void onSuccess(GoogleDriveFileHolder googleDriveFileHolder) {
                        //progressDialog.dismiss();
                        //Toast.makeText(getApplicationContext(),"Upload successfully",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //progressDialog.dismiss();
                        // Toast.makeText(getApplicationContext(),"Check your api key",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public  void WORK_FLOW()
    {
        driveServiceHelper.searchFile("CloudSMS", "text/plain")
                .addOnSuccessListener(new OnSuccessListener<List<GoogleDriveFileHolder>>() {
                    @Override
                    public void onSuccess(List<GoogleDriveFileHolder> googleDriveFileHolders) {

                        //Toast.makeText(MainActivity.this, googleDriveFileHolders.get(0).getId(),Toast.LENGTH_SHORT).show();
                        for(GoogleDriveFileHolder g : googleDriveFileHolders)
                        {
                            deleteFile_(g.getId());
                        }



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        getSMS();
                    }
                });
    }

    public void deleteFile_(String ID)
    {
        driveServiceHelper.deleteFolderFile(ID).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getSMS();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getSMS();
            }
        });
    }

    public  void getSMS() {
        TelephonyProvider telephonyProvider = new TelephonyProvider(this);
        List<Sms> smses = telephonyProvider.getSms(TelephonyProvider.Filter.ALL).getList();

        content = "";
        for(Sms s : smses)
        {
            content += s.address +" " + ConvertMilliSecondsToFormattedDate(String.valueOf(s.receivedDate)) + " " + s.body + "\n";
            content += "***********\n";

        }
        createTextFile(content);
    }


    public static String dateFormat = "yyyy/MM/dd hh:mm";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

    public static String ConvertMilliSecondsToFormattedDate(String milliSeconds){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(milliSeconds));
        return simpleDateFormat.format(calendar.getTime());
    }
}