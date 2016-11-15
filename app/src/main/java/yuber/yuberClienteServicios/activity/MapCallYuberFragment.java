package yuber.yuberClienteServicios.activity;

/**
 * Created by Agustin on 20-Oct-16.
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import yuber.yuberClienteServicios.R;


public class MapCallYuberFragment extends Fragment {



    public static final String TAG = "CALL FRAGMENT";


    public MapCallYuberFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_calling, container, false);




        Log.d(TAG, "ADENTRO DEL ACTION_MI_UBICACION: ");

        //seteando listener en boton
        Button botonMiUbicacion = (Button) rootView.findViewById(R.id.buttonMiUbicacion);
        botonMiUbicacion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent("MapFragment.action.MI_UBICACION");
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);

            }
        });



        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}