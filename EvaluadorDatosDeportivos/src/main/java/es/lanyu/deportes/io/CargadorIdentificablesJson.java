package es.lanyu.deportes.io;

import es.lanyu.commons.identificable.AbstractNombrable;
import es.lanyu.commons.identificable.GestorIdentificables;
import es.lanyu.commons.servicios.entidad.CargadorIdentificables;

public class CargadorIdentificablesJson implements CargadorIdentificables {

	@Override
	public <T extends AbstractNombrable, S extends T> void cargarNombrables(
						String rutaArchivo, Class<T> claseMapa,
						Class<S> claseEspecializacion, GestorIdentificables gestor) {
		Utils.cargarNombrables(rutaArchivo, claseMapa, claseEspecializacion, gestor);
	}

}
