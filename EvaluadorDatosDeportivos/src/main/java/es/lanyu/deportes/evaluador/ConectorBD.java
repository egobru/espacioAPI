package es.lanyu.deportes.evaluador;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import es.lanyu.commons.config.Propiedades;
import es.lanyu.comun.evento.Competicion;
import es.lanyu.comun.evento.Evento;
import es.lanyu.comun.evento.Partido;
import es.lanyu.comun.suceso.Gol;
import es.lanyu.participante.Participante;

public class ConectorBD {
	
    Map<String, String> idsPorNombre;
    
    public static void main(String[] args) {
//        idsPorNombre = new HashMap<>();
//        Propiedades propiedades = new Propiedades("datos/mapaCSV.properties");
//        propiedades.forEach((k, v) -> idsPorNombre.put((String)v, (String)k));

        List<Participante> participantes = new ArrayList<>(Evaluador.getParticipantesRegistrados());
        
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (java.lang.ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        String url = "jdbc:postgresql://manny.db.elephantsql.com:5432/dekxhfqp";
        String username = "dekxhfqp";
        String password = "BIaAAWJ6GD25MyiDF2D_0JOobz8wOreb";

        try (Connection db = DriverManager.getConnection(url, username, password)) {
//            borrarTablas();
        	
//            PreparedStatement pst = db.prepareStatement("INSERT INTO participantes (id, nombre) VALUES (?, ?)");
//            db.setAutoCommit(false);
//            for(Participante p : participantes){
//            	pst.setString(1, p.getIdentificador());
//            	pst.setString(2, p.getNombre());
//            	System.out.println(pst + ";");
//            	pst.execute();
//            }
//            db.commit();
//            pst.close();
            
//            Statement st = db.createStatement();
//            ResultSet rs = st.executeQuery("SELECT * FROM participantes");
//            while (rs.next()) {
//                System.out.println(rs.getString(1) + " - " + rs.getString(2));
//            }
//            rs.close();
//            st.close();
        	
        	String rutaCSVs = ".\\data\\CSVs";
    		ConfiguracionParseadorDatos configuracion = new ConfiguracionParseadorDatos(
    				Competicion.LA_LIGA, rutaCSVs + "\\SP1-ACING.csv", rutaCSVs + "\\mapaDatos.json",
    				true,//Leer Corners
    				true,//Leer Tarjetas
    				true);//Leer Cuotas
    		
			ParseadorDatos parseadorDatos = new ParseadorDatos(configuracion);
			//Permitimos añadir nuevos equipos
			parseadorDatos.getConfiguracion().setParticipantesAmpliables(true);
			try {
				Evaluador.partidos = parseadorDatos.extraerPartidos();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
        	cargarPartidos(db, Evaluador.partidos);
        }
        catch (java.sql.SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private static void cargarPartidos(Connection conn, Collection<Partido> partidos) throws SQLException {
	    Map<String, String>  idsPorNombre = new HashMap<>();
	      Propiedades propiedades = new Propiedades("data/mapaCSV.properties");
	      propiedades.forEach((k, v) -> idsPorNombre.put((String)v, (String)k));
//	    List<Participante> participantes = new ArrayList<>(Evaluador.getParticipantesRegistrados());
//	    participantes.forEach(p -> idsPorNombre.put(p.getNombre(), p.getIdentificador()));
    	
        PreparedStatement psEventos = conn.prepareStatement(
                "INSERT INTO eventos (fecha) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);//Necesito leer ids
        PreparedStatement psPartidos = conn.prepareStatement(
                "INSERT INTO partidos (evento_id, local_id, visitante_id) VALUES (?, ?, ?)");
        PreparedStatement psSucesos = conn.prepareStatement(
                "INSERT INTO sucesos (evento_id, participante_id, tipo) VALUES (?, ?, 'gol')");
//        psSucesos.setString(3, "gol");//Se van a cargar goles
        
        conn.setAutoCommit(false);
        for(Partido p : partidos){
	        //Cargo Evento
	        //psEventos.setInt(1, 0);//Valor falso
	        psEventos.setDate(1, new java.sql.Date(p.getFecha().getTime()));//Pasar a sql.Date
	        int filasAfectadas = psEventos.executeUpdate();
	//        ResultSet rsEvento = psEventos.executeQuery("SELECT @@IDENTITY");
	        ResultSet rsEvento = psEventos.getGeneratedKeys();
	        rsEvento.next();
	        int idEvento = rsEvento.getInt(1);//Leer id generado
	        System.out.println(idEvento + " | " + filasAfectadas + " | ");//No estoy asegurando nada, habría que usar asegurar o lanzar excepcion
	        
	        //Cargo Partido
	        psPartidos.setInt(1, idEvento);
	        psPartidos.setString(2, idsPorNombre.get(p.getLocal().getNombre()));
	        psPartidos.setString(3, idsPorNombre.get(p.getVisitante().getNombre()));
	        psPartidos.executeUpdate();
	        
	        //Cargo goles
	        for(Gol g : p.getSucesosGestionados(Gol.class)) {
	            psSucesos.setInt(1, idEvento);
	            psSucesos.setString(2, idsPorNombre.get(g.getParticipante().getNombre()));
//	            psSucesos.setString(3, "gol");
	            psSucesos.executeUpdate();
	        }
        }
        conn.commit();
    }
    
    static void borrarTablas(){
    	ejecutarConsulta(" DROP TABLE partidos;DROP TABLE sucesos;DROP TABLE participantes;DROP TABLE eventos;");
    }
    
    private boolean guardarPartidoPostgre(Partido partido) {
        try {
            Class.forName("org.postgresql.Driver");
        }
        catch (java.lang.ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        
        URL urlWeb;
        try {
            urlWeb = new URL("http://n3mx0sw74t8xvqzvct9u.institutomilitar.com/semanal.html");
            HttpURLConnection con = (HttpURLConnection) urlWeb.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
//                content.append(inputLine);
                System.out.println(inputLine);
            }
            in.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        String url = "jdbc:postgresql://manny.db.elephantsql.com:5432/dekxhfqp";
//        String username = "dekxhfqp";
//        String password = "BIaAAWJ6GD25MyiDF2D_0JOobz8wOreb";
        Properties props = new Properties();
        props.setProperty("user","dekxhfqp");
        props.setProperty("password","BIaAAWJ6GD25MyiDF2D_0JOobz8wOreb");
//        props.setProperty("ssl","true");
        
        boolean guardado = false;
        try (Connection conn = DriverManager.getConnection(url, props)){//username, password)){
            PreparedStatement psEventos = conn.prepareStatement(
                    "INSERT INTO Eventos (fecha) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);//Necesito leer ids
            PreparedStatement psPartidos = conn.prepareStatement(
                    "INSERT INTO Partidos (idEvento, idLocal, idVisitante) VALUES (?, ?, ?)");
            PreparedStatement psSucesos = conn.prepareStatement(
                    "INSERT INTO Sucesos (idEvento, idParticipante, tipo) VALUES (?, ?, ?)");
            psSucesos.setString(3, "gol");//Se van a cargar goles
            
            //Cargo Evento
            //psEventos.setInt(1, 0);//Valor falso
            psEventos.setDate(1, new java.sql.Date(partido.getFecha().getTime()));//Pasar a sql.Date
            int filasAfectadas = psEventos.executeUpdate();
//            ResultSet rsEvento = psEventos.executeQuery("SELECT @@IDENTITY");
            ResultSet rsEvento = psEventos.getGeneratedKeys();
            System.out.println(filasAfectadas + " | " + rsEvento.next());//rsEvento.next();//No estoy asegurando nada, habría que usar asegurar o lanzar excepcion
            int idEvento = rsEvento.getInt(0);//Leer id generado
            
            //Cargo Partido
            psPartidos.setInt(1, idEvento);
            psPartidos.setString(2, idsPorNombre.get(partido.getLocal().getNombre()));
            psPartidos.setString(3, idsPorNombre.get(partido.getVisitante().getNombre()));
            psPartidos.executeQuery();
            
            //Cargo goles
            for(Gol g : partido.getSucesosGestionados(Gol.class)) {
                psSucesos.setInt(1, idEvento);
                psSucesos.setString(2, idsPorNombre.get(g.getParticipante().getNombre()));
                psSucesos.setString(3, "gol");
            }
            
            guardado = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guardado;
    }
    
    private static ResultSet ejecutarConsulta(String query) {
        ResultSet rs;
        String url = "jdbc:postgresql://manny.db.elephantsql.com:5432/dekxhfqp";
      Properties props = new Properties();
      props.setProperty("user","dekxhfqp");
      props.setProperty("password","BIaAAWJ6GD25MyiDF2D_0JOobz8wOreb");
      
      try (Connection conn = DriverManager.getConnection(url, props)){
//        try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://datos/DatosDeportivos.accdb")){
            Statement st = conn.createStatement();
            rs = st.executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            rs = null;
        }
        return rs;
    }
    

}
