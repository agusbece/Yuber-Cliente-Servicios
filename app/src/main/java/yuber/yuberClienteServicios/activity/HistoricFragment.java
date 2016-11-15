package yuber.yuberClienteServicios.activity;

/**
 * Created by Agustin on 28-Oct-16.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;;import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import yuber.yuberClienteServicios.R;
import yuber.yuberClienteServicios.adapter.HistorialAdapter;

public class HistoricFragment extends Fragment {

    private List<Historial> historialList = new ArrayList<>();
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String HistorialKey = "historialKey";
    public static final String TAG = "FRAGMENTO HISTORIAL";
    SharedPreferences sharedpreferences;
    private String Ip = "54.213.51.6";
    private String Puerto = "8080";

    private JSONObject rec;
    private JSONObject datos;
    private JSONObject datos2;
    private JSONObject datos3;

    public HistoricFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_historic, container, false);

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv_recycler_view_historic);
        rv.setHasFixedSize(true);

        prepareMovieData();
        HistorialAdapter adapter = new HistorialAdapter(historialList);

        rv.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        rv.addOnItemTouchListener(new RecyclerTouchListener(getActivity().getApplicationContext(), rv, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Historial historial = historialList.get(position);
                sendBodyToMapFragment(historial);
            }
            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        return rootView;
    }

    private void sendBodyToMapFragment(Historial historial) {
        Bundle args = new Bundle();
        args.putString("DatosHistorial", historial.toString());
        FragmentDialogYuberHistorial newFragmentDialog = new FragmentDialogYuberHistorial();
        newFragmentDialog.setArguments(args);
        newFragmentDialog.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    private void prepareMovieData() {
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String email = sharedpreferences.getString(EmailKey, "");
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/MisReseñasObtenidas/" + email;
        Log.d(TAG, "LA URL ES: " + url);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(HistorialKey, response);
                editor.commit();
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });

        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String Response = sharedpreferences.getString(HistorialKey, "");
        agregarItems(Response);
    }

    private void agregarItems(String response){
        //Datos que se consumen del JSON
        String Comentario;
        String Puntaje;
        String Costo;
        String Distancia;
        String UbicacionJSON;
        String LatitudO;
        String LongitudO;
        String LatitudD;
        String LongitudD;
        String instanciaServicioJSON;
        String Fecha;
        Historial historial;
        try {
            JSONArray arr_strJson = new JSONArray(response);
            for (int i = 0; i < arr_strJson.length(); ++i) {
                //rec todos los datos de una instancia servicio
                rec = arr_strJson.getJSONObject(i);

                //datos tiene los datos basicos
                datos = new JSONObject(rec.toString());
                Comentario = (String) datos.getString("reseñaComentario");
                Puntaje = (String) datos.getString("reseñaPuntaje");
                instanciaServicioJSON = (String) datos.getString("instanciaServicio");

                //datos2 tiene los datos de la instanciaServicio
                datos2 = new JSONObject(instanciaServicioJSON);
                Costo = (String) datos2.getString("instanciaServicioCosto");
                Distancia = (String) datos2.getString("instanciaServicioDistancia");
                Fecha = (String) datos2.getString("instanciaServicioFechaInicio");

                UbicacionJSON = (String) datos2.getString("ubicacion");
                //datos3 tiene los datos de la ubicacion
                datos3 = new JSONObject(UbicacionJSON);
                LatitudO = (String) datos3.getString("latitud");
                LongitudO = (String) datos3.getString("longitud");

                UbicacionJSON = (String) datos2.getString("ubicacionDestino");
                //datos3 tiene los datos de la ubicacion
                datos3 = new JSONObject(UbicacionJSON);
                LatitudD = (String) datos3.getString("latitud");
                LongitudD = (String) datos3.getString("longitud");

                double lat;
                double lon;

                lat = Double.parseDouble(LatitudO);
                lon = Double.parseDouble(LongitudO);
                String dirO = "-";
                if ((lat != 0)&&(lon != 0)){
                    dirO = getAddressFromLatLng(lat, lon);
                }

                lat = Double.parseDouble(LatitudD);
                lon = Double.parseDouble(LongitudD);
                String dirD = "-";
                if ((lat != 0)&&(lon != 0)){
                    dirD = getAddressFromLatLng(lat, lon);
                }
                //Agrego a la lista
                historial = new Historial(Comentario, Puntaje, Costo, Distancia, dirO, dirD, Fecha);
                historialList.add(historial);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getAddressFromLatLng(double lat, double lon) {
        Geocoder geocoder = new Geocoder( getActivity() );
        String address = "";
        try {
            address =geocoder
                    .getFromLocation( lat, lon, 1 )
                    .get( 0 ).getAddressLine( 0 ) ;
        } catch (IOException e ) {
            // this is the line of code that sends a real error message to the  log
            Log.e("ERROR", "ERROR IN CODE: " + e.toString());
            // this is the line that prints out the location in the code where the error occurred.
            e.printStackTrace();
            return "ERROR_IN_CODE";
        }
        return address;
    }


}