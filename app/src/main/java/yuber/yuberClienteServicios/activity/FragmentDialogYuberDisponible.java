package yuber.yuberClienteServicios.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import yuber.yuberClienteServicios.R;

/**
 * Fragmento con un diálogo personalizado
 */
public class FragmentDialogYuberDisponible extends DialogFragment {
    private static final String TAG = FragmentDialogYuberDisponible.class.getSimpleName();
    private JSONObject mProveedor;

    public FragmentDialogYuberDisponible() {
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

       // String proveedorString = getArguments().getString("proveedorJson");
        String proveedorString = getArguments().getString("datos");


        try {
            mProveedor = new JSONObject(proveedorString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.dialogo_yuber_disponible, null);

        builder.setView(v);


       // RatingBar ratingBarYuber = (RatingBar) v.findViewById(R.id.ratingBarYuberDispo);
        //ratingBarYuber.setRating();



        TextView textoNombreProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_nombre);
        TextView textoAppellidoProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_apellido);
        TextView textoMarcaModProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_marca);
        TextView textoTelefonoProv = (TextView) v.findViewById(R.id.text_dialog_yub_disp_telefono);
        RatingBar ratingBarPuntajeProv = (RatingBar) v.findViewById(R.id.ratingBarYuberDispo);

        String stringPuntaje = "";
        try {
            textoNombreProv.setText(mProveedor.getString("usuarioNombre"));
            textoAppellidoProv.setText(mProveedor.getString("usuarioApellido"));
            //textoMarcaModProv.setText(mProveedor.getString("marca") + " " + mProveedor.getString("modelo"));
            textoTelefonoProv.setText(mProveedor.getString("usuarioTelefono"));
            stringPuntaje = mProveedor.getString("usuarioPromedioPuntaje");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            ratingBarPuntajeProv.setRating(Float.parseFloat(stringPuntaje));
        }
        catch (Exception e){
            ratingBarPuntajeProv.setRating(0);
            Log.d(TAG, "Comportamiento extrano del rating: " + e );
        }



        Button botonAceptar = (Button) v.findViewById(R.id.boton_aceptar_yuber);
        Button botonCancelar = (Button) v.findViewById(R.id.boton_cancelar_yuber);

        Log.d(TAG, "Se creo el dialogo con el login");

        botonAceptar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Crear Cuenta...
                        dismiss();
                    }
                }
        );

        botonCancelar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Loguear...
                        dismiss();
                    }
                }

        );

        return builder.create();
    }

}

