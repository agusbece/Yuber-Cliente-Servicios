package yuber.yuberClienteServicios.activity;

import com.google.firebase.iid.FirebaseInstanceIdService;


public class MiFirebaseInstanceIdService extends FirebaseInstanceIdService {

    public static final String TAG = "NOTICIAS";

    @Override
    public void onTokenRefresh() {
     /*   super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().getToken();

        Log.d(TAG, "Token: " + token);

        enviarTokenAlServidor(token);
    */}

    private void enviarTokenAlServidor(String token) {
        // Enviar token al servidor
    }
}