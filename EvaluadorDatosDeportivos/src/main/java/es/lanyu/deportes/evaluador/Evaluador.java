package es.lanyu.deportes.evaluador;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.StreamHandler;

import es.lanyu.commons.servicios.entidad.CargadorIdentificables;
import es.lanyu.commons.servicios.entidad.ServicioEntidad;
import es.lanyu.commons.servicios.entidad.ServicioEntidadImpl;
import es.lanyu.comun.evento.Competicion;
import es.lanyu.comun.evento.Partido;
import es.lanyu.deportes.io.CargadorIdentificablesJson;
import es.lanyu.futbol.ClasificadorDeJornadas;
import es.lanyu.futbol.Jornada;
import es.lanyu.participante.Participante;

public class Evaluador {
	static Collection<Partido> partidos = new ArrayList<>();
	static String rutaCSVs = ".\\data\\CSVs";
	
	private static ServicioEntidad servicioEntidad = new ServicioEntidadImpl();
	
	static {
		ServicioEntidad.SERVICIO_ENTIDAD_LOG.setLevel(Level.WARNING);
		CargadorIdentificables cargador = new CargadorIdentificablesJson();
		cargador.cargarNombrables("data/participantes.json",
				Participante.class, Participante.class, servicioEntidad);
	}
	
	public static Collection<Participante> getParticipantesRegistrados(){
		return servicioEntidad.getElementosRegistradosDe(Participante.class);
	}
	
	public static Participante getParticipante(String id) {
		return servicioEntidad.getIdentificable(Participante.class, id);
	}
	
	public static Participante generarParticipantePorNombre(String nombre){
		return servicioEntidad.generarNombrable(Participante.class, nombre);
	}
	
	public static void main (String... args){
		
		String rutaArchivo;
//		rutaArchivo = "\\SP1.csv";//La Liga
		rutaArchivo = "\\D1.csv";//Bundesliga
		
		ConfiguracionParseadorDatos configuracionLaLiga = new ConfiguracionParseadorDatos(
				Competicion.LA_LIGA, rutaCSVs + "\\SP1.csv", rutaCSVs + "\\mapaDatos.json",
				true,//Leer Corners
				true,//Leer Tarjetas
				true);//Leer Cuotas
		ConfiguracionParseadorDatos configuracionBundesliga = new ConfiguracionParseadorDatos(
				Competicion.BUNDESLIGA, rutaCSVs + "\\D1.csv", rutaCSVs + "\\mapaDatos.json",
				true, true, false);
		
		ConfiguracionParseadorDatos configuracion = configuracionBundesliga;//configuracionLaLiga;
		
		try {
			
			ParseadorDatos parseadorDatos = new ParseadorDatos(configuracion);
			//Permitimos añadir nuevos equipos
			parseadorDatos.getConfiguracion().setParticipantesAmpliables(true);
			partidos = parseadorDatos.extraerPartidos();
			//Añado al Manchester United para pruebas
//			parseadorDatos.registrarEquipo("Man Utd");
			parseadorDatos.getConfiguracion().getCompeticion().getParticipantes().stream().forEach(p -> System.out.println(p));
			//Bloqueamos añadir nuevos equipos
//			parseadorDatos.getConfiguracion().setParticipantesAmpliables(false);
			Handler handler = new StreamHandler();
			handler.setLevel(Level.FINE);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Competicion competicion = configuracion.getCompeticion();
//		competicion.setParticipantes(Participante.getParticipantesRegistrados());
		competicion.setParticipantes(getParticipantesRegistrados());
		ClasificadorDeJornadas clasificadorDeJornadas = new ClasificadorDeJornadas(competicion);
		Collection<Jornada> jornadas = clasificadorDeJornadas.dividirJornadas(partidos);
		
		for(Jornada j : jornadas){
			j.listarPartidos();
			System.out.println();//j + " (" + j.getPartidos().size() + ")");
		}
		
	}

}
