package yuber.yuberClienteServicios.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import yuber.yuberClienteServicios.R;
import yuber.yuberClienteServicios.dialog.ErrorDialogFragment;
import yuber.yuberClienteServicios.dialog.ProgressDialogFragment;

public class SignUpActivity extends AppCompatActivity {

    private static final String CURRENCY_UNSPECIFIED = "Unspecified";
    public static final String PUBLISHABLE_KEY = "pk_test_4vfEzEuVKYT0Wzk5uvh3WEpa";
    private static final String TAG = "SignUpActivity";

    private String Ip = "";
    private String Puerto = "8080";

    private EditText nameText;
    private EditText addressText;
    private EditText emailText;
    private EditText mobileText;
    private EditText passwordText;
    private EditText reEnterPasswordText;
    private EditText LastNameText;
    private EditText ciudadText;
    ProgressDialog prgDialog;


    //======STRIPE ==================
    //Button saveButton;
    EditText cardNumber;
    EditText cvc;
    Spinner monthSpinner;
    Spinner yearSpinner;
    Spinner currencySpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Ip = getResources().getString(R.string.IP);

        Button signupButton = (Button) findViewById(R.id.btn_signup);
        TextView loginLink = (TextView) findViewById(R.id.link_login);

        nameText = (EditText) findViewById(R.id.input_name);
        addressText = (EditText) findViewById(R.id.input_address);
        emailText = (EditText) findViewById(R.id.input_email);
        mobileText = (EditText) findViewById(R.id.input_mobile);
        passwordText = (EditText) findViewById(R.id.input_password);
        reEnterPasswordText = (EditText) findViewById(R.id.input_reEnterPassword);
        LastNameText = (EditText) findViewById(R.id.input_LastName);
        ciudadText = (EditText) findViewById(R.id.input_ciudad);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Creando su cuenta...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        //======STRIPE ==================

        this.cardNumber = (EditText) findViewById(R.id.number);
        this.cvc = (EditText) findViewById(R.id.cvc);
        this.monthSpinner = (Spinner) findViewById(R.id.expMonth);
        this.yearSpinner = (Spinner) findViewById(R.id.expYear);
        this.currencySpinner = (Spinner) findViewById(R.id.currency);
    }

    public void signup() {
        if (!validate()) {
            onSignupFailed();
            return;
        }
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(false);

        prgDialog.show();

        final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                R.style.AppTheme_Dialog);


        String name = nameText.getText().toString();
        String LastName = LastNameText.getText().toString();
        String address = addressText.getText().toString();
        String email = emailText.getText().toString();
        String mobile = mobileText.getText().toString();
        String ciudad = ciudadText.getText().toString();
        String password = passwordText.getText().toString();

        //*****************  Consulta a BD si existe el user ********************//
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/RegistrarCliente/";
        JSONObject obj = new JSONObject();
        JSONObject objVert = new JSONObject();
        try {
            obj.put("usuarioDireccion", address);
            obj.put("usuarioContraseña", password);
            obj.put("usuarioTelefono", mobile);
            obj.put("usuarioApellido", LastName);
            obj.put("usuarioNombre", name);
            obj.put("usuarioPromedioPuntaje", 0.0);
            obj.put("usuarioCorreo", email);
            obj.put("usuarioCiudad", ciudad);
            obj.put("estado", "OK");

            objVert.put("tipoVertical", "Transporte");
            objVert.put("cliente", obj);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(objVert.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "onSucces del RegistrarCliente: " + response);

                if (response.contains("Ok")){
                    //Mando el token de la tarjeta
                    saveCreditCard();
                    //llamo a login para que cree la session
                    login();
                }else{
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
        //*****************  Lo redirecciono al MainActivity ********************//

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        onSignupSuccess();
                        //progressDialog.dismiss();
                    }
                }, 3000);

    }

    public void login(){
        //token
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                String value = getIntent().getExtras().getString(key);
            }
        }
        //adding token
        String token = FirebaseInstanceId.getInstance().getToken();

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
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
                Log.d(TAG, "onSucces del Cliente/Login: " + response);

                if (response.contains("true")){
                    Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
                    homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                }else{
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onSignupSuccess() {
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "La creación de cuenta ha fallado", Toast.LENGTH_LONG).show();
        Button signupButton = (Button) findViewById(R.id.btn_signup);
        signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = nameText.getText().toString();
        String LastName = LastNameText.getText().toString();
        String ciudad = ciudadText.getText().toString();
        String address = addressText.getText().toString();
        String email = emailText.getText().toString();
        String mobile = mobileText.getText().toString();
        String password = passwordText.getText().toString();
        String reEnterPassword = reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            nameText.setError("Al menos 3 caracteres");
            valid = false;
        } else {
            nameText.setError(null);
        }

        if (LastName.isEmpty() || LastName.length() < 3) {
            LastNameText.setError("Al menos 3 caracteres");
            valid = false;
        } else {
            LastNameText.setError(null);
        }

        if (ciudad.isEmpty() || ciudad.length() < 3) {
            ciudadText.setError("Al menos 4 caracteres");
            valid = false;
        } else {
            ciudadText.setError(null);
        }

        if (address.isEmpty()) {
            addressText.setError("Ingrese una contraseña válida");
            valid = false;
        } else {
            addressText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Ingrese un email válido");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=9) {
            mobileText.setError("Ingrese un número de celular válido");
            valid = false;
        } else {
            mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError("La contraseña debe ser de entre 4 y 10 caracteres alfanuméricos");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError("Las contraseñas no coinciden");
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        Card card = new Card(
                cardNumber.getText().toString(),
                getInteger(this.monthSpinner),
                getInteger(this.yearSpinner),
                cvc.getText().toString());
        card.setCurrency(getCurrency());

        if (!card.validateNumber()) {
            valid = false;
            handleError("El número de tarjeta ingresado es inválido");
        }
        else if (!card.validateExpiryDate()) {
             valid = false;
             handleError("La fecha de vencimiento ingresada es inválida");
         }
        else if (!card.validateCVC()) {
             valid = false;
             handleError("El código CVC ingresado es inválido");
         }

        return valid;
    }

    //=============================================
    // DEL PAGO CON STRIPE

    private ProgressDialogFragment progressFragment;

    public void saveCreditCard() {

        Card card = new Card(
                cardNumber.getText().toString(),
                getInteger(this.monthSpinner),
                getInteger(this.yearSpinner),
                cvc.getText().toString());
        card.setCurrency(getCurrency());

        boolean validation = card.validateCard();
        if (validation) {
            //startProgress();
            new Stripe().createToken(
                    card,
                    PUBLISHABLE_KEY,
                    new TokenCallback() {
                        public void onSuccess(Token token) {
                            String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/AsociarMecanismoDePago/" + emailText.getText().toString() + "," + token.getId().toString();
                            AsyncHttpClient client = new AsyncHttpClient();
                            ByteArrayEntity entity = null;
                            client.get(null, url, new AsyncHttpResponseHandler(){
                                @Override
                                public void onSuccess(String response) {
                                    if (response.contains("Ok")){
                                        //nada
                                    }else{
                                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                                    }
                                }
                                @Override
                                public void onFailure(int statusCode, Throwable error, String content){
                                    if(statusCode == 404){
                                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                                    }else if(statusCode == 500){
                                        Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            //finishProgress();
                        }
                        public void onError(Exception error) {
                            handleError(error.getLocalizedMessage());
                            finishProgress();
                        }
                    });
        } else if (!card.validateNumber()) {
            handleError("El número de tarjeta ingresado es inválido");
        } else if (!card.validateExpiryDate()) {
            handleError("La fecha de vencimiento ingresada es inválida");
        } else if (!card.validateCVC()) {
            handleError("El código CVC ingresado es inválido");
        } else {
            handleError("Los detalles de la tarjeta ingresada son inválidos");
        }
    }


    private void startProgress() {
       // progressFragment.show(getSupportFragmentManager(), "progress");
    }

    private void finishProgress() {
        progressFragment.dismiss();
    }

    private void handleError(String error) {
        DialogFragment fragment = ErrorDialogFragment.newInstance(R.string.validationErrors, error);
        fragment.show(getSupportFragmentManager(), "error");
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


    private Integer getInteger(Spinner spinner) {
        try {
            return Integer.parseInt(spinner.getSelectedItem().toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    public String getCurrency() {
        if (currencySpinner.getSelectedItemPosition() == 0) return null;
        String selected = (String) currencySpinner.getSelectedItem();
        if (selected.equals(CURRENCY_UNSPECIFIED))
            return null;
        else
            return selected.toLowerCase();
    }

}
