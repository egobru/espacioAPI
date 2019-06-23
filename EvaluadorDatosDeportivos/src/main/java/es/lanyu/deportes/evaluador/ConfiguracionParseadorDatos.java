package es.lanyu.deportes.evaluador;
import java.io.File;
import java.text.SimpleDateFormat;

import com.esotericsoftware.jsonbeans.Json;

import es.lanyu.comun.evento.Competicion;

public class ConfiguracionParseadorDatos {
	private Competicion competicion;
	private String ruta;
	private ParseadorDatos.MapaDeValores mapa;
	private SimpleDateFormat sDateFormat;
	private boolean parsearCorners;
	private boolean parsearTarjetas;
	private boolean parsearCuotas;
	private boolean participantesAmpliables;
	
	public Competicion getCompeticion() {
		return competicion;
	}
	
	public String getRuta() {
		return ruta;
	}
	
	public ParseadorDatos.MapaDeValores getMapa() {
		return mapa;
	}
	
	public SimpleDateFormat getSimpleDateFormat() {
		return sDateFormat;
	}

	public boolean parsearCorners() {
		return parsearCorners;
	}

	public boolean parsearTarjetas() {
		return parsearTarjetas;
	}

	public boolean parsearCuotas() {
		return parsearCuotas;
	}

	public boolean isParticipantesAmpliables() {
		return participantesAmpliables;
	}

	public void setParticipantesAmpliables(boolean participantesAmpliables) {
		this.participantesAmpliables = participantesAmpliables;
	}

	public ConfiguracionParseadorDatos(Competicion competicion, String rutaArchivo, String rutaMapa) {
		this(competicion, rutaArchivo, rutaMapa, false, false, false);
	}
	
	public ConfiguracionParseadorDatos(Competicion competicion, String rutaArchivo, String rutaMapa, boolean corners, boolean tarjetas, boolean cuotas) {
		this.competicion = competicion;
		this.ruta = rutaArchivo;
		if(rutaMapa != null)
			mapa = new Json().fromJson(ParseadorDatos.MapaDeValores.class, new File(rutaMapa));
		sDateFormat = new SimpleDateFormat("dd/MM/yy");
		parsearCorners = corners;
		parsearTarjetas = tarjetas;
		parsearCuotas = cuotas;
	}
}
