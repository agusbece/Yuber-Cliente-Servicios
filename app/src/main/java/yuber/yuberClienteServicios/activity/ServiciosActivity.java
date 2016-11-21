package yuber.yuberClienteServicios.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
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

public class ServiciosActivity extends AppCompatActivity {

    private String Ip = "";
    private String Puerto = "8080";

    private List<Servicios> serviciosList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ServiciosAdapter mAdapter;



    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String IdServicioKey = "IdServicioKey";
    public static final String EmailKey = "emailKey";
    public static final String ServiciosKey = "ServiciosKey";
    SharedPreferences sharedpreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios);
        Ip = getResources().getString(R.string.IP);
        obtenerServiciosDisponibles();

        recyclerView = (RecyclerView) findViewById(R.id.rv_recycler_view_servicios);

        mAdapter = new ServiciosAdapter(serviciosList);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(),3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Servicios servicio = serviciosList.get(position);
                pasarAIntro(position);
                //Toast.makeText(getApplicationContext(), servicio.getNombre() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));



        //prepareMovieData();
    }

    private void pasarAIntro(int posicion) {
        Servicios servicios = serviciosList.get(posicion);
        Gson gson = new Gson();
        String jsonServicio = gson.toJson(servicios);
        // Seteo en el SharedPreferences el ID del servicio generado y voy a mapa
        SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(IdServicioKey, servicios.getID());
        editor.putString(ServiciosKey, jsonServicio);
        editor.commit();

        Intent homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }


    private void obtenerServiciosDisponibles() {


        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Servicios/ObtenerServicios/On-Site" ;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString(ServiciosKey, response);
                editor.commit();
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
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
                serviciosList.add(servicio);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    private void prepareMovieData() {

        Servicios servicio = new Servicios(1,50,50,"Carpinteria");
        serviciosList.add(servicio);

        servicio = new Servicios(1,50,50,"Sanitario");
        serviciosList.add(servicio);

        servicio = new Servicios(1,50,50,"Electricista");
        serviciosList.add(servicio);

        servicio = new Servicios(1,50,50,"Jardineria");
        serviciosList.add(servicio);

        servicio = new Servicios(1,50,50,"Peluqueria");
        serviciosList.add(servicio);

        servicio = new Servicios(1,50,50,"Limpieza");
        serviciosList.add(servicio);

        servicio = new Servicios(1,50,50,"Informatica");
        serviciosList.add(servicio);

        mAdapter.notifyDataSetChanged();
    }

}
