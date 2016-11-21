package yuber.yuberClienteServicios.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.json.JSONObject;

import java.util.List;

import yuber.yuberClienteServicios.R;

public class Intro extends AppCompatActivity {

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String TokenKey = "tokenKey";
    SharedPreferences sharedpreferences;

    private String Ip = "";

    private String Puerto = "8080";

    private static final String TAG = "INTRO";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        Ip = getResources().getString(R.string.IP);

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
            }
        }
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "SESIIIIIIIIIIIION con token :" + token);
        //Guardo el token en session
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(TokenKey, token);
        editor.commit();
        Log.d(TAG, "TengoSession con token :" + token);
        //Combruebo si ya tengo session.
        TengoSession(token);


    }

    public void saltearLogin(View view){
        //under button properties
        //android:onClick="loginUser"
        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //QUE ES ESTO? jaja
        startActivity(homeIntent);
    }


    public void TengoSession(String token){


        Log.d(TAG, "TengoSession con token :" + token);

        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/ValidarSesion/" + token;
        JSONObject obj = new JSONObject();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestHandle Rq = client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (response.contains("ERROR")){
                    cambiarALogin();
                }else{
                    SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(EmailKey, response);
                    editor.commit();
                    //Veo si esta trabajando o no
                    cambiarAMain();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cambiarAMain(){
        Log.d(TAG, "cambiar a main ... XXXXX" );

        Intent homeIntent = new Intent(getApplicationContext(), ServiciosActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void cambiarALogin(){
        Intent homeIntent = new Intent(getApplicationContext(), LoginActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

}
