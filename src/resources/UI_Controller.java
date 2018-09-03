package resources;



import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class UI_Controller {
	
	//textAreaPrincipal
	//botonComprobar
	//botonDescargar
	//botonSalir
	
	@FXML
	private TextArea textAreaPrincipal;

	//private String direccionTanuss="https://sede.seg-social.gob.es/wps/portal/sede/sede/TablonAnuncios/";
	private String direccionTanuss="C:\\Users\\Jesus-Pc\\Downloads\\tanuss_31_08_2018.html";
	//private String direccionTanuss="C:\\Users\\Jesus-Pc\\Downloads\\tanus_1_pagina.html";
	
	private static ChromeDriver navegador = null;
	private static HashMap<String, URL> urls; 
	private static final Logger log = LoggerFactory.getLogger(UI_Controller.class);
	
	
	@FXML
	void comprobarConexion() {
		textAreaPrincipal.appendText("Comprobando"+"\r\n");
		navegador = new ChromeDriver();
		try {
			navegador.get(direccionTanuss);
			textAreaPrincipal.appendText("Conectado a dirección: "+direccionTanuss+"\r\n");
		}catch (Exception e) {
			// TODO: handle exception
			textAreaPrincipal.appendText("No se ha logrado conectar a dirección: "+direccionTanuss+"\r\n");
		}
		
		//Cierro Navegador
				if (navegador != null) {
					navegador.close();
				}
	}
	
	@FXML
	void descargarFechaActual() {
		urls = new HashMap<String, URL>();
		ArrayList<String> urlsPaginasAnuncios = new ArrayList<>();
		String identificador;
		
		navegador = new ChromeDriver();

		navegador.get(direccionTanuss);
		textAreaPrincipal.appendText("Conectado y descargando de dirección: " + direccionTanuss + "\r\n");

		//Primero, antes de realizar descarga, compruebo si hay más de una página de anuncios, para después poder
		//navegar a esta.
		
		//Busco elemento contenedor de páginas de anuncios
		List<WebElement> listaPaginas = navegador.findElement(By.className("pagination-container"))
				.findElements(By.tagName("a"));
		
		//Compruebo cada elemento, para obtener su URl.
		for (WebElement webElement : listaPaginas) {			
			String hrefString = webElement.getAttribute("href");
			
			//Compruebo que el enlace "a" no sea null, y contenga un href
			if(webElement.getAttribute("href")!=null) {
				Pattern p = Pattern.compile("==/");
				Matcher m = p.matcher(webElement.getAttribute("href"));
				if(m.find()) {
					identificador = StringUtils.substringAfterLast(webElement.getAttribute("href"),m.group(0)); 
					if(!urlsPaginasAnuncios.contains(identificador)) {
						//Guardo url de páginas adicionales.
						urlsPaginasAnuncios.add(webElement.getAttribute("href"));
					}
					
				}
			}	
			//Si UrlPaginasAnuncios size no es mayor que cero, no hay pagination container, y no es necesario ir a otras páginas.
		}
		
		
		//Obtengo url de anuncios individuales para la página actual. Pasar a método, para poder ejecutar al final, si el tamaño
		//es mayor que cero
		List <WebElement> anuncios = navegador.findElement(By.className("edicts")).findElement(By.cssSelector("tbody")).findElements(By.tagName("a"));
		int i = 0;
		for(WebElement anuncio : anuncios) {
			try {
				urls.put(String.valueOf(i),new URL(anuncio.getAttribute("href")));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		
		

		//Cierro Navegador
		if (navegador != null) {
			navegador.close();
		}
	}
	
	private static File _down(URL url, String dia, String mes, String año, String numBoletin){

		boolean descargado = false;
		File fichero = null;
		
		try{
			String ruta="C:\\Temp\\DescargaTanuss\\2018\\";		
			if(!new File(ruta).exists()){
				new File(ruta).mkdirs();
			}
			
			String nombreFichero=ruta+numBoletin+"_("+dia+"_"+mes+"_"+año+")";
			
			nombreFichero+=".pdf";
				
			fichero=new File(nombreFichero);					
			log.info("  Descargando "+url.toString());
			URLConnection urlcon = url.openConnection();
			urlcon.connect();		
			InputStream is = urlcon.getInputStream();  
			
			byte[] buffer=new byte[8192];
			int leido = is.read(buffer);			
			FileOutputStream fw = new FileOutputStream(fichero);
			while (leido > 0) {
				
				fw.write(buffer, 0, leido);
				leido = is.read(buffer);
			}
			fw.close();
			is.close();
					
			//si el fichero no es válido lo elimino
//			if (!Utiles.validaPDF(fichero)) {
//				fichero.delete();
//			} else {
//				descargado = true;
//			}
//			
		}catch (Exception e) {
			log.error(e.toString());
			descargado = false;
		}	
		
		return (descargado?fichero:null);
	}

}
