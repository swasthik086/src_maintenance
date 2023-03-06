package com.suzuki.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.suzuki.R;
import com.suzuki.broadcaster.BleConnection;
import com.suzuki.pojo.EvenConnectionPojo;
import com.suzuki.pojo.RiderProfileModule;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.suzuki.activity.RiderProfileActivity.bitmap;
import static com.suzuki.activity.RiderProfileActivity.decodeBase64;
import static com.suzuki.broadcaster.BluetoothCheck.BLUETOOTH_STATE;

import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.utils.Common.BikeBleName;
//import com.suzuki.activity.IntroScreenHelpActivity;


public class HelpActivity extends AppCompatActivity implements OnClickListener {
    static boolean flag = true;

    ImageView imArrowHelp, closeBtn;
    Button submitBtn;
    ImageView ivHelpDrop1, ivGen, ivCont, ivLeg;
    Activity context;
    LinearLayout llFirstLayout, linearGeneral, llRedAlertBle, llAboutUs, llMail, llCall;
    Dialog myDialog;
    Realm realm;
    TextView riderNameFeed, riderLocationFeed, tvAppVersion;
    private BleConnection mReceiver;
    String riderName, riderLoc;
    SharedPreferences myPrefrence;
    public static final String key = "USER_IMAGE", MyPREFERENCES = "MyPreferences";
    boolean isValid;
    private String cluster_id = "NEWBT_1003_4";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        myDialog = new Dialog(this);
        context = this;

        setContentView(R.layout.help_activity);
        linearGeneral = (LinearLayout) findViewById(R.id.lblGenHeader);
        imArrowHelp = (ImageView) findViewById(R.id.imArrowHelp);
        ivGen = (ImageView) findViewById(R.id.ivGen);
        ivCont = (ImageView) findViewById(R.id.ivContact);
        ivLeg = (ImageView) findViewById(R.id.ivLegal);

//        riderNameFeed = (TextView) findViewById(R.id.tvNameFeed);
//        riderLocationFeed = (TextView) findViewById(R.id.tvRiderLocation);
        llRedAlertBle = (LinearLayout) findViewById(R.id.llRedAlertBle);
        tvAppVersion = (TextView) findViewById(R.id.tvAppVersion);
        llAboutUs = (LinearLayout) findViewById(R.id.llAboutUs);
        llMail = (LinearLayout) findViewById(R.id.llMail);
        llCall = (LinearLayout) findViewById(R.id.llCall);
        tvAppVersion.setText(R.string.build_version);
        final LinearLayout llFirstLayout = (LinearLayout) findViewById(R.id.llInLayout);

        llAboutUs.setOnClickListener(v -> startActivity(new Intent(HelpActivity.this, AboutUsActivity.class)));

        llCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + 18001217996L));
            startActivity(intent);
        });

        llMail.setOnClickListener(v -> {
            try {
                String userDetails = createUserDetailsSignature();
                sendMail("customer.queries@suzukimotorcycle.in","smiplconnected2020@gmail.com", "Suzuki Ride Connect: " + cluster_id, userDetails);
            } catch (Exception e) {
                Toast.makeText(HelpActivity.this, "Sorry...You don't have any mail app", Toast.LENGTH_SHORT).show();
            }
        });

        llRedAlertBle.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), DeviceListingScanActivity.class)));

        setBluetoothStatus();
//        viewRecord();

        SharedPreferences sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        riderName = sharedPreferences.getString("name","");
        riderLoc = sharedPreferences.getString("location","");

        linearGeneral.setOnClickListener(v -> {
            if (flag) {
                llFirstLayout.setVisibility(View.VISIBLE);
                ivGen.setImageResource(R.drawable.help_drop);
                flag = false;
            } else {
                llFirstLayout.setVisibility(View.GONE);
                ivGen.setImageResource(R.drawable.general);
                flag = true;
            }
        });

        LinearLayout linearContact = (LinearLayout) findViewById(R.id.lblContactHeader);

        final LinearLayout llSecondLayout = (LinearLayout) findViewById(R.id.llInLayout2);

        linearContact.setOnClickListener(v -> {
            if (flag) {
                llSecondLayout.setVisibility(View.VISIBLE);
                ivCont.setImageResource(R.drawable.help_drop);
                flag = false;
            } else {
                llSecondLayout.setVisibility(View.GONE);
                ivCont.setImageResource(R.drawable.general);
                flag = true;
            }
        });

        LinearLayout linearLegal = (LinearLayout) findViewById(R.id.lblLeagalHeader);

        final LinearLayout llThirdLayout = (LinearLayout) findViewById(R.id.llInLayout3);

        linearLegal.setOnClickListener(v -> {
            if (flag) {
                llThirdLayout.setVisibility(View.VISIBLE);
                ivLeg.setImageResource(R.drawable.help_drop);

                flag = false;
            } else {
                llThirdLayout.setVisibility(View.GONE);
                ivLeg.setImageResource(R.drawable.general);

                flag = true;
            }
        });

        imArrowHelp.setOnClickListener(v -> finish());

        final LinearLayout llUserGuide = (LinearLayout) findViewById(R.id.llUserGuide);

        llUserGuide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(context, UserguideActivity.class));
            }
        });

        final LinearLayout llTerms = (LinearLayout) findViewById(R.id.llTerms);

        llTerms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(context, TermsActivity.class));
            }
        });

        final LinearLayout llPrivacy = (LinearLayout) findViewById(R.id.llPrivacyPolicy);

        llPrivacy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(context, PrivacyActivity.class));
            }
        });

        final LinearLayout llFeedBack = (LinearLayout) findViewById(R.id.llFeedback);

        llFeedBack.setOnClickListener((OnClickListener) v -> {

            myDialog.setContentView(R.layout.feedback);

            myDialog.show();
            myDialog.setCanceledOnTouchOutside(false);


            TextView tvNameFeed = (TextView) myDialog.findViewById(R.id.tvNameFeed);
            TextView tvLocationFeed = (TextView) myDialog.findViewById(R.id.tvLocationFeed);
            ImageView ivUserImage = (ImageView) myDialog.findViewById(R.id.ivUserImage);
            EditText etEditEmail = myDialog.findViewById(R.id.etEditEmail);
            EditText etFeedback = myDialog.findViewById(R.id.etFeedback);
            closeBtn = (ImageView) myDialog.findViewById(R.id.ivFeedClose);
            submitBtn = (Button) myDialog.findViewById(R.id.btnSubmit);

            tvNameFeed.setText(riderName);
            tvLocationFeed.setText(riderLoc);

            myPrefrence = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

            String photo = myPrefrence.getString(key, "photo");

            if (!photo.equals("photo")) {
                bitmap = decodeBase64(photo);
                ivUserImage.setImageBitmap(bitmap);
            }

            closeBtn.setOnClickListener(v12 -> myDialog.dismiss());

            submitBtn.setOnClickListener(v1 -> {

                if (etEditEmail.getText().toString().contentEquals("")) {
                    etEditEmail.setError("Please enter Mobile number");
                   etEditEmail.requestFocus();
                   // Toast.makeText(myDialog.getContext(), "", Toast.LENGTH_SHORT).show();

                } else if (validateMobile(etEditEmail.getText().toString())) {

                    Log.d("et--s", "--" + etFeedback.getText().toString());

                   if (etFeedback.getText().toString().isEmpty() || etFeedback.getText().toString().contentEquals(" ")) {
                       etFeedback.setError("Please enter feedback");
                       etFeedback.requestFocus();
                       // Toast.makeText(myDialog.getContext(), "Please enter feedback", Toast.LENGTH_SHORT).show();

                    } else {
                        String formattedBody = formatBodyForMail(etEditEmail.getText().toString(), etFeedback.getText().toString());
                        String subject = "Suzuki Ride Connect - Feedback: " + cluster_id;

                        myDialog.dismiss();

                        try {
                            sendMail("customer.queries@suzukimotorcycle.in","smiplconnected2020@gmail.com", subject, formattedBody);
                        } catch (Exception e) {
                            Toast.makeText(HelpActivity.this, "Sorry...You don't have any mail app", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                } else {
                    etEditEmail.setError("Invalid MNo");
                    etEditEmail.requestFocus();
                    Toast.makeText(myDialog.getContext(), "Please enter valid Mobile Number", Toast.LENGTH_SHORT).show();
                }

                
            });
        });
        cluster_id = BikeBleName.getValue();
    }

    private String createUserDetailsSignature() {

        if (cluster_id.isEmpty()){
            SharedPreferences sharedPreferences = this.getSharedPreferences("BLE_DEVICE", MODE_PRIVATE);
            cluster_id = sharedPreferences.getString("prev_cluster","Never Connected to a Vehicle");
        }

        return "User Details:"+"\n" +
                "Cluster Id: " + cluster_id + "\n" +
                "Rider Name: " + riderName + "\n" +
                "Location: " + riderLoc;
    }

    private String formatBodyForMail(String mobileNo, String feedback) {

        return "Dear Sir/Ma'am," + "\n\n" +
                "Feedback:" +"\n"+
                feedback + "\n\n" +
                "User Details:" +"\n"+
                "Cluster Id: " + cluster_id + "\n" +
                "Rider Name: " + riderName + "\n" +
                "Mobile Number: " + mobileNo + "\n" +
                "Location: " + riderLoc;
    }

    private void sendMail(String mailTo,String cc, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
      //  intent.setData(Uri.parse("cc:"));// only email apps should handle this
        intent.putExtra(Intent.EXTRA_CC, new String[]{cc});
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailTo});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);

        startActivity(intent);
        /*if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
        else Toast.makeText(myDialog.getContext(), "Can't send mail", Toast.LENGTH_SHORT).show();*/
    }

    public boolean validateMobile(String mobileNo) {
        boolean isValidated = false;

        Pattern pattern = Pattern.compile("[^0-9]");
        Matcher matchers = pattern.matcher(mobileNo);
        boolean hasSpecialChars = matchers.find();

        if (hasSpecialChars) isValidated = false;
        else if (mobileNo.length() == 10) isValidated = true;

        return isValidated;
    }

    /*public boolean validateData(String email) {
//
//        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
//                "[a-zA-Z0-9_+&*-]+)*@" +
//                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
//                "A-Z]{2,7}$";
        String emailRegex = "\\+[0-9]{10,13}$]";
        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();

//        return isValid;
    }*/

    public void viewRecord() {
        RealmResults<RiderProfileModule> results = realm.where(RiderProfileModule.class).limit(1).findAll();
        for (RiderProfileModule riderProfileModuleofile : results) {
            riderName = riderProfileModuleofile.getName();
            riderLoc = riderProfileModuleofile.getLocation();
        }
    }

    /*public void postData(String name, String loc, String email, String feed) {
        MediaType MEDIA_TYPE =
                MediaType.parse("application/json");
        final OkHttpClient client = new OkHttpClient();
        JSONObject postdata = new JSONObject();
        try {
            postdata.put("name", name);
            postdata.put("email", email);
            postdata.put("location", loc);
            postdata.put("comment", feed);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE,
                postdata.toString());

        final Request request = new Request.Builder()
                .url("http://3.225.117.165:3010/api/v1/create_comment")
                .post(body)
                .addHeader("Content-Type", "application/json")
//                .addHeader("Authorization", "Your Token")
//                .addHeader("cache-control", "no-cache")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {

                String mMessage = response.body().string();
                Log.d("resss", "--" + response);
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(mMessage);

                        final String serverResponse = json.getString("status");
                        Log.d("ressssqs", "--" + serverResponse);


                        updateToast(serverResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }*/

    /*private void updateToast(String res) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (res.contentEquals("200")) {
                    Toast.makeText(HelpActivity.this, "FeedBack Submitted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HelpActivity.this, "Something wrong, Please try again.", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }*/

    @Override
    public void onClick(View v) {
        if (linearGeneral.getId() == v.getId()) {
            if (flag) {
                llFirstLayout.setVisibility(View.VISIBLE);
                flag = false;
            } else {
                llFirstLayout.setVisibility(View.GONE);
                flag = true;
            }
        } else if (ivGen.getId() == v.getId()) {
            if (flag) {
                ivGen.setImageResource(R.drawable.help_drop);
                flag = false;

            } else {
                ivGen.setImageResource(R.drawable.general);
                flag = true;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionEvent(EvenConnectionPojo event) {

        if (!BLUETOOTH_STATE) {

            staticConnectionStatus = false;

            Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
            sendBroadcast(i);
            if (staticConnectionStatus) llRedAlertBle.setVisibility(View.GONE);
            else llRedAlertBle.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void setBluetoothStatus() {

        if (BLUETOOTH_STATE) {
            if (staticConnectionStatus) llRedAlertBle.setVisibility(View.GONE);
            else llRedAlertBle.setVisibility(View.VISIBLE);

        } else llRedAlertBle.setVisibility(View.VISIBLE);

        IntentFilter intentFilter = new IntentFilter("status");

        mReceiver = new BleConnection() {

            @Override
            public void onReceive(Context context, Intent intent) {

                Boolean status = intent.getExtras().getBoolean("status");
                if (BLUETOOTH_STATE) {
                    if (status) llRedAlertBle.setVisibility(View.GONE);
                    else llRedAlertBle.setVisibility(View.VISIBLE);

                } else llRedAlertBle.setVisibility(View.VISIBLE);
            }
        };
        this.registerReceiver(mReceiver, intentFilter);
    }
}
