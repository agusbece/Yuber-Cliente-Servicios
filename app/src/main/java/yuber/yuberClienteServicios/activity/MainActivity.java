package yuber.yuberClienteServicios.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import yuber.yuberClienteServicios.R;

public class MainActivity extends AppCompatActivity implements FragmentDrawer.FragmentDrawerListener  {

    private Toolbar mToolbar;
    private FragmentDrawer drawerFragment;

    private String Ip = "";
    private String Puerto = "8080";

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String TokenKey = "tokenKey";
    public static final String IdServicioKey = "IdServicioKey";
    public static final String ServiciosKey = "ServiciosKey";
    public static final String TAG = "MAIN ACTIVITY";
    private String nombreApellido;
    private int mIdServicio = 0;
    private List<Historial> ListaHistorial = new ArrayList<>();


    private String emailSession = "";
    private String tokenSession = "";

    public String getEmailSession(){
        return emailSession;
    }

    public int getmIdServicio(){
        return mIdServicio;
    }

    public void setmIdServicio(int id){
        mIdServicio = id;
    }

    public String getTokenSession(){
        return tokenSession;
    }

    public String getNombtrUsuario(){
        return nombreApellido;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Ip = getResources().getString(R.string.IP);

        //ADDED FOR TOOLBAR
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
        drawerFragment.setDrawerListener(this);

        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        emailSession = sharedpreferences.getString(EmailKey, "");
        tokenSession = sharedpreferences.getString(TokenKey, "");
        int IdServicio = sharedpreferences.getInt(IdServicioKey, 0);
        Log.d(TAG, "El token es: " + tokenSession);

        // display the first navigation drawer view on app launch
        displayView(0);

        cargarHistorial(IdServicio);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public List<Historial> getListaHistorial() {
        return ListaHistorial;
    }

    public void agregarEnHistorial(Historial h){
        ListaHistorial.add(h);
    }

    public void cargarHistorial(int idServicio){
        System.out.println("----entre a cargar el historial");

        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        String email = sharedpreferences.getString(EmailKey, "");
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/MisReseñasObtenidas/" + email + "," + idServicio;
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                cargar(true, response);
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                cargar(false, "");
            }
        });
    }

    private void cargar(boolean ok, String ListaConDatos){
        //Datos que se consumen del JSON
        String Comentario;
        String Puntaje;
        String Costo;
        String Tiempo;
        String dirO = "-";
        String dirD = "-";
        String Fecha;
        Historial historial;
        if (ok){
            try {
                JSONObject rec;
                JSONObject datos;
                JSONObject datos2;
                JSONObject datos3;
                JSONArray arr_strJson = new JSONArray(ListaConDatos);
                ListaHistorial = new ArrayList<Historial>();
                for (int i = 0; i < arr_strJson.length(); ++i) {
                    //rec todos los datos de una instancia servicio
                    rec = arr_strJson.getJSONObject(i);
                    //datos tiene los datos basicos
                    datos = new JSONObject(rec.toString());
                    Comentario = (String) datos.getString("reseñaComentario");
                    Puntaje = (String) datos.getString("reseñaPuntaje");
                    String instanciaServicioJSON = (String) datos.getString("instanciaServicio");
                    //datos2 tiene los datos de la instanciaServicio
                    datos2 = new JSONObject(instanciaServicioJSON);
                    Costo = (String) datos2.getString("instanciaServicioCosto");

                    Tiempo = (String) datos2.getString("instanciaServicioDistancia");
                    float flo = Float.valueOf(Tiempo);
                    flo = flo * 1000;
                    int time = (int) flo;

                    Fecha = (String) datos2.getString("instanciaServicioFechaInicio");

                    Long longFecha = Long.parseLong(Fecha);
                    final Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(longFecha);
                    final SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
                    Fecha = f.format(cal.getTime());

                    String UbicacionJSON = (String) datos2.getString("ubicacion");
                    //datos3 tiene los datos de la ubicacion
                    datos3 = new JSONObject(UbicacionJSON);
                    String LatitudO = (String) datos3.getString("latitud");
                    String LongitudO = (String) datos3.getString("longitud");

                    UbicacionJSON = (String) datos2.getString("ubicacionDestino");
                    //datos3 tiene los datos de la ubicacion
                    datos3 = new JSONObject(UbicacionJSON);
                    String LatitudD = (String) datos3.getString("latitud");
                    String LongitudD = (String) datos3.getString("longitud");
                    double lat;
                    double lon;
                    lat = Double.parseDouble(LatitudO);
                    lon = Double.parseDouble(LongitudO);
                    dirO = "-";
                    if ((lat != 0)&&(lon != 0)){
                        dirO = getAddressFromLatLng(lat, lon);
                    }

                    lat = Double.parseDouble(LatitudD);
                    lon = Double.parseDouble(LongitudD);
                    dirD = "-";
                    if ((lat != 0)&&(lon != 0)){
                        dirD = getAddressFromLatLng(lat, lon);
                    }
                    //Agrego a la lista
                    historial = new Historial(Comentario, Puntaje, Costo, obtenerTiempo(time), dirO, dirD, Fecha);
                    ListaHistorial.add(historial);
                }
            } catch (Exception e) {
                ListaHistorial = new ArrayList<Historial>();
            }
        }else{
            ListaHistorial = new ArrayList<Historial>();
        }
    }

    public String obtenerTiempo(int tiempo){
        String horas = "00";
        String minutos = "00";
        String segundos = "00";
        int resto = tiempo;

        int h = resto / (24*60);
        resto = resto % (24*60);
        horas = String.valueOf(h);
        if(h < 10) {
            horas = "0" + horas;
        }

        int m = resto / (60);
        resto = resto % (24*60);
        minutos = String.valueOf(m);
        if(m < 10) {
            minutos = "0" + minutos;
        }

        int s = resto;
        segundos = String.valueOf(s);
        if(s < 10) {
            segundos = "0" + segundos;
        }

        return (horas + ":" + minutos + ":" + segundos);
    }

    private String getAddressFromLatLng(double lat, double lon) {
        Geocoder geocoder = new Geocoder( this );
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/


        if (id == R.id.action_cerrar_sesion) {
            CerrarSesion();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    public void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case 0:
                fragment = new MapFragment();
                SharedPreferences sharedPreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String stringJsonServicio = sharedPreferences.getString(ServiciosKey, "ERROR - ALGO ANDA MAL");
                String nombreServicio = "";
                try {
                    JSONObject jsonServicio = new JSONObject(stringJsonServicio);
                    nombreServicio = jsonServicio.getString("mNombre");
                    mIdServicio = jsonServicio.getInt("mID");
                } catch (JSONException e) {
                    e.printStackTrace();
                    nombreServicio = "ERROR";
                }
                title = nombreServicio;
                break;
            case 1:
                fragment = new HistoricFragment();
                title = getString(R.string.title_historic);
                break;
            default:
                break;
        }

        if (fragment != null) {



            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar textNombreServicio
            getSupportActionBar().setTitle(title);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void CerrarSesion(){
        //
        // TODO AGREGAR ABANDONAR SERVICIO
        //
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/Logout";
        JSONObject obj = new JSONObject();
        try {
            obj.put("correo", emailSession);
            obj.put("password", "");
            obj.put("deviceId", tokenSession);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(obj.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        RequestHandle Rq = client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (response.contains("true") ){
                    cambiarALogin();
                }else{
                    Toast.makeText(getApplicationContext(), "No se pudo cerrar la sesión. Vuelva a intentar.", Toast.LENGTH_LONG).show();
                }
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
    }

    public void cambiarALogin(){
        Intent homeIntent = new Intent(getApplicationContext(), LoginActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }



}
