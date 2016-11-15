package yuber.yuberClienteServicios.activity;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;


public class MiFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "NOTICIAS";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Gson gson = new Gson();
        //      super.onMessageReceived(remoteMessage);



        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            Log.d(TAG, "Message data payload: " + data);


            String tituloNotificacion = remoteMessage.getNotification().getTitle();

            Log.d(TAG, "Message textNombreServicio: " + tituloNotificacion );

            if (tituloNotificacion.equals("Tu Yuber esta en camino")){
                String jsonData = gson.toJson(data);
                sendProviderInfoToMapFragment(jsonData);
            }
            else if (tituloNotificacion.equals("Ubicacion proveedor")){
                String jsonData = gson.toJson(data);
                mandarUbicacionMapFragment(jsonData);
            }
            else if (tituloNotificacion.equals("Empieza el viaje")){
                mandarEmpiezaViajeMapFragment();
            }
            else if (tituloNotificacion.equals("Filanizo su viaje")){
                String jsonData = gson.toJson(data);
                mandarTerminoViajeMapFragment(jsonData);
            }



        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

        }



        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.


        //       String from = remoteMessage.getFrom();
        //       Log.d(TAG, "Mensaje recibido de: " + from);
/*
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notificación: " + remoteMessage.getNotification().getBody());
            mostrarNotificacion(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data: " + remoteMessage.getData());
        }
*/
    }


    protected void sendProviderInfoToMapFragment(String text) {
        Intent intent = new Intent("MapFragment.action.YUBER_DISPONIBLE");
        intent.putExtra("DATOS_PROVEEDOR", text);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    protected void mandarEmpiezaViajeMapFragment() {
        Intent intent = new Intent("MapFragment.action.EMPIEZA_VIAJE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    protected void mandarTerminoViajeMapFragment(String text) {
        Intent intent = new Intent("MapFragment.action.TERMINO_VIAJE");
        intent.putExtra("DATOS_VIAJE", text);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void mandarUbicacionMapFragment(String text) {
        Intent intent = new Intent("MapFragment.action.UBICACION_YUBER");
        intent.putExtra("UBICACION", text);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    private void mostrarNotificacion(String title, String body) {

        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //QUE ES ESTO? jaja
        startActivity(homeIntent);

   /*
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                //.setSmallIcon(R.drawable.logo)    IMAGEN DE LA NOTIFICACION
                .setContentTitle(textNombreServicio)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());

       */

    }


}
