package es.lanyu.deportes.evaluador;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.StreamHandler;

import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonSerializer;
import com.esotericsoftware.jsonbeans.JsonValue;
import com.esotericsoftware.jsonbeans.OutputType;

import es.lanyu.commons.servicios.entidad.CargadorIdentificables;
import es.lanyu.commons.servicios.entidad.ServicioEntidad;
import es.lanyu.commons.servicios.entidad.ServicioEntidadImpl;
import es.lanyu.comun.evento.Competicion;
import es.lanyu.comun.evento.Partido;
import es.lanyu.deportes.io.CargadorIdentificablesJson;
import es.lanyu.futbol.ClasificadorDeJornadas;
import es.lanyu.futbol.Jornada;
import es.lanyu.participante.Participante;

public class EvaluadorCopia {
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
//		rutaArchivo = "\\E0.csv";//Premier - FALLA
		rutaArchivo = "\\D1.csv";//Bundesliga
//		rutaArchivo = "\\D2.csv";//Bundesliga2
//		rutaArchivo = "\\F1.csv";//Francia
//		rutaArchivo = "\\F2.csv";//Francia2
//		rutaArchivo = "\\N1.csv";//Eredivisie
//		rutaArchivo = "\\B1.csv";//Jupiler Belgica
//		rutaArchivo = "\\P1.csv";//Portugal - FALLA
//		rutaArchivo = "\\G1.csv";//Grecia - FALLA
//		rutaArchivo = "\\I1.csv";//Serie A - FALLA
		
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
			
			Json json = new Json(OutputType.javascript);
//			json.toJson(mapaPrueba, new File(".\\src\\datos\\mapaMercados"));
			
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
//			Logger.getLogger(ParseadorDatos.class.getName()).addHandler(handler);
			
			json.setSerializer(Partido.class, new JsonSerializer<Partido>() {

				@Override
				public void write(Json json, Partido partido, Class knownType) {
					json.writeObjectStart();
					json.writeValue("local", partido.getLocal().getIdentificador());
					json.writeValue("visitante", partido.getVisitante().getIdentificador());
					//TODO EL RESTO
					json.writeObjectEnd();
				}

				@Override
				public Partido read(Json json, JsonValue jsonData, Class type) {
					String idLocal = jsonData.getString("local");
					String idVisitante = jsonData.getString("visitante");
					//TODO EL RESTO
//					return new Partido(Participante.getIdentificable(idLocal), Participante.getIdentificable(idVisitante));
					return new Partido(getParticipante(idLocal), getParticipante(idVisitante));
				}

			});
			
//			GestorGenerico<Partido> gestor = new GestorGenerico<Partido>(Partido.class);
//			specs = gestor.cargarEspecificacionesJSON(
//					EspecificacionStringSeparadoCaracter.class,
//					new File(".\\src\\datos\\Partidos.txt"));
//			List<Partido> pruebaPartidos = gestor.cargarDatosDesdeArchivo(new File(rutaArchivo));
			
			
//			for(Partido partido : partidos){
//				System.out.println(partido.detallesDelPartido());
//			}
//			System.out.println(partidos.size() + " partidos en total");
			
			
			
//			List<Participante> partOrdenados = new ArrayList<>(Participante.getParticipantesRegistrados());
//			partOrdenados.sort((o1, o2) -> o1.toString().compareTo(o2.toString()));
//			System.out.println(partOrdenados);
			
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
		
//		//MEDIA DE EMPATES
//		float numero = 0;
//		for(Jornada j : jornadas){
//			for(Partido p : j.getPartidos()){
//				if(p.getGanador() == null)
//					numero++;
//			}
//		}
//		System.out.println("Media de empates " + numero/jornadas.size());
//		
//		//JORNADAS SIN EMPATES
//		numero = 0;
//		for(Jornada j : jornadas){
//			boolean hayEmpate = false;
//			for(Partido p : j.getPartidos()){
//				if(p.getGanador() == null){
//					hayEmpate = true;
//					numero++;
//					break;
//				}
//			}
//			if(!hayEmpate)
//				j.listarPartidos();
//		}
//		System.out.println("Jornadas sin empate " + (jornadas.size() - numero));
	}

}
