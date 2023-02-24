package bbdd;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BBDD {

	private String nombreBBDD;
	private ArrayList<String> listaSentencias;
	private Connection conexion;
	private Statement sentencia;
	
	private ArrayList<Dia> listaDias;
	private ArrayList<Hora> listaHoras;
	
	//Conexion Hibernate
	Configuration config;
	SessionFactory factoria;
	Session sesion;
	
	public BBDD(String nombre)	{
		
		this.nombreBBDD = nombre;
		listaSentencias = new ArrayList<String>();
		listaDias = new ArrayList<Dia>();
		listaHoras = new ArrayList<Hora>();
		
		try {
			conexion = DriverManager.getConnection("jdbc:mysql://localhost", "root", "root");
			
			sentencia = conexion.createStatement();
			sentencia.executeUpdate("DROP DATABASE IF EXISTS " + nombreBBDD);
			
			sentencia = conexion.createStatement();
			sentencia.executeUpdate("CREATE DATABASE IF NOT EXISTS " + nombreBBDD);
			
			introducirSentencias();
			rellenarBBDD();
			
			System.out.println("Base de datos creada correctamente");
		} catch (SQLException e) {
			System.out.println("Comprueba que MAMP esté iniciado");
			e.printStackTrace();
		}
	}
	
	private void introducirSentencias()	{
		listaSentencias.add("CREATE TABLE IF NOT EXISTS TIEMPO.DIA (\r\n"
				+ "DIA DATE NOT NULL,\r\n"
				+ "TEMPMAX INT NOT NULL,\r\n"
				+ "TEMPMIN INT NOT NULL,\r\n"
				+ " DESCRIPCION VARCHAR(100) NOT NULL,\r\n"
				+ " HUMEDAD INT NOT NULL,\r\n"
				+ " VIENTO INT NOT NULL,\r\n"
				+ " DIRECCION VARCHAR(100) NOT NULL,\r\n"
				+ "PRIMARY KEY(DIA)\r\n"
				+ ")");
		listaSentencias.add("CREATE TABLE IF NOT EXISTS TIEMPO.HORA (\r\n"
				+ "HORA TIME NOT NULL,\r\n"
				+ " DIA DATE NOT NULL,\r\n"
				+ "TEMP INT NOT NULL,\r\n"
				+ "DESCRIPCION VARCHAR(100) NOT NULL,\r\n"
				+ " PRESION INT NOT NULL,\r\n"
				+ " HUMEDAD INT NOT NULL,\r\n"
				+ " VIENTO INT NOT NULL,\r\n"
				+ " DIRECCION VARCHAR(100) NOT NULL,\r\n"
				+ "PRIMARY KEY(HORA,DIA)\r\n"
				+ ")");
		listaSentencias.add("ALTER TABLE TIEMPO.HORA ADD CONSTRAINT FOREIGN KEY(DIA) REFERENCES\r\n"
				+ "TIEMPO.DIA(DIA) ON UPDATE CASCADE ON DELETE CASCADE");
	}
	
	private void rellenarBBDD()	{
		for (String sql : listaSentencias) {
			try {
				conexion = DriverManager.getConnection("jdbc:mysql://localhost/" + nombreBBDD, "root", "root");
				sentencia = conexion.createStatement();
				sentencia.executeUpdate(sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void anhadirDias()	{
		try	{
			conectar();
			Document arbolDOM = UtilidadesDOM.gernerararbolDOMURL("https://api.tutiempo.net/xml/?lan=es&apid=zwDX4azaz4X4Xqs&lid=3768");
			Element raizData = arbolDOM.getDocumentElement();
			
			for (int i = 1; i <= 7; i++) {
				Element dia = (Element) raizData.getElementsByTagName("day" + i).item(0);
				Element Edate = (Element) dia.getElementsByTagName("date").item(0);
				Element Etemperature_max = (Element) dia.getElementsByTagName("temperature_max").item(0);
				Element Etemperature_min = (Element) dia.getElementsByTagName("temperature_min").item(0);
				Element Etext = (Element) dia.getElementsByTagName("text").item(0);
				Element Ehumidity = (Element) dia.getElementsByTagName("humidity").item(0);
				Element Ewind = (Element) dia.getElementsByTagName("wind").item(0);
				Element Ewind_direction = (Element) dia.getElementsByTagName("wind_direction").item(0);
				
				String date = Edate.getFirstChild().getNodeValue().toString();
				int temp_max = Integer.parseInt(Etemperature_max.getFirstChild().getNodeValue().toString());
				int temp_min = Integer.parseInt(Etemperature_min.getFirstChild().getNodeValue().toString());
				String text = Etext.getFirstChild().getNodeValue().toString();
				int humidity = Integer.parseInt(Ehumidity.getFirstChild().getNodeValue().toString());
				int wind = Integer.parseInt(Ewind.getFirstChild().getNodeValue().toString());
				String wind_direction = Ewind_direction.getFirstChild().getNodeValue().toString();
				
				Dia nuevoDia = new Dia(date, temp_max, temp_min, text, humidity, wind, wind_direction);
				listaDias.add(nuevoDia);
				sesion.beginTransaction();
				sesion.save(nuevoDia);
				sesion.getTransaction().commit();
			}
			System.out.println("Base de datos rellenada con la informacion meteorologica en dias.");
			desconectar();
		}catch (Exception e)	{
			e.printStackTrace();
		}
	}
	
	public void anhadirHoras()	{
		try	{
			conectar();
			Document arbolDOM = UtilidadesDOM.gernerararbolDOMURL("https://api.tutiempo.net/xml/?lan=es&apid=zwDX4azaz4X4Xqs&lid=3768");
			Element raizData = arbolDOM.getDocumentElement();
			Element raizHora = (Element) raizData.getElementsByTagName("hour_hour").item(0);
			
			for (int i = 1; i <= 25; i++) {
				Element hora = (Element) raizHora.getElementsByTagName("hour" + i).item(0);
				
				Element Edate = (Element) hora.getElementsByTagName("date").item(0);
				Element hour_data = (Element) hora.getElementsByTagName("hour_data").item(0);
				Element Etemperature = (Element) hora.getElementsByTagName("temperature").item(0);
				Element Etext = (Element) hora.getElementsByTagName("text").item(0);
				Element Ehumidity = (Element) hora.getElementsByTagName("humidity").item(0);
				Element Epressure = (Element) hora.getElementsByTagName("pressure").item(0);
				Element Ewind = (Element) hora.getElementsByTagName("wind").item(0);
				Element Ewind_direction = (Element) hora.getElementsByTagName("wind_direction").item(0);
				
				String horaString = hour_data.getFirstChild().getNodeValue().toString();
				
				SimpleDateFormat formateadorfecha = new SimpleDateFormat("yyyy-mm-dd");
				Date diasql=new java.sql.Date(formateadorfecha.parse(Edate.getFirstChild().getNodeValue().toString()).getTime());
				Dia diaAsignado = sesion.get(Dia.class, diasql);
				
				int temp = Integer.valueOf(Etemperature.getFirstChild().getNodeValue().toString()); 
				String desc = Etext.getFirstChild().getNodeValue().toString();
				int presion = Integer.valueOf(Epressure.getFirstChild().getNodeValue().toString());
				int humedad = Integer.valueOf(Ehumidity.getFirstChild().getNodeValue().toString());
				int viento = Integer.valueOf(Ewind.getFirstChild().getNodeValue().toString());
				String direccion = Ewind_direction.getFirstChild().getNodeValue().toString();
				
				Hora nuevaHora = new Hora(horaString, diaAsignado, temp, desc, presion, humedad, viento, direccion);
				listaHoras.add(nuevaHora);
				sesion.beginTransaction();
				sesion.save(nuevaHora);
				sesion.getTransaction().commit();
			}
			
			System.out.println("Base de datos rellenada con la informacion meteorologica en dias.");
			desconectar();
		}catch (Exception e)	{
			e.printStackTrace();
		}
	}
	
	public void generarHTML()	{
		SimpleDateFormat formateadorDia = new SimpleDateFormat("dd/MM/yyyy");
		
		String html = "<!DOCTYPE html>\r\n"
				+ "<html lang=\"es\">\r\n"
				+ "<head>\r\n"
				+ "    <meta charset=\"UTF-8\">\r\n"
				+ "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\r\n"
				+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
				+ "    <title>Practica Tiempo</title>\r\n"
				+ "</head>\r\n"
				+ "<body>\r\n"
				+ "    <table border=\"2\" style=\"margin:auto\">";
		
		for (Dia dia : listaDias) {
			switch(dia.getDescripcion().toLowerCase())	{
			case "despejado":
				html += "<tr>\r\n"
						+ "            <td colspan=\"6\" style=\"font-size: xx-large; font-weight: bold; text-align: center;\">" + formateadorDia.format(dia.getDia()) + "</td>\r\n"
						+ "            <td rowspan=\"3\"><img src='./././imagenes/despejado.PNG'></td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura maxima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura minima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Descripcion</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Humedad</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Viento</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Direccion</td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmax() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmin() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDescripcion() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getHumedad() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getViento() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDireccion() + "</td>\r\n"
						+ "        </tr>";
				break;
			case "cubierto":
				html += "<tr>\r\n"
						+ "            <td colspan=\"6\" style=\"font-size: xx-large; font-weight: bold; text-align: center;\">" + formateadorDia.format(dia.getDia()) + "</td>\r\n"
						+ "            <td rowspan=\"3\"><img src='./././imagenes/cubierto.PNG'></td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura maxima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura minima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Descripcion</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Humedad</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Viento</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Direccion</td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmax() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmin() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDescripcion() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getHumedad() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getViento() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDireccion() + "</td>\r\n"
						+ "        </tr>";
				break;
			case "cubierto lluvioso":
				html += "<tr>\r\n"
						+ "            <td colspan=\"6\" style=\"font-size: xx-large; font-weight: bold; text-align: center;\">" + formateadorDia.format(dia.getDia()) + "</td>\r\n"
						+ "            <td rowspan=\"3\"><img src='./././imagenes/cubierto_lluvioso.PNG'></td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura maxima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura minima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Descripcion</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Humedad</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Viento</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Direccion</td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmax() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmin() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDescripcion() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getHumedad() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getViento() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDireccion() + "</td>\r\n"
						+ "        </tr>";
				break;
			case "muy nuboso":
				html += "<tr>\r\n"
						+ "            <td colspan=\"6\" style=\"font-size: xx-large; font-weight: bold; text-align: center;\">" + formateadorDia.format(dia.getDia()) + "</td>\r\n"
						+ "            <td rowspan=\"3\"><img src='./././imagenes/muy_nuboso.PNG'></td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura maxima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura minima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Descripcion</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Humedad</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Viento</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Direccion</td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmax() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmin() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDescripcion() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getHumedad() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getViento() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDireccion() + "</td>\r\n"
						+ "        </tr>";
				break;
			case "nubes dispersa":
				html += "<tr>\r\n"
						+ "            <td colspan=\"6\" style=\"font-size: xx-large; font-weight: bold; text-align: center;\">" + formateadorDia.format(dia.getDia()) + "</td>\r\n"
						+ "            <td rowspan=\"3\"><img src='./././imagenes/nubes_dispersa.PNG'></td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura maxima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura minima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Descripcion</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Humedad</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Viento</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Direccion</td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmax() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmin() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDescripcion() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getHumedad() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getViento() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDireccion() + "</td>\r\n"
						+ "        </tr>";
				break;
			case "nublado lluvioso":
				html += "<tr>\r\n"
						+ "            <td colspan=\"6\" style=\"font-size: xx-large; font-weight: bold; text-align: center;\">" + formateadorDia.format(dia.getDia()) + "</td>\r\n"
						+ "            <td rowspan=\"3\"><img src='./././imagenes/nublado_lluvioso.PNG'></td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura maxima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura minima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Descripcion</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Humedad</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Viento</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Direccion</td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmax() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmin() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDescripcion() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getHumedad() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getViento() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDireccion() + "</td>\r\n"
						+ "        </tr>";
				break;
			case "parcialmente nuboso":
				html += "<tr>\r\n"
						+ "            <td colspan=\"6\" style=\"font-size: xx-large; font-weight: bold; text-align: center;\">" + formateadorDia.format(dia.getDia()) + "</td>\r\n"
						+ "            <td rowspan=\"3\"><img src='./././imagenes/parcialmente_nuboso.PNG'></td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura maxima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura minima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Descripcion</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Humedad</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Viento</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Direccion</td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmax() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmin() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDescripcion() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getHumedad() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getViento() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDireccion() + "</td>\r\n"
						+ "        </tr>";
				break;
			case "tormenta":
				html += "<tr>\r\n"
						+ "            <td colspan=\"6\" style=\"font-size: xx-large; font-weight: bold; text-align: center;\">" + formateadorDia.format(dia.getDia()) + "</td>\r\n"
						+ "            <td rowspan=\"3\"><img src='./././imagenes/tormenta.PNG'></td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura maxima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Temperatura minima</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Descripcion</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Humedad</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Viento</td>\r\n"
						+ "            <td style=\"font-weight: bold; padding: 10px; font-size: larger; text-align: justify;\">Direccion</td>\r\n"
						+ "        </tr>\r\n"
						+ "        <tr>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmax() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getTempmin() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDescripcion() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getHumedad() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getViento() + "</td>\r\n"
						+ "            <td style=\"text-align: center; padding: 10px;\">" + dia.getDireccion() + "</td>\r\n"
						+ "        </tr>";
				break;
			}
			
		}
		
		html += "</table>\r\n"
				+ "</body>\r\n"
				+ "</html>";
		
		try {
			Path rutaficheroSalida=Paths.get("index.html");
			Charset cs=Charset.forName("utf-8");
			BufferedWriter ficherosalida;
			ficherosalida = Files.newBufferedWriter(rutaficheroSalida,cs);
			ficherosalida.write(html);
			ficherosalida.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Fichero HTML creado.");
	}
	
	public void generarXML()	{
		try {
			Document arbolDOM = UtilidadesDOM.generararbolDOMvacio();
			Element raiz = arbolDOM.createElement("tiempo");
			arbolDOM.appendChild(raiz);
			
			for (Dia dia : listaDias) {
				Element Edia = arbolDOM.createElement("dia");
				Edia.setAttribute("id", dia.getDiaString());
				raiz.appendChild(Edia);
				
				Element Etempmax = arbolDOM.createElement("tempmax");
				Etempmax.setTextContent(String.valueOf(dia.getTempmax()));
				
				Element Etempmin = arbolDOM.createElement("tempmin");
				Etempmin.setTextContent(String.valueOf(dia.getTempmin()));
				
				Element Edescripcion = arbolDOM.createElement("descripcion");
				Edescripcion.setTextContent(dia.getDescripcion());
				
				Element Ehumedad = arbolDOM.createElement("humedad");
				Ehumedad.setTextContent(String.valueOf(dia.getHumedad()));
				
				Element Eviento = arbolDOM.createElement("viento");
				Eviento.setTextContent(String.valueOf(dia.getViento()));
				
				Element Edireccion = arbolDOM.createElement("direccion");
				Edireccion.setTextContent(dia.getDireccion());
				
				Edia.appendChild(Etempmax);
				Edia.appendChild(Etempmin);
				Edia.appendChild(Edescripcion);
				Edia.appendChild(Ehumedad);
				Edia.appendChild(Eviento);
				Edia.appendChild(Edireccion);
				
				int id = 0;
				if (tieneHora(dia))	{
					
					Element Ehoras = arbolDOM.createElement("horas");
					Edia.appendChild(Ehoras);
					
					for (Hora hora : listaHoras) {
						
						if (hora.getDia().getDia().equals(dia.getDia()))	{
							SimpleDateFormat formateadorHora = new SimpleDateFormat("HH:mm");
							
							id++;
							Element Ehora = arbolDOM.createElement("hora");
							Ehora.setAttribute("id", String.valueOf(id));
							
							Element Edato = arbolDOM.createElement("dato");
							Edato.setTextContent(formateadorHora.format(hora.getId().getHora()));
							
							Element Etemp = arbolDOM.createElement("temp");
							Etemp.setTextContent(String.valueOf(hora.getTemp()));
							
							Element EdescripcionHora = arbolDOM.createElement("descripcion");
							EdescripcionHora.setTextContent(hora.getDescripcion());
							
							Element Epresion = arbolDOM.createElement("presion");
							Epresion.setTextContent(String.valueOf(hora.getPresion()));
							
							Element EhumedadHora = arbolDOM.createElement("humedad");
							EhumedadHora.setTextContent(String.valueOf(hora.getHumedad()));
							
							Element EvientoHora = arbolDOM.createElement("viento");
							EvientoHora.setTextContent(String.valueOf(hora.getViento()));
							
							Element EdireccionHora = arbolDOM.createElement("direccion");
							EdireccionHora.setTextContent(hora.getDireccion());
							
							Ehoras.appendChild(Ehora);
							Ehora.appendChild(Edato);
							Ehora.appendChild(Etemp);
							Ehora.appendChild(EdescripcionHora);
							Ehora.appendChild(Epresion);
							Ehora.appendChild(EhumedadHora);
							Ehora.appendChild(EdireccionHora);
						}
						
					}
					
				}
				id = 0;
			}
			
			UtilidadesDOM.crearficheroxml(arbolDOM, "tiempo.xml");
			System.out.println("Fichero XML creado.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean tieneHora(Dia dia)	{
		for (Hora hora : listaHoras) {
			if (hora.getId().getDia().equals(dia.getDia()))	{
				return true;
			}
		}
		return false;
	}
	
	private void conectar()	{
		config = new Configuration();
		config.configure("./hibernate.cfg.xml");
		factoria = config.buildSessionFactory();
		sesion = factoria.openSession();
	}
	
	private void desconectar()	{
		sesion.close();
		factoria.close();
	}
	
}
