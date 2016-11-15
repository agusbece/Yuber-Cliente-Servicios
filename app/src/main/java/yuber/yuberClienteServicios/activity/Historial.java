package yuber.yuberClienteServicios.activity;

public class Historial {

    private String Comentario;
    private String Puntaje;
    private String Costo;
    private String Distancia;
    private String DireccionO;
    private String DireccionD;
    private String Fecha;

    public Historial() {
    }

    public Historial(String comentario, String puntaje, String costo, String distancia, String direccionO,String direccionD, String fecha) {
        Comentario = comentario;
        Puntaje = puntaje;
        Costo = costo;
        Distancia = distancia;
        DireccionO = direccionO;
        DireccionD = direccionD;
        Fecha = fecha;
    }

    public String getComentario() {
        return Comentario;
    }

    public void setComentario(String comentario) {
        Comentario = comentario;
    }

    public String getPuntaje() {
        return Puntaje;
    }

    public void setPuntaje(String puntaje) {
        Puntaje = puntaje;
    }

    public String getCosto() {
        return Costo;
    }

    public void setCosto(String costo) {
        Costo = costo;
    }

    public String getDistancia() {
        return Distancia;
    }

    public void setDistancia(String distancia) {
        Distancia = distancia;
    }

    public String getDireccionOrigen() {
        return DireccionO;
    }

    public void setDireccionOrigen(String direccion) {
        DireccionO = direccion;
    }

    public String getDireccionDestino() {
        return DireccionD;
    }

    public void setDireccionDestino(String direccion) {
        DireccionD = direccion;
    }

    public String getFecha() {
        return Fecha;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }

    @Override
    public String toString() {
        return "{" +
                "\"Comentario\":\"" + Comentario + '\"' +
                ",\"Puntaje\":\"" + Puntaje + '\"' +
                ",\"Costo\":\"" + Costo + '\"' +
                ",\"Distancia\":\"" + Distancia + '\"' +
                ",\"Fecha\":\"" + Fecha + '\"' +
                ",\"DireccionO\":\"" + DireccionO + '\"' +
                ",\"DireccionD\":\"" + DireccionD + '\"' +
                '}';
    }

}
