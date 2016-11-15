package yuber.yuberClienteServicios.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import yuber.yuberClienteServicios.R;
import yuber.yuberClienteServicios.adapter.ServiciosAdapter;

public class ServiciosActivity extends AppCompatActivity {
    private List<Servicios> serviciosList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ServiciosAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servicios);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
                Toast.makeText(getApplicationContext(), servicio.getNombre() + " is selected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));



        prepareMovieData();
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
