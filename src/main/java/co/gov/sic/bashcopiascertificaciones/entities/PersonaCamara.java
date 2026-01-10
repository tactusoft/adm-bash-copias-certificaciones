package co.gov.sic.bashcopiascertificaciones.entities;

import java.time.LocalDate;

public class PersonaCamara {

    private Long idCamaraComercio;
    private String nombre;
    private String tipoDocumento;
    private Integer numeroDocumento;
    private String numeroDocumentoDesc;
    private String cargo;
    private String numeroActaDelegacion;
    private String nombreActaDelegacion;
    private LocalDate fechaActaDelegacion;

    public String getNombre() {
        return nombre;
    }

    public void setIdCamaraComercio(Long val) {
        idCamaraComercio = val;
    }

    public Long getIdCamaraComercio() {
        return idCamaraComercio;
    }

    public void setNombre(String val) {
        nombre = val;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String val) {
        tipoDocumento = val;
    }

    public Integer getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(Integer val) {
        numeroDocumento = val;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String val) {
        cargo = val;
    }

    public String getNumeroActaDelegacion() {
        return numeroActaDelegacion;
    }

    public void setNumeroActaDelegacion(String val) {
        numeroActaDelegacion = val;
    }

    public LocalDate getFechaActaDelegacion() {
        return fechaActaDelegacion;
    }

    public void setFechaActaDelegacion(LocalDate val) {
        fechaActaDelegacion = val;
    }

    public String getNumeroDocumentoDesc() {
        return this.numeroDocumentoDesc;
    }

    public void setNumeroDocumentoDesc(String val) {
        this.numeroDocumentoDesc = val;
    }

    public String getNombreActaDelegacion() {
        return this.nombreActaDelegacion;
    }

    public void setNombreActaDelegacion(String val) {
        this.nombreActaDelegacion = val;
    }
}
