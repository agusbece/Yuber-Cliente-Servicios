package yuber.yuberClienteServicios.activity;

public class Servicios {

    private int mID;
    private int mTarifaBase;
    private int mPrecioKM;
    private int mPrecioHora;
    private String mNombre;


    public Servicios() {
    }

    public Servicios(int id, int tarifaBase, int precioKM, String nombre) {
        mID = id;
        mTarifaBase = tarifaBase;
        mPrecioKM = precioKM;
        mNombre = nombre;
    }

    public int getID() {
        return mID;
    }

    public int getTarifaBase() {
        return mTarifaBase;
    }

    public int getPrecioPorKM() {
        return mPrecioKM;
    }

    public int getPrecioPorHora() {
        return mPrecioHora;
    }

    public String getNombre() {
        return mNombre;
    }


}
