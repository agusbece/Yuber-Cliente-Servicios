package yuber.yuberClienteServicios.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import yuber.yuberClienteServicios.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private EditText mEmailView;
    private EditText mPasswordView;
    private String Ip = "";
    private String Puerto = "8080";
    ProgressDialog prgDialogCargando;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String TokenKey = "tokenKey";
    public static final String ServiciosKey = "ServiciosKey";
    SharedPreferences sharedpreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Ip = getResources().getString(R.string.IP);;

        mEmailView = (EditText) findViewById(R.id.input_email);
        mPasswordView = (EditText) findViewById(R.id.input_password);
        Button loginButton = (Button) findViewById(R.id.btn_login);

        TextView signUpLink = (TextView) findViewById(R.id.link_signup);
        assert signUpLink != null;
        signUpLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        prgDialogCargando = new ProgressDialog(this);
        prgDialogCargando.setMessage("Please wait");
        prgDialogCargando.setCancelable(false);

        //BOTON LOGIN
        Button login = (Button) findViewById(R.id.btn_login);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EventoLogin(v);
            }
        });

        obtenerServiciosDisponibles();
    }// finaliza END onCreate()

    public void EventoLogin(View view){
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String token = sharedpreferences.getString(TokenKey, "");
        if(validate()){
            //prgDialogCargando.show();
            String email = mEmailView.getText().toString();
            String password = mPasswordView.getText().toString();

            sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(EmailKey, email);
            editor.commit();
            /*****************  Consulta a BD si existe el user ********************/
            String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/Login";
            JSONObject obj = new JSONObject();
            try {
                obj.put("correo", email);
                obj.put("password", password);
                obj.put("deviceId", token);
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            AsyncHttpClient client = new AsyncHttpClient();
            ByteArrayEntity entity = null;
            try {
                entity = new ByteArrayEntity(obj.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(String response) {
                    if (response.contains("true")){
                        cambiarAHome();
                    }else{
                        Toast.makeText(getApplicationContext(), "El usuario y/o contraseña son incorrectos", Toast.LENGTH_LONG).show();
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
    }

    public void cambiarAHome(){
        //Obtengo el mail
        Intent homeIntent = new Intent(getApplicationContext(), ServiciosActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                this.finish();
            }
        }
    }

    public void onLoginSuccess() {
        Button loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();
        Button loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailView.setError("Introduzca un email válido");
            valid = false;
        } else {
            mEmailView.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPasswordView.setError("La contraseña debe de contener entre 4 y 10 caracteres alfanuméricos");
            valid = false;
        } else {
            mPasswordView.setError(null);
        }

        return valid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void obtenerServiciosDisponibles() {
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Servicios/ObtenerServicios/On-Site" ;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(ServiciosKey, response);
                editor.commit();
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
}
