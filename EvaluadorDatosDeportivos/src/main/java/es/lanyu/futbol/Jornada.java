package es.lanyu.futbol;

import java.util.ArrayList;
import java.util.Collection;

import es.lanyu.comun.evento.Competicion;
import es.lanyu.comun.evento.Partido;

public class Jornada implements Comparable<Jornada> {
	private Competicion competicion;
	private int numero;
	private Collection<Partido> partidos;
	
	public Competicion getCompeticion() {
		return competicion;
	}

	public Collection<Partido> getPartidos() {
		return partidos;
	}

	public Jornada(int numero, Competicion competicion){
		this.numero = numero;
		this.competicion = competicion;
		partidos = new ArrayList<>();
	}
	
	@Override
	public String toString() {
		return competicion + " | Jornada Nº " + numero;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((competicion == null) ? 0 : competicion.hashCode());
		result = prime * result + numero;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Jornada other = (Jornada) obj;
		if (competicion == null) {
			if (other.competicion != null)
				return false;
		} else if (!competicion.equals(other.competicion))
			return false;
		if (numero != other.numero)
			return false;
		return true;
	}

	public void listarPartidos() {
		System.out.println(this + " (" + getPartidos().size() + ")");
		for(Partido partido : getPartidos()){
			System.out.println(partido.detallesDelPartido());
		}
	}

	@Override
	public int compareTo(Jornada o) {
		return ((Integer)this.numero).compareTo((Integer)o.numero);
	}
	
}
