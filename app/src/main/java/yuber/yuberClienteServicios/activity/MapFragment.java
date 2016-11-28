package yuber.yuberClienteServicios.activity;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yuber.yuberClienteServicios.R;

/**
 * A actualFragment that launches other parts of the demo application.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener{



    //Banderas del broadcaster
    public static final String ACTION_INTENT = "MapFragment.action.YUBER_DISPONIBLE";
    public static final String ACTION_MI_UBICACION = "MapFragment.action.MI_UBICACION";
    public static final String ACTION_EMPIEZA_VIAJE = "MapFragment.action.EMPIEZA_VIAJE";
    public static final String ACTION_TERMINO_VIAJE = "MapFragment.action.TERMINO_VIAJE";
    public static final String ACTION_CALIFICAR_VIAJE = "MapFragment.action.CALIFICAR_VIAJE";
    public static final String ACTION_UBICACION_YUBER = "MapFragment.action.UBICACION_YUBER";
    public static final String ACTION_CONTADOR_FINALIZO = "MapFragment.action.CONTADOR_FINALIZO";
    public static final String TAG = "MAPA";

    //Bander para las preferencias compartidas (obtener datos glovales)
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String EmailKey = "emailKey";
    public static final String TokenKey = "tokenKey";
    public static final String IdServicioKey = "IdServicioKey";
    public static final String ServiciosKey = "ServiciosKey";
    public static final String InstanciaServicioIDKey = "InstanciaServicioIDKey";

    //del tutorial https://androidkennel.org/android-tutorial-getting-the-users-location/ para manejar el identificador del permiso concedido
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final int REQUEST_LOCATION = 1 ;

    //banderas de conexion
    private String Ip = "";
    private String Ip2 = "54.203.12.195";
    private String Puerto = "8080";

    // COSAS DEL MAPA
    MapView mMapView;
    private GoogleMap googleMap;
    LocationRequest mLocationRequest; // NO ESTOY SEGURO PARA QUE SIRVE ESTO
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private Marker mOrigenMarker;
    private Marker mDestinationMarker;
    private Marker mUbicacionYuberProveedor;


    //Elementos del UI
    private Switch switchGPS;
    private TextView textoUbicacionOrigen;
    private TextView textoUbicacion;
    private Button mButtonLlammarUber;

    private enum mapState {ELIGIENDO_ORIGEN, BUSCANDO_YUBER, YUBER_EN_CAMINO, YUBER_TRABAJANDO}


    private mapState mActualState;
    private Fragment actualFragment = null;
    private int mIdServicioEnUso = -1;
    private int mIdInstanciaServicio;

    SharedPreferences sharedPreferences;

    // Progress Dialog Object
    ProgressDialog prgDialog;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_mp, container,
                false);
        Ip = getResources().getString(R.string.IP);


        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the googleMap to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) { //comentario
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);
        mButtonLlammarUber = (Button) v.findViewById(R.id.callYuberButton);
        //seteando listener en boton
        mButtonLlammarUber.setOnClickListener(createListenerBottomButton());

        displayView(mapState.ELIGIENDO_ORIGEN);



        sharedPreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String stringJsonServicio = sharedPreferences.getString(ServiciosKey, "ERROR - ALGO ANDA MAL");
        try {
            JSONObject servicioSeleccionado = new JSONObject(stringJsonServicio);
            mIdServicioEnUso = servicioSeleccionado.getInt("mID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // PARA TESTING... SEGURAMENTE SIN USO FUTURO, PODRIA SER ELIMINADO O REUSADO EN OTRO CODIGO
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(getActivity());
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);


        IntentFilter filter = new IntentFilter(ACTION_INTENT);
        filter.addAction(ACTION_MI_UBICACION);
        filter.addAction(ACTION_EMPIEZA_VIAJE);
        filter.addAction(ACTION_TERMINO_VIAJE);
        filter.addAction(ACTION_CALIFICAR_VIAJE);
        filter.addAction(ACTION_UBICACION_YUBER);
        filter.addAction(ACTION_CONTADOR_FINALIZO);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(ActivityDataReceiver, filter);

        Log.d(TAG, "SE CREO EL MAPA");

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }
        // Perform any camera updates here



        //PARA LA CREACION DE RUTAS
        // Initializing
        markerPoints = new ArrayList<LatLng>();


        return v;
    } // FIN onCreate()


    protected BroadcastReceiver ActivityDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "En BroadcastReceiver con ACTION: " + intent.getAction());
            if(ACTION_INTENT.equals(intent.getAction()) && (mActualState == mapState.BUSCANDO_YUBER) ){
                //Si llega una notificacion "Proveedor acepto viaje" y se esta buscando Yuber
                String jsonProveedor = intent.getStringExtra("DATOS_PROVEEDOR");
                displayView(mapState.YUBER_EN_CAMINO);
                mostrarDialAceptarProveedor(jsonProveedor);
            }
             else if(ACTION_MI_UBICACION.equals(intent.getAction())) {
                mostrarMiUbicacion();
             }
             else if(ACTION_EMPIEZA_VIAJE.equals(intent.getAction()) && mActualState == mapState.YUBER_EN_CAMINO) {
                displayView(mapState.YUBER_TRABAJANDO);
            }
            else if(ACTION_TERMINO_VIAJE.equals(intent.getAction())) {
                String jsonDatosViaje = intent.getStringExtra("DATOS_VIAJE");
                mostrarViajeFinalizado(jsonDatosViaje);
            }
            else if(ACTION_CALIFICAR_VIAJE.equals(intent.getAction())) {
                displayView(mapState.ELIGIENDO_ORIGEN);
                String puntosViaje = intent.getStringExtra("PUNTAJE_VIAJE");
                enviarPuntaje(puntosViaje);
            }
            else if(ACTION_UBICACION_YUBER.equals(intent.getAction())) {
                String jsonUbicacion = intent.getStringExtra("UBICACION");
                mostrarUbicacionYuber(jsonUbicacion);
            }
            else if(ACTION_CONTADOR_FINALIZO.equals(intent.getAction())) {
                finTemporizador();
            }
        }
    };

    private void mostrarMiUbicacion(){
        // IMPLEMENTAR LO DE ARRIBA EMPIEZA_VIAJE
        LatLng myActualLatLng;
        if(mCurrentLocation!= null)
            myActualLatLng = new LatLng( mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude() );//new LatLng(-34.9, -56.16);
        else
            myActualLatLng = new LatLng(-34.9, -56.16);

        if (mOrigenMarker != null)
            mOrigenMarker.remove();

        MarkerOptions options;
        options = new MarkerOptions().position(myActualLatLng);
        options.title(getAddressFromLatLng(myActualLatLng));
        options.icon(BitmapDescriptorFactory.defaultMarker());
        mOrigenMarker = googleMap.addMarker(options);

        textoUbicacionOrigen = (TextView) actualFragment.getView().findViewById(R.id.textUbicacionOrigen);
        textoUbicacionOrigen.setText(getAddressFromLatLng(myActualLatLng));

        // Llevar a la posicion actual
        CameraPosition position = CameraPosition.builder()
                .target(myActualLatLng)
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);
    }

    @Override
    public void onMapReady(GoogleMap googleMapParam) {
        googleMap = googleMapParam;
        LatLng myLocatLatLng;
        LatLng mdeoLatLng = new LatLng(-34, -56);
        Location myLocation = null;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Check Permissions Now
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);

        }
        else {
            // permission has been granted, continue as usual
            myLocation =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        // Add a marker in Montevideo and move the camera
        if (myLocation == null){
            myLocatLatLng = mdeoLatLng;
        }
        else{
            myLocatLatLng = new LatLng( myLocation.getLatitude(), myLocation.getLongitude());
        }


        initListeners();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initListeners() {
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {


        } else {
            // permission has been granted, continue as usual
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }


    /*
        podria usarse para hallar la velociad y mandarlo?
        http://www.androidtutorialpoint.com/intermediate/android-map-app-showing-current-location-android/
        */

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        /*

    */
        initCamera(mCurrentLocation);
    }

    //gennymotion
// jwt token
    private void mostrarUbicacionYuber(String jsonDataUbicacion) {
        Log.d(TAG, "Adentro de mostrarUbicacionYuber" + jsonDataUbicacion);

        if (mActualState == mapState.YUBER_EN_CAMINO){

            JSONObject dataUbicacion = null;
            double latitud = 0;
            double longitud = 1;
            try {
                dataUbicacion = new JSONObject(jsonDataUbicacion);
                latitud = dataUbicacion.getDouble("latitud");
                longitud = dataUbicacion.getDouble("longitud");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Adentro de longitud" + latitud);
            LatLng latLng = new LatLng(latitud,longitud);

            //  mUbicacionYuberProveedor = googleMap.addMarker(new MarkerOptions().position(latLng).title("Ubicacion Yuber"));

            if (mUbicacionYuberProveedor != null)
                mUbicacionYuberProveedor.remove();

            MarkerOptions options;
            options = new MarkerOptions().position(latLng);
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.customs));
            mUbicacionYuberProveedor = googleMap.addMarker(options);
            mUbicacionYuberProveedor.setTitle("Ubicacion Yuber");


            /*
            JSONObject dataUbicacion = new JSONObject(jsonDataUbicacion);
            LatLng latLng = new LatLng(dataUbicacion.getDouble("latitud"),dataUbicacion.getDouble("longitud"));
            mUbicacionYuberProveedor = googleMap.addMarker(new MarkerOptions().position(latLng).title("Ubicacion Yuber"));
            if (mUbicacionYuberProveedor != null)
                mUbicacionYuberProveedor.remove();
            MarkerOptions options = new MarkerOptions().position(latLng);
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.customs));
            mUbicacionYuberProveedor.setTitle("Ubicacion Yuber");
            mUbicacionYuberProveedor = googleMap.addMarker(options);
            */
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //handle play services disconnecting if location is being constantly used
    }

    private void initCamera(Location location) {
        //improvisacion para ver si anda con ubicacio inventada
        LatLng myActualLatLng;
        if(location!= null)
            myActualLatLng = new LatLng( location.getLatitude(),location.getLongitude() );//new LatLng(-34.9, -56.16);
        else
            myActualLatLng = new LatLng(-34.9, -56.16);//new LatLng(-34.9, -56.16);
        // LatLng myActualLatLng = new LatLng(-34.9, -56.16);//new LatLng(-34.9, -56.16);
        //
        CameraPosition position = CameraPosition.builder()
                .target(myActualLatLng)
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        //marker inicial
        String direccion = "Origen";
        direccion = getAddressFromLatLng(myActualLatLng);

        try {
            mOrigenMarker = googleMap.addMarker(new MarkerOptions().position(myActualLatLng).title(direccion));
        }catch (Exception e){
            Log.d(TAG, "El marcador no se puso por el siguiente motivo: " + e);
        }

        textoUbicacionOrigen = (TextView) actualFragment.getView().findViewById(R.id.textUbicacionOrigen);
        textoUbicacionOrigen.setText(direccion);

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);


        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled( true );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(getActivity(), "Need your location!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Create a default location if the Google API Client fails. Placing location at Googleplex
        mCurrentLocation = new Location( "" );
        mCurrentLocation.setLatitude( -34.9 );
        mCurrentLocation.setLongitude( -56.16 );
        initCamera(mCurrentLocation);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText( getActivity(), "Clicked on marker", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        switch (mActualState) {
            case ELIGIENDO_ORIGEN:
                textoUbicacion = (TextView) actualFragment.getView().findViewById(R.id.textUbicacionOrigen);
                MarkerOptions options;
                if (mOrigenMarker != null)
                    mOrigenMarker.remove();
                options = new MarkerOptions().position(latLng);
                options.title(getAddressFromLatLng(latLng));
                options.icon(BitmapDescriptorFactory.defaultMarker());
                mOrigenMarker = googleMap.addMarker(options);
                textoUbicacion.setText(getAddressFromLatLng(latLng));
                break;
            default:
                break;
        }
    }

    private String getAddressFromLatLng( LatLng latLng ) {
        Geocoder geocoder = new Geocoder( getActivity() );
        String address = "";
        try {
            address =geocoder
                    .getFromLocation( latLng.latitude, latLng.longitude, 1 )
                    .get( 0 ).getAddressLine( 0 ) ;
        } catch (Exception e ) {
            // this is the line of code that sends a real error message to the  log
            Log.e("ERROR", "ERROR IN CODE: " + e.toString());
            // this is the line that prints out the location in the code where the error occurred.
            e.printStackTrace();
            return "ERROR_IN_CODE";
        }
        return address;
    }

    private void displayView(mapState estado) {
        mActualState = estado;
        TextView textoDialogoChico;
        switch (estado) {
            case ELIGIENDO_ORIGEN:
                mButtonLlammarUber.setEnabled(true);
                mButtonLlammarUber.setText("SOLICITAR");
                if (mOrigenMarker != null){
                    mOrigenMarker.remove();
                    mOrigenMarker = null;
                }
                if (mDestinationMarker != null){
                    mDestinationMarker.remove();
                    mDestinationMarker = null;
                }
                if (mUbicacionYuberProveedor != null){
                    mUbicacionYuberProveedor.remove();
                    mUbicacionYuberProveedor = null;
                }
                actualFragment = new MapCallYuberFragment();
                break;
            case BUSCANDO_YUBER:
                // Se modifica el icono del Marker
                LatLng latLng = mOrigenMarker.getPosition();;
                if (mOrigenMarker != null)
                    mOrigenMarker.remove();
                MarkerOptions options;
                options = new MarkerOptions().position(latLng);
                options.title(getAddressFromLatLng(latLng));
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_52));
                mOrigenMarker = googleMap.addMarker(options);
                mOrigenMarker.setTitle("Origen");
                empezarContador();

                actualFragment = new MapWaitYFragment();
                break;
            case YUBER_EN_CAMINO:
                actualFragment = new MapYubConfirmadoFragment();
                break;
            case YUBER_TRABAJANDO:
                mButtonLlammarUber.setEnabled(false);
                textoDialogoChico = (TextView) actualFragment.getView().findViewById(R.id.textEstadoFragmentoYubConfir);
                textoDialogoChico.setText("Trabajando...");
                break;
            default:
                break;
        }
        if (actualFragment != null) {
            FragmentManager fragmentManager = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.containerMiniFrameMapFragment, actualFragment);
            fragmentTransaction.commit();
        }
    }

    private void empezarContador() {
        new Thread(new Runnable() {
            public void run() {
                TemporizadorProv temp = new TemporizadorProv();
                temp.esperarXsegundos(60);
                Intent intent = new Intent(ACTION_CONTADOR_FINALIZO);
                try{
                    LocalBroadcastManager.getInstance(getActivity().getBaseContext()).sendBroadcast(intent);
                }catch (Exception e){
                    Log.d(TAG,"Se rompio al finalizar el contador del MapFragment" + e);
                }
            }
        }).start();
    }

    private void finTemporizador() {
        if (mActualState == mapState.BUSCANDO_YUBER){
            cancelarServicioOnline();
            Toast.makeText(getActivity().getApplicationContext(), "Ningun Yuber acepto tu solicitud", Toast.LENGTH_LONG).show();
        }
    }

    private View.OnClickListener createListenerBottomButton(){
        View.OnClickListener clickListtener = new View.OnClickListener() {
            public void onClick(View v) {
                // Estados del boton en funcion de los clicks
                switch (mActualState) {
                    case ELIGIENDO_ORIGEN:
                        if (mOrigenMarker != null) {
                            displayView(mapState.BUSCANDO_YUBER);
                            pedirServicio();
                        } else
                            Toast.makeText(getActivity().getApplicationContext(), "Por favor, elija el origen del viaje", Toast.LENGTH_LONG).show();
                        //Se pidio un Yuber
                        break;
                    case BUSCANDO_YUBER:
                        //Se cancelo el Yuber pedido
                        cancelarServicioOnline();
                        break;
                    case YUBER_EN_CAMINO:
                        //Se cancelo el Yuber pedido
                        cancelarServicioOnline();
                        break;
                    default:
                        break;
                }
            }
        };
        return clickListtener;
    }

    private void mostrarViajeFinalizado(String datosViaje){
        Bundle args = new Bundle();
        args.putString("datosViaje", datosViaje);
        FragmentDialogFinViaje dialogoFinViajeYCalificar = new FragmentDialogFinViaje();
        dialogoFinViajeYCalificar.setArguments(args);
        dialogoFinViajeYCalificar.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    private void mostrarDialAceptarProveedor(String jProveedor){
        Bundle args = new Bundle();
        args.putString("datos", jProveedor);
        FragmentDialogYuberDisponible newFragmentDialog = new FragmentDialogYuberDisponible();
        newFragmentDialog.setArguments(args);
        newFragmentDialog.setCancelable(false);
        newFragmentDialog.show(getActivity().getSupportFragmentManager(), "TAG");
    }

    private void pedirServicio(){
        mButtonLlammarUber.setEnabled(false);
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/PedirServicio";
        JSONObject obj = new JSONObject();
        try {

            JSONObject jsonOrigen = new JSONObject();
            jsonOrigen.put("longitud", mOrigenMarker.getPosition().longitude);
            jsonOrigen.put("latitud", mOrigenMarker.getPosition().latitude);
            jsonOrigen.put("estado", "Ok");

            MainActivity mainActivity = (MainActivity) getActivity();

            obj.put("correo", mainActivity.getEmailSession());
            obj.put("servicioId", mIdServicioEnUso);
            obj.put("ubicacion", jsonOrigen);


        } catch (JSONException e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        ByteArrayEntity entity = null;
        try {
            entity = new ByteArrayEntity(obj.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        RequestHandle Rq = client.post(null, url, entity, "application/json", new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                if (response.contains("id") ){
                    try {
                        // JSON Object
                        JSONObject obj = new JSONObject(response);
                        int idString = obj.getInt("id");
                        guardarIdInstanciaServicio(idString);
                        mButtonLlammarUber.setText("CANCELAR");
                        //Toast.makeText(getActivity().getApplicationContext(), "Se pidio el servicio con exito", Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        Toast.makeText(getActivity().getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    }
                }else{
                    displayView(mapState.ELIGIENDO_ORIGEN);
                    Toast.makeText(getActivity().getApplicationContext(), "No hay Yuber disponibles en su zona", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Throwable error, String content){
                displayView(mapState.ELIGIENDO_ORIGEN);
                if(statusCode == 404){
                    Toast.makeText(getActivity().getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }else if(statusCode == 500){
                    Toast.makeText(getActivity().getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity().getApplicationContext(), "Unexpected Error occured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
        mButtonLlammarUber.setEnabled(true);

    }


    private void cancelarServicioOnline(){

        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Cliente/CancelarPedido/" + mIdInstanciaServicio;
        AsyncHttpClient client = new AsyncHttpClient();
        RequestHandle Rq = client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                cancelarServicio();
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

    }

    private void cancelarServicio() {
        displayView(mapState.ELIGIENDO_ORIGEN);
    }

    private void guardarIdInstanciaServicio(int id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(InstanciaServicioIDKey, id);
        editor.commit();
        mIdInstanciaServicio = id;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        mCurrentLocation = location;


        /* lo que hace abajo es tirar un marcador por cada vez que se mueve....
        Marker mCurrLocationMarker = null;

        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.textNombreServicio("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = googleMap.addMarker(markerOptions);

        //move googleMap camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
*/

          /*
        //SE QUEDA LO DE ABAJO? O SE DEBERIA IR? XXX
        //stop location updates ---> ESTO PARA EL LISTENER de cuando se mueve el GPS
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

*/

    }

    public void enviarPuntaje(String puntaje){
        String url = "http://" + Ip + ":" + Puerto + "/YuberWEB/rest/Proveedor/PuntuarProveedor/" + puntaje + ",-," + mIdInstanciaServicio;
        Log.d(TAG,"LA URI ES " + url);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(null, url, new AsyncHttpResponseHandler(){
            @Override
            public void onSuccess(String response) {
                //Toast.makeText(getActivity().getApplicationContext(), "puntuo!", Toast.LENGTH_LONG).show();
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
    }






    //
    //      PRUEBA MAPA RUTA
    //      TODO TESTEAR
    //
    private ArrayList<LatLng> markerPoints;


    @Override
    public void onMapLongClick(LatLng point) {
        // Removes all the points from Google Map
        googleMap.clear();

        // Removes all the points in the ArrayList
        markerPoints.clear();

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;


        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("MAAAAAAL", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }



    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";



            if(result.size()<1){
                Toast.makeText(getActivity().getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }


            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    if(j==0){	// Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);

            }

            Toast.makeText(getActivity(), "Distancia: " + distance, Toast.LENGTH_SHORT).show();

            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);
        }
    }




}// FIN CLASS MapFragment