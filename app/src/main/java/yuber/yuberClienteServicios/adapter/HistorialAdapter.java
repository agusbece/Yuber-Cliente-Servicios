package yuber.yuberClienteServicios.adapter;

/**
 * Created by Agustin on 28-Oct-16.
 */
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import yuber.yuberClienteServicios.R;
import yuber.yuberClienteServicios.activity.Historial;


public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.MyViewHolder> {


    public static final String TAG = "HISTORIAL ADAPTER";
    private List<Historial> historialList;

    String titulo;
    String subTitulo;
    String fecha;
    //Datos que se consumen del JSON

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView titulo, subtitulo, año;

        public MyViewHolder(View view) {
            super(view);
            titulo = (TextView) view.findViewById(R.id.titulo);
            subtitulo = (TextView) view.findViewById(R.id.subtitulo);
            año = (TextView) view.findViewById(R.id.ano);
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public HistorialAdapter(List<Historial> myDataset) {
        historialList = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistorialAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_list_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Historial historial = historialList.get(position);

        String[] splitDir = historial.getDireccionOrigen().split(" ");
        String numero = "";
        String calle = "";

        try{
            numero = splitDir[splitDir.length - 1];
        }catch (Exception e){
            Log.d(TAG, "Error parseando strings: " + e);
            numero = "";
        }
        try{
            calle = splitDir[splitDir.length - 2];
        }catch (Exception e){
            Log.d(TAG, "Error parseando strings: " + e);
            calle = "";
        }
        String Direccion = calle + " " + numero;

        String tiempo = historial.getDistancia();
        Log.d(TAG,"El tiempo de historial: " + tiempo);
        try {
            float x = Float.valueOf(tiempo);
            if (x < 0) {
                x = x * -1;
            }
            int t = (int) x;
            tiempo = obtenerTiempo(t);
        }catch (Exception e){        }

        titulo = "Ubicación: " + Direccion;
        subTitulo = "Tiempo: " + tiempo + "   Costo: $" + historial.getCosto();
        fecha = historial.getFecha();
        String[] fechaSplit = fecha.split(" ");

        holder.titulo.setText(titulo);
        holder.subtitulo.setText(subTitulo);
        holder.año.setText(fechaSplit[0]);
    }

    @Override
    public int getItemCount() {
        return historialList.size();
    }

    public String obtenerTiempo(int tiempo){
        Log.d(TAG,"El tiempo es: " + tiempo);

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


}