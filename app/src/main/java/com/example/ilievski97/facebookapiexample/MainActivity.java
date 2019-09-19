package com.example.ilievski97.facebookapiexample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class
MainActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    TextView txtEmail,txtBirthday,txtFriends,txtName,txtGender;
    ProgressDialog mDialog;
    ImageView imgAvatar;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        callbackManager = CallbackManager.Factory.create();

        txtName = findViewById(R.id.name);
        txtEmail = findViewById(R.id.txtEmail);
        txtBirthday = findViewById(R.id.txtBirthday);
        txtFriends = findViewById(R.id.txtFriends);
        txtGender = findViewById(R.id.txtGender);


        imgAvatar = (ImageView) findViewById(R.id.avatar);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile","email","user_gender","user_birthday","user_friends"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setMessage("Retrieving data...");
                mDialog.show();


                String accesstoken = loginResult.getAccessToken().getToken();
                Log.d("fbaccesstoken", accesstoken);


                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        mDialog.dismiss();

                        Log.d("response", response.toString());

                        getData(object);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields","id,first_name,email,birthday,friends,gender");

                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        if(AccessToken.getCurrentAccessToken()!=null){
            txtEmail.setText(AccessToken.getCurrentAccessToken().getUserId());

            Log.d("fbtestthis",String.valueOf(AccessToken.getCurrentAccessToken().getToken()));

        }

        printKeyHash();
    }

    private void getData(JSONObject object) {
        try{
            URL profile_picture = new URL("https://graph.facebook.com/"+object.getString("id")+"/picture?width=250&height=250");

            Picasso.get().load(profile_picture.toString()).into(imgAvatar);

            txtName.setText(object.getString("first_name"));
            txtEmail.setText(object.getString("email"));
            txtBirthday.setText(object.getString("birthday"));
            txtFriends.setText("Friends "+object.getJSONObject("friends").getJSONObject("summary").getString("total_count"));
            txtGender.setText(object.getString("gender"));


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
// 2126547080745375

    private void printKeyHash(){
        try{
            PackageInfo info = getPackageManager().getPackageInfo("com.example.nikolastojkoskiloc.facebookapiexample",PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures){
                MessageDigest nd = MessageDigest.getInstance("SHA");
                nd.update(signature.toByteArray());
                Log.d("KeyHash",Base64.encodeToString(nd.digest(),Base64.DEFAULT));
            }
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
    }
}
