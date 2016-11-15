package yuber.yuberClienteServicios.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import yuber.yuberClienteServicios.R;
import yuber.yuberClienteServicios.activity.Servicios;

public class ServiciosAdapter extends RecyclerView.Adapter<ServiciosAdapter.MyViewHolder> {

    private List<Servicios> mServiciosList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textNombreServicio;
       // public TextView textNombreServicio, year, genre;

        public MyViewHolder(View view) {
            super(view);
            textNombreServicio = (TextView) view.findViewById(R.id.titulo);
            //genre = (TextView) view.findViewById(R.id.genre);
            //year = (TextView) view.findViewById(R.id.year);
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public ServiciosAdapter(List<Servicios> myDataset) {

        mServiciosList = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ServiciosAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.servicios_list_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Servicios servicio = mServiciosList.get(position);
        holder.textNombreServicio.setText(servicio.getNombre());
        //holder.genre.setText(movie.getGenre());
       // holder.year.setText(movie.getYear());
    }

    @Override
    public int getItemCount() {
        return mServiciosList.size();
    }
}