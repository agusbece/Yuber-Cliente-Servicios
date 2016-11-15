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

        titulo = "Destino: " + Direccion;
        subTitulo = "Distancia: " + historial.getDistancia() + "Km   Costo: $" + historial.getCosto();
        fecha = historial.getFecha();

        holder.titulo.setText(titulo);
        holder.subtitulo.setText(subTitulo);
        holder.año.setText(fecha);
    }

    @Override
    public int getItemCount() {
        return historialList.size();
    }

}