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
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import yuber.yuberClienteServicios.R;
import yuber.yuberClienteServicios.adapter.HistorialAdapter;

;

public class HistoricFragment extends Fragment {

    private List<Historial> historialList = new ArrayList<>();
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String HistorialKey = "historialKey";
    public static final String TAG = "FRAGMENTO HISTORIAL";
    SharedPreferences sharedpreferences;
    private String Ip = "";
    private String Puerto = "8080";


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
        Ip = getResources().getString(R.string.IP);

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv_recycler_view_historic);
        rv.setHasFixedSize(true);

        MainActivity mainActivity = (MainActivity)getActivity();
        List<Historial> lista = mainActivity.getListaHistorial();
        Historial htemp;
        Iterator<Historial> it = lista.iterator();
        while (it.hasNext()) {
            htemp = it.next();
            historialList.add(htemp);
        }

        HistorialAdapter adapter = new HistorialAdapter(historialList);

        rv.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        rv.addOnItemTouchListener(new HistoricRecyclerTouchListener(getActivity().getApplicationContext(), rv, new HistoricRecyclerTouchListener.ClickListener() {
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

    private void obtenerServiciosDisponibles() {
        MainActivity mainActivity = (MainActivity) getActivity();
        int idServicio = mainActivity.getmIdServicio();
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String email = sharedpreferences.getString(EmailKey, "");
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/MisReseñasObtenidas/" + email + "," + idServicio;
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
        String latOrigen;
        String longOrigen;
        String latDestino;
        String longDestino;
        String instanciaServicioJSON;
        String Fecha;
        Historial historial;


        JSONObject dataHistoria;
        JSONObject jsonInstanciaServicio;

        try {
            JSONArray arr_strJson = new JSONArray(response);
            for (int i = 0; i < arr_strJson.length(); ++i) {
                //dataHistoria todos los datos de una instancia servicio
                dataHistoria = arr_strJson.getJSONObject(i);

                Log.d(TAG, "dataHistoria: " + dataHistoria);
                //Comentario = jsonReseniaProveedor.getString("reseñaComentario");
                double doublePuntaje = dataHistoria.getDouble("reseñaPuntaje");
                Puntaje = "" + doublePuntaje;

                //jsonReseniaCliente tiene el data de reseña hecho al proveedor
                jsonInstanciaServicio = new JSONObject(dataHistoria.getString("instanciaServicio"));
                Costo = jsonInstanciaServicio.getString("instanciaServicioCosto");
                Distancia = jsonInstanciaServicio.getString("instanciaServicioDistancia");
                Fecha = jsonInstanciaServicio.getString("instanciaServicioFechaFin");

                try {
                    Long longFecha = Long.parseLong(Fecha);
                    final Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(longFecha);
                    final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
                    Fecha = f.format(cal.getTime());
                    //System.out.println(f.format(cal.getTime()))
                }catch (Exception e){
                    Log.d(TAG, "Problema parseando la fecha: " + e);
                }


                JSONObject jsonUbicacion = new JSONObject(jsonInstanciaServicio.getString("ubicacion"));
                latOrigen = jsonUbicacion.getString("latitud");
                longOrigen = jsonUbicacion.getString("longitud");

                jsonUbicacion = new JSONObject(jsonInstanciaServicio.getString("ubicacionDestino"));
                latDestino = jsonUbicacion.getString("latitud");
                longDestino = jsonUbicacion.getString("longitud");

                double lat;
                double lon;

                lat = Double.parseDouble(latOrigen);
                lon = Double.parseDouble(longOrigen);
                String dirO = "-";
                if ((lat != 0)&&(lon != 0)){
                    dirO = getAddressFromLatLng(lat, lon);
                }

                lat = Double.parseDouble(latDestino);
                lon = Double.parseDouble(longDestino);
                String dirD = "-";
                if ((lat != 0)&&(lon != 0)){
                    dirD = getAddressFromLatLng(lat, lon);
                }

                Comentario = "";
                Log.d(TAG, "Distancia es: " + Distancia);
                //Agrego a la lista
                historial = new Historial(Comentario, Puntaje, Costo, Distancia, dirO, dirD, Fecha);
                Log.d(TAG, "Distancia es: " + historial.getDistancia());
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