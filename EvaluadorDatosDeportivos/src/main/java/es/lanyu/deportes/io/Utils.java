package es.lanyu.deportes.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonSerializer;
import com.esotericsoftware.jsonbeans.JsonValue;
import es.lanyu.commons.identificable.AbstractNombrable;
import es.lanyu.commons.identificable.GestorIdentificables;
import es.lanyu.commons.identificable.Identificable;
//import es.lanyu.commons.serial.EspecificacionString;
//import es.lanyu.commons.serial.EspecificacionStringSeparadoCaracter;

/**Clase de utilidades para la carga de {@link Identificables} desde archivos JSON.
 * @author <a href="https://github.com/Awes0meM4n">Awes0meM4n</a>
 * @version 1.0
 * @since 1.0
 */
class Utils {
	
	/**Carga los recursos de tipo {@link Identificable} almacenados
	 * en cada fila del fichero {@code rutaArchivo} en formato .json
	 * y los almacena en el {@code gestor} como un recurso del tipo {@code claseMapa}.
	 * Puede especializarse el recurso a guardar con {@code claseEspecializacion}
	 * si esta serializado un subtipo de {@code claseMapa}
	 * @param <K> Tipo del identificador del {@code Identificable}
	 * @param <T> Tipo de la {@code claseMapa}
	 * @param <S> Tipo de la especializacion. Puede ser el igual a {@code T}
	 * @param json Serializador de tipo {@link Json} que implementa la lectura y escritura en formato json
	 * @param rutaArchivo ruta al archivo .json
	 * @param claseMapa Clase que sirve para mapear el recurso (normalmente la mas generica)
	 * @param claseEspecializacion Clase subtipo de claseMapa para gestionar recursos mas especializados
	 * @param gestor Gestor que administrara los recursos a cargar
	 */
	static <K extends Comparable<K>, T extends Identificable<K>, S extends T> void cargarIdentificables(
												Json json,
												String rutaArchivo,
												Class<T> claseMapa,
												Class<S> claseEspecializacion,
												GestorIdentificables gestor) {

	    String linea = null;
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(rutaArchivo), "UTF-8"))){
			while((linea = buffer.readLine()) != null){
				try {
				    T objeto = json.fromJson(claseEspecializacion, linea);
				    gestor.addIdentificable(claseMapa, objeto);
				} catch (Exception e) {
					Logger.getLogger(Utils.class.getName()).log(
							Level.WARNING, "Error parseando " + linea + ": Se omite");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**Facilita {@link Utils#cargarIdentificables(Json, String, Class, Class, GestorIdentificables)}
	 * proporcionando un serializador por defecto para el subtipos de {@link AbstractNombrable}
	 * ({@code id} y {@code nombre}.
	 * @param <T> Tipo de la {@code claseMapa}
	 * @param <S> Tipo de la especializacion. Puede ser el igual a {@code T}
	 * @param rutaArchivo ruta al archivo .json
	 * @param claseMapa Clase que sirve para mapear el recurso (normalmente la mas generica)
	 * @param claseEspecializacion Clase subtipo de claseMapa para gestionar recursos mas especializados
	 * @param gestor Gestor que administrara los recursos a cargar
	 */
	static <T extends AbstractNombrable, S extends T> void cargarNombrables (
			String rutaArchivo,
			Class<T> claseMapa,
			Class<S> claseEspecializacion,
			GestorIdentificables gestor) {
		Json json = getSerializador(claseMapa);
		cargarIdentificables(json, rutaArchivo, claseMapa, claseEspecializacion, gestor);
	}
	
	/**Serilizador por defecto para los {@link AbstractNombrable}
	 * @param <T> Subtipo de {@code AbstractNombrable}
	 * @param clase Subtipo de {@code AbstractNombrable}
	 * @return {@link Json} para el tipo pasado
	 */
	private static <T extends AbstractNombrable> Json getSerializador (Class<T> clase) {
		Json json = new Json();
		
		json.setSerializer(clase, new JsonSerializer<T>() {
			@SuppressWarnings("rawtypes")
			public void write (Json json, T nombrable, Class knownType) {
				json.writeObjectStart();
				json.writeValue("id", nombrable.getIdentificador());
				json.writeValue("nombre", nombrable.getNombre());
				json.writeObjectEnd();
			}
			
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public T read (Json json, JsonValue jsonData, Class type) {
				T nombrable;
				try {
					nombrable = (T) type.newInstance();
					nombrable.setIdentificador(jsonData.getString("id"));
					nombrable.setNombre(jsonData.getString("nombre"));
					return nombrable;
				} catch (InstantiationException e) {
					e.printStackTrace();
					return null;
				} catch (IllegalAccessException e) {
					e.printStackTrace();
					return null;
				}
			}
		});
		
		return json;
	}
	
	
//	//PROVIENE DE es.lanyu.commons.serial.EspecificacionStringSeparadoCaracter
//	
//	//Estos metodos tambien mejoran la entrada de EspecificacionString y solo para este tipo
//	//Se sobrecarga para dar facilidad de uso del metodo, aunque es una llamada simple
//	public static Collection<EspecificacionStringSeparadoCaracter> leerEspecificaciones2 (String ruta) {
//		return leerEspecificaciones2(new File(ruta));
//	}
//	
//	public static Collection<EspecificacionStringSeparadoCaracter> leerEspecificaciones2 (File archivo) {
//		Collection<EspecificacionStringSeparadoCaracter> specs = new ArrayList<>();
//		
//		try (BufferedReader buffer = new BufferedReader(new FileReader(archivo))){
//	         String strLinea;
//	         EspecificacionStringSeparadoCaracter spec;
//	         
//	         while ((strLinea = buffer.readLine()) != null)   {
//	        	 spec = deserializarEspecificacion2(strLinea);
//	        	 if (spec != null)
//	        		 specs.add(spec);
//	         }
//	    }
//		catch(Exception e) {
//	          e.printStackTrace();
//		}
//		
//		return specs;
//	}
//	
//	private static EspecificacionStringSeparadoCaracter deserializarEspecificacion2 (String linea) {
//		Json json = new Json(OutputType.javascript);
//		EspecificacionStringSeparadoCaracter spec = json.fromJson(EspecificacionStringSeparadoCaracter.class, linea);
//		
//		return spec;	
//	}
//	
//	//PROVIENE de GestorGenerico
//	//Cargar las especificaciones para una clase que se leera de un fichero de texto
//	public <E extends EspecificacionString> Collection<E> cargarEspecificacionesJSON2(Class<E> clase, File archivo) {
//		Collection<E> specs = new ArrayList<>();
//		try(BufferedReader buffer = new BufferedReader(new FileReader(archivo))){
//			Json json = new Json(OutputType.javascript);
//			String linea;
//			while((linea = buffer.readLine()) != null){
//				specs.add(json.fromJson(clase, linea));
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		//TODO Modificar
//		//especificaciones = specs;//De GestorGenerico
//		
//		return specs;
//	}
	
}
