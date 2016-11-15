package yuber.yuberClienteServicios.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import yuber.yuberClienteServicios.R;

/**
 * Fragmento con un diálogo personalizado
 */
public class FragmentDialogFinViaje extends DialogFragment {
    private static final String TAG = FragmentDialogFinViaje.class.getSimpleName();


    private String Ip = "54.213.51.6";
    private String Puerto = "8080";

    private RatingBar ratingBarPuntaje;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String ClienteInstanciaServicioKey = "clienteInstanciaServicioKey";
    public static final String ClienteNombreKey = "clienteNombreKey";
    public static final String ClienteApellidoKey = "clienteApellidoKey";
    public static final String ClienteUbicacionOrigenKey = "ubicacionOrigenKey";
    public static final String ClienteTelefonoKey = "clienteTelefonoKey";
    public static final String ClienteUbicacionDestinoKey = "ubicacionDestinoKey";
    public static final String EnViaje = "enViaje";
    public static final String InstanciaServicioIDKey = "InstanciaServicioIDKey";

    SharedPreferences sharedpreferences;



    public FragmentDialogFinViaje() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    /**
     * Crea un diálogo con personalizado para comportarse
     * como formulario de login
     *
     * @return Diálogo
     */
    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialogo_fin_viaje, null);

        builder.setView(v);


        String stringDatosViaje = getArguments().getString("datosViaje");
        int distancia = -1;
        int costo = -1;
        try {
            JSONObject jsonDatosViaje = new JSONObject(stringDatosViaje);
            distancia = jsonDatosViaje.getInt("instanciaServicioDistancia");
            costo = jsonDatosViaje.getInt("instanciaServicioCosto");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView textoDistancia = (TextView) v.findViewById(R.id.text_distancia_variable_fin_viaje);
        textoDistancia.setText("" + distancia);
        TextView textoCosto = (TextView) v.findViewById(R.id.text_fin_viaje_costo_variable);
        textoCosto.setText("" + costo);

        ratingBarPuntaje = (RatingBar) v.findViewById(R.id.ratingBarFinViaje);

        Button botonConfirmar = (Button) v.findViewById(R.id.boton_confirmar);
        botonConfirmar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Dejo de estar en viaje
                        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString(EnViaje, "false");
                        editor.commit();
                        // mandar al MapFragment (o MainActivity si se quiere)
                        int number = (int) ratingBarPuntaje.getRating();
                        String puntaje = String.valueOf(number);
                        Intent intent = new Intent("MapFragment.action.CALIFICAR_VIAJE");
                        intent.putExtra("PUNTAJE_VIAJE", puntaje);
                        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                        //Envio el puntaje al servidor
                        //enviarPuntaje();
                        //Saco el pop-up
                        dismiss();
                    }
                }
        );
        return builder.create();
    }



    public void enviarPuntaje(){
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        int instanciaID = sharedpreferences.getInt(InstanciaServicioIDKey, -333333333);
        String puntaje = "9";
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/PuntuarProveedor/" + puntaje + ",-," + instanciaID;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
               // Toast.makeText(getActivity().getApplicationContext(), "puntuo!", Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
               /* if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
                */
            }
        });
    }


}

