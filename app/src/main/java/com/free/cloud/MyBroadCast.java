package com.free.cloud;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

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

public class MyBroadCast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"[DEBUG] sync SMS",Toast.LENGTH_SHORT).show();
        this.context = context;

        try{
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

            //if you want to use your common space of G drive
            driveServiceHelper = new DriveServiceHelper(DriveServiceHelper.getGoogleDriveService(context, account, "CloudSMS"));

            WORK_FLOW();

        }catch (Exception e) {
            //signIn();
        }
    }

    public Context context = null;
    public Handler handler = null;
    public static Runnable runnable = null;
    DriveServiceHelper driveServiceHelper;
    String content = "";

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
        TelephonyProvider telephonyProvider = new TelephonyProvider(context);
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