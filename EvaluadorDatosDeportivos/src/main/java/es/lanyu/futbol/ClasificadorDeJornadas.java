package es.lanyu.futbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import es.lanyu.comun.evento.Competicion;
import es.lanyu.comun.evento.Partido;
import es.lanyu.participante.Participante;

public class ClasificadorDeJornadas {

	private Collection<Jornada> jornadas;
	//Para que las jornadas se mantengan ordenadas uso el TreeMap
	private Map<Jornada, Collection<Participante>> equiposPorJugarEnJornadas = new TreeMap<>();
	private Competicion competicion;
	private int numeroJornada;
	
	public Collection<Jornada> getJornadas() {
		return jornadas;
	}

	public ClasificadorDeJornadas(Competicion competicion) {
		super();
		this.competicion = competicion;
		this.numeroJornada = 1;
	}

	public Collection<Jornada> dividirJornadas(Collection<Partido> partidos){
		//Inicio una nueva coleccion de jornadas
		jornadas = new ArrayList<>();
		//Ordeno los partidos por fecha de antiguo a reciente
		List<Partido> partidosOrdenados = new ArrayList<>(partidos);
		partidosOrdenados.sort((p1, p2) -> p2.getFecha().compareTo(p1.getFecha()));
		Jornada jornada = null;
		
		for(Partido partido : partidos){
			Jornada ultimaJornada = addPartido(jornada, partido);
			//Actualizo la jornada en su caso
			if(jornada != ultimaJornada)
				jornada = ultimaJornada;
		}
		
		return jornadas;
	}
	
	//Metodo base que guarda el partido en la jornada que corresponda
	public Jornada addPartido(Jornada jornada, Partido partido){
		
//		if(partido.getLocal().getNombre().equals("West Brom") && partido.getVisitante().getNombre().equals("Arsenal"))
//			System.out.println("Que pasa con el partido?");
		
		
		//Busco si hay alguna jornada anterior donde ubicar el partido
		if(!buscarJornadaParaPartido(partido))
			//Hay que iniciar jornada
			jornada = partidoIniciaJornada(partido);
		
		return jornada;
	}
	
	private boolean buscarJornadaParaPartido(Partido partido){
		boolean exito = false;
		for(Jornada jornada : equiposPorJugarEnJornadas.keySet()){
			if(losEquiposNoHanJugadoEnJornada(jornada, partido.getParticipantes()))
				exito = registrarPartidoEnJornada(jornada, partido);
			
			if(exito)
				break;
		}
		
		return exito;
	}
	
	private boolean losEquiposNoHanJugadoEnJornada(Jornada jornada, Collection<Participante> equipos){
		return equiposPorJugarEnJornadas.get(jornada).containsAll(equipos);
	}
	
	//Registra el partido en una jornada y actualiza el mapa de seguimiento de equipos por jugar
	private boolean registrarPartidoEnJornada(Jornada jornada, Partido partido){
		Collection<Participante> equiposPorJugar = equiposPorJugarEnJornadas.get(jornada);
		boolean anadido = equiposPorJugar.removeAll(partido.getParticipantes());
		
		//Si ya no quedan equipos por jugar en una jornada, se deja de seguir
		if(equiposPorJugar.size() == 0)
			equiposPorJugarEnJornadas.remove(jornada);
		
		anadido &= jornada.getPartidos().add(partido);
		
		return anadido;
	}
	
	//Inicia la jornada y guarda el partido
	private Jornada partidoIniciaJornada(Partido partido){
		Jornada jornada = iniciarJornada();
		registrarPartidoEnJornada(jornada, partido);
		
		return jornada;
	}
	
	//Inicia una jornada y actualiza el mapa de seguimiento de enfrentamientos
	private Jornada iniciarJornada(){
		Jornada jornada = new Jornada(numeroJornada, competicion);
//		if(numeroJornada == 28)
//			System.out.println("Estoy en la " + numeroJornada);
		equiposPorJugarEnJornadas.put(jornada, new ArrayList<>(jornada.getCompeticion().getParticipantes()));
		jornadas.add(jornada);
		numeroJornada++;
		
		return jornada;
	}
}
