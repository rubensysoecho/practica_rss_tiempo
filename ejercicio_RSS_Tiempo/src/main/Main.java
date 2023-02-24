package main;

import bbdd.BBDD;

public class Main {
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		org.jboss.logging.Logger logger = org.jboss.logging.Logger.getLogger("org.hibernate");
		java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF);
		
		BBDD bbdd_tiempo = new BBDD("tiempo");
		bbdd_tiempo.anhadirDias();
		bbdd_tiempo.anhadirHoras();
		bbdd_tiempo.generarHTML();
		bbdd_tiempo.generarXML();
	}

}
