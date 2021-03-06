package com.lmntrx.lefo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public class FollowCode extends Activity {

    //Parse Class Name
    public static final String PARSE_CLASS = "LeFo_DB";

    //Parse Keys
    public static final String KEY_QRCODE = "QR_CODE";
    public static final String KEY_LOCATION = "LOCATION";

    //Parse ObjectID
    public static String objectId = null;

    //Location
    public static Location leaderLoc = new Location("");
    Location followerLoc = new Location("");

    String code;
    Context followCodeCon;
    EditText codeTXT;
    Button startPursuit;
    ProgressBar mProgressBar;

    Activity followCodeActivity=this;

    boolean doubleBackToExitPressedOnce;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_code);
        MainActivity.mProgressBar.setVisibility(View.INVISIBLE);
        codeTXT = (EditText) findViewById(R.id.insertCode);
        followCodeCon = this.getApplicationContext();
        startPursuit = (Button) findViewById(R.id.connectBTN);
        mProgressBar = (ProgressBar) findViewById(R.id.mProgressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    //Fetching leader location and follower location and then taking it to navigation activity
    public void connect(View v) {

        //Getting entered code
        final String qrCode = codeTXT.getText().toString().trim();
        if (!qrCode.isEmpty()) {
            mProgressBar.setVisibility(View.VISIBLE);
            final int code = Integer.parseInt(qrCode);
            ParseQuery<ParseObject> queryID = ParseQuery.getQuery(PARSE_CLASS);
            queryID.whereEqualTo(KEY_QRCODE, code);
            queryID.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null) {
                        if (parseObjects.isEmpty()) {
                            Toast.makeText(followCodeCon, "Verification Failed. Please ensure that the code entered is valid", Toast.LENGTH_LONG).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        } else {
                            for (ParseObject result : parseObjects) {
                                // Retrieving objectId
                                objectId = result.getObjectId();
                            }
                            Intent mapsIntent=new Intent(followCodeCon,MapsActivity.class);
                            mapsIntent.putExtra("CODE",qrCode);
                            mapsIntent.putExtra("OBJECT_ID",objectId);
                            startActivity(mapsIntent);
                            mProgressBar.setVisibility(View.INVISIBLE);
                            followCodeActivity.finish();
                        }
                    } else {
                        //Incase of an unknown error
                        Toast.makeText(followCodeCon, "Verification Failed. Please ensure that the code entered is valid", Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });
        } else {
            Toast.makeText(followCodeCon, "Enter shared code to proceed", Toast.LENGTH_LONG).show();
        }

    }

    //Intent-To Be changed later with native qrCode reader
    public void qrCode(View v) {
        try {

            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            intent.setPackage("com.google.android.apps.unveil");
            startActivityForResult(intent,0);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.android.apps.unveil");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                try {
                    code = data.getStringExtra("SCAN_RESULT");
                    int codeInt=Integer.parseInt(code);
                    codeTXT.setText(codeInt+"");
                    startPursuit.callOnClick();
                }catch (NumberFormatException e){
                    //If any other QRCode is scanned
                    Log.i("", code + "is not a number");
                    Toast.makeText(followCodeCon,"Incorrect Format. Scan only LeFo QRCodes!", Toast.LENGTH_LONG).show();
                }
            }
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(followCodeCon,"Scan Canceled", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            followCodeActivity.finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please tap BACK again to go back", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

}
