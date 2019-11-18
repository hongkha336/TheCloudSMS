package com.free.cloud;

import android.app.Service;
import android.content.*;
import android.os.*;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;

public class BackgroundService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;
    DriveServiceHelper driveServiceHelper;
    String content = "";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                //Toast.makeText(context, "Service is still running", Toast.LENGTH_LONG).show();
                try{
                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

                    //if you want to use your common space of G drive
                    driveServiceHelper = new DriveServiceHelper(DriveServiceHelper.getGoogleDriveService(getApplicationContext(), account, "CloudSMS"));

                    WORK_FLOW();

                }catch (Exception e) {
                    //signIn();
                }
                handler.postDelayed(runnable, 5*60*1000);

            }
        };

        handler.postDelayed(runnable, 15000);
    }

    @Override
    public void onDestroy() {

        //Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {

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

                        getSMS();

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

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

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