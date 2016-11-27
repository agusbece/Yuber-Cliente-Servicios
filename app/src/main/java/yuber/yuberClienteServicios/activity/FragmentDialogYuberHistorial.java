package yuber.yuberClienteServicios.activity;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import yuber.yuberClienteServicios.R;

public class FragmentDialogYuberHistorial extends DialogFragment {
    private static final String TAG = "FragmentDialogYuberHistorial";
    private JSONObject datos;
    private String Ip = "";
    private String Puerto = "8080";

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String TokenKey = "tokenKey";
    SharedPreferences sharedpreferences;

    public FragmentDialogYuberHistorial() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return createLoginDialogo();
    }

    public AlertDialog createLoginDialogo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String datosHistorial = getArguments().getString("DatosHistorial");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialogo_historial, null);
        Ip = getResources().getString(R.string.IP);
        builder.setView(v);

        TextView textoFecha = (TextView) v.findViewById(R.id.text_fecha_historial);
        TextView textoCosto = (TextView) v.findViewById(R.id.text_hist_costo_variable);
        TextView textoComentario = (TextView) v.findViewById(R.id.text_hist_costo_variable);
        TextView textoDistancia = (TextView) v.findViewById(R.id.text_hist_distancia_variable);
        TextView textoOrigen = (TextView) v.findViewById(R.id.text_hist_origen_variable);
        RatingBar ratingBarPuntaje = (RatingBar) v.findViewById(R.id.ratingBarDialogHistorial);
        double puntaje = 0;

        try {
            datos = new JSONObject(datosHistorial);
            textoComentario.setText(datos.getString("Comentario"));
            textoFecha.setText(datos.getString("Fecha"));
            textoCosto.setText("$ " + datos.getString("Costo"));
            textoDistancia.setText(datos.getString("Distancia") + "Km");
            textoOrigen.setText(datos.getString("DireccionO"));
            puntaje = datos.getDouble("Puntaje");
            if (puntaje > 5){
                puntaje = 5;
            }
            ratingBarPuntaje.setRating(((float) puntaje));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button botonAceptar = (Button) v.findViewById(R.id.boton_aceptar_yuber);
        botonAceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                }
        );

        return builder.create();

    }

}

