package yuber.yuberClienteServicios.activity;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import yuber.yuberClienteServicios.R;
import yuber.yuberClienteServicios.adapter.ServiciosAdapter;

;

public class ServiciosFragment extends Fragment {

    private String Ip = "";
    private String Puerto = "8080";


    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String IdServicioKey = "IdServicioKey";
    public static final String EmailKey = "emailKey";
    public static final String ServiciosKey = "ServiciosKey";
    SharedPreferences sharedpreferences;

    private List<Servicios> servicioList = new ArrayList<>();

    private JSONObject rec;
    private JSONObject datos;
    private JSONObject datos2;
    private JSONObject datos3;


    public ServiciosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_servicios, container, false);
        Ip = getResources().getString(R.string.IP);

        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv_recycler_view);
        rv.setHasFixedSize(true);

        //LO NUEVO QUE HICE 30-OCT

        obtenerServiciosDisponibles();

        ServiciosAdapter adapter = new ServiciosAdapter(servicioList);



        // termina lo nuevo



        //HistorialAdapter adapter = new HistorialAdapter(new String[]{"test one", "test two", "test three", "test four", "test five" , "test six" , "test seven", "test eight", "test nine", "test ten"});
        rv.setAdapter(adapter);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        //AGREGA LA RAYITA AL MEDIO
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        rv.setItemAnimator(new DefaultItemAnimator());

        rv.addOnItemTouchListener(new HistoricRecyclerTouchListener(getActivity().getApplicationContext(), rv, new HistoricRecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
              cambiarAMapa(position);
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));



        return rootView;
    }


    public void cambiarAMapa(int id){
        System.out.print("--------");
        Servicios servicios = servicioList.get(id);
        Gson gson = new Gson();
        String jsonServicio = gson.toJson(servicios);
        // Seteo en el SharedPreferences el ID del servicio generado y voy a mapa
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IdServicioKey, jsonServicio);
        editor.commit();
        Toast.makeText(getActivity().getApplicationContext(), "Ha seleccionado viajar en " + servicios.getNombre() , Toast.LENGTH_SHORT).show();

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.setmIdServicio(servicios.getID());
        mainActivity.displayView(1);

        mainActivity.cargarHistorial(servicios.getID());

        System.out.print("--------");
        System.out.println(mainActivity.getListaHistorial());
    }


    private void obtenerServiciosDisponibles() {

        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Servicios/ObtenerServicios/On-Site" ;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(ServiciosKey, response);
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
        String Response = sharedpreferences.getString(ServiciosKey, "");
        agregarItems(Response);
    }

    private void agregarItems(String response){
        Servicios servicio;
        try {
            JSONArray arr_strJson = new JSONArray(response);
            for (int i = 0; i < arr_strJson.length(); ++i) {
                //rec todos los datos de una instancia servicio
                JSONObject jsonServicio = arr_strJson.getJSONObject(i);
                int id = jsonServicio.getInt("servicioId");
                int tarifaBase = jsonServicio.getInt("servicioTarifaBase");
                int precioKM = jsonServicio.getInt("servicioPrecioKM");
                String nombre = jsonServicio.getString("servicioNombre");

                //Agrego a la lista
                servicio = new Servicios(id, tarifaBase, precioKM, nombre);
                servicioList.add(servicio);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}