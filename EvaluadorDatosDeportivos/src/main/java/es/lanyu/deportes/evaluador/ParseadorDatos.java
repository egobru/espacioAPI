package es.lanyu.deportes.evaluador;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import es.lanyu.comun.evento.Partido;
import es.lanyu.comun.suceso.Tarjeta.TipoTarjeta;
import es.lanyu.participante.Participante;

public class ParseadorDatos {

	private Map<String, String> clavesEquipos = new HashMap<>();
	private ConfiguracionParseadorDatos configuracion;
	
	public ConfiguracionParseadorDatos getConfiguracion() {
		return configuracion;
	}

	public ParseadorDatos (ConfiguracionParseadorDatos configuracion){
		this.configuracion = configuracion;
	}
	
	protected Participante registrarEquipo(String nombreDescarga){
		Participante equipo = Evaluador.getParticipante(clavesEquipos.get(nombreDescarga));
		
		//Si se permiten añadir equipos se añaden
		if(equipo == null) {
			if (configuracion.isParticipantesAmpliables()){
				equipo = Evaluador.generarParticipantePorNombre(nombreDescarga);
				clavesEquipos.put(nombreDescarga, equipo.getIdentificador());
				configuracion.getCompeticion().getParticipantes().add(equipo);
			}
			else
				Logger.getLogger(getClass().getName())
					.log(Level.WARNING, "No se ha encontrado Participante con nombre " + nombreDescarga);
		}
		
		return equipo;
	}
	
	public Collection<Partido> extraerPartidos() throws FileNotFoundException, IOException {
		return extraerPartidos(configuracion.getRuta());
	}
	
	private Collection<Partido> extraerPartidos (String ruta) throws FileNotFoundException, IOException{
		Collection<Partido> partidos = new ArrayList<>();
		
		try (BufferedReader buffer = new BufferedReader(new FileReader(ruta))) {
			
//			String cabecera = buffer.readLine();
//			GestorGenerico<MapaDeValores> gestor = new GestorGenerico<MapaDeValores>(MapaDeValores.class);
//			gestor.cargarEspecificacionesJSON(	EspecificacionStringSeparadoCaracter.class,
//												new File(".\\src\\datos\\Partidos.txt"));
//			MapaDeValores mapa = new Json().fromJson(MapaDeValores.class, new File(".\\src\\datos\\mapa.json"));
			
			//Quitar la primera linea de cabecera
			String linea = buffer.readLine();
			while ((linea = buffer.readLine()) != null){
				partidos.add(parsearPartidoDesdeString(linea));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return partidos;
	}
	
	public Partido parsearPartidoDesdeString(String linea) throws ParseException {
		MapaDeValores mapa = configuracion.getMapa();
		String[] campos = linea.split(",");
		Partido partido = new Partido(registrarEquipo(campos[mapa.local]), registrarEquipo(campos[mapa.visitante]));
		partido.setFecha(configuracion.getSimpleDateFormat().parse(campos[mapa.fecha]));
		
		//Goles
		int golesLocal = Integer.parseInt(campos[mapa.golesLocal]);
		int golesVisitante = Integer.parseInt(campos[mapa.golesVisitante]);
		partido.addGoles(golesLocal, golesVisitante);
		
		//Corners
		if(configuracion.parsearCorners()){
			int cornersLocal = Integer.parseInt(campos[mapa.cornersLocal]);
			int cornersVisitante = Integer.parseInt(campos[mapa.cornersVisitante]);
			partido.addCorners(cornersLocal, cornersVisitante);
		}
		
		//Tarjetas
		if(configuracion.parsearTarjetas()){
			//Amarillas
			int tarjetasLocal = Integer.parseInt(campos[mapa.tarjetasAmarillasLocal]);
			int tarjetasVisitante = Integer.parseInt(campos[mapa.tarjetasAmarillasVisitante]);
			partido.addTarjetas(tarjetasLocal, tarjetasVisitante, TipoTarjeta.AMARILLA);
			//Rojas
			tarjetasLocal = Integer.parseInt(campos[mapa.tarjetasRojasLocal]);
			tarjetasVisitante = Integer.parseInt(campos[mapa.tarjetasRojasVisitante]);
			partido.addTarjetas(tarjetasLocal, tarjetasVisitante, TipoTarjeta.ROJA);
		}
		
		return partido;
	}
	
	public static class MapaDeValores {
		public int fecha = 0, local = 0, visitante = 0, golesLocal = 0, golesVisitante = 0,
				cornersLocal = 0, cornersVisitante = 0,
				tarjetasAmarillasLocal = 0, tarjetasAmarillasVisitante = 0,
				tarjetasRojasLocal = 0, tarjetasRojasVisitante = 0;
		
		public MapaDeValores () {}
	}
}
