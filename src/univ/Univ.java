/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package univ;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

/**
 *
 * @author gurrutia
 */
public class Univ {

    public static final String SEP = ",";
    public static final String ESP = " ";
    public static ArrayList<ArrayList<String>> universidades = new ArrayList<>();
    public static ArrayList<ArrayList<String>> universidadesSinonimo = new ArrayList<>();
    public static ArrayList<String> universidadesHomologadas = new ArrayList<>();
    public static ArrayList<ArrayList<String>> universidadesSinonimoConsolidado = new ArrayList<>();
    public static JSONArray js;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        leerCSV();
        leerJSON();
        homologar();
    }

    public static void leerCSV() {

        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader("...\\rsc\\instituciones_educativas.csv"));
            String line = br.readLine();
            ArrayList<String> universidad;
            while (line != null) {
                String[] fields = line.split(SEP, 2);
                //System.out.println(Arrays.toString(fields));
                universidad = new ArrayList<>();

                if (fields.length > 1) {
                    universidad.add(fields[0]);
                    universidad.add(fields[1]);
                } else if (fields.length == 1) {
                    universidad.add(fields[0]);
                }

                universidades.add(universidad);

                line = br.readLine();
            }

            //for (int i = 0; i < universidades.size(); i++) {
                //System.out.println(i + "> " + universidades.get(i).toString());
            //}
            
            universidades.remove(0);

        } catch (IOException e) {
            System.err.println("Error " + e.getMessage());
        }

    }

    public static void leerJSON() {

        try {

            Object ob = new JSONParser().parse(new FileReader("...\\rsc\\universidades.json"));

            js = (JSONArray) ob;

            //System.out.println("" + js.toJSONString());
        } catch (IOException | ParseException e) {
            System.err.println("Error " + e.getMessage());
        }

    }

    public static void homologar() {
        String[] fields;
        String field;
        String palabras = "";

        for (int i = 0; i < universidades.size(); i++) {
        //for (int i = 0; i < 100; i++) {
            palabras = "";

            if (universidades.get(i).size() == 1) {
                fields = universidades.get(i).get(0).split(ESP);
                field = universidades.get(i).get(0);
            } else {
                fields = universidades.get(i).get(1).split(ESP);
                field = universidades.get(i).get(1);
            }

//            for (int y = 0; y < fields.length; y++) {
//                palabras = palabras + fields[y] + "-";
//            }

            comparaJSON(fields, universidades.get(i), field);

            //System.out.println("" + palabras);

        }
        
        setCSVUnivHomologadas();
        //verUniversidadesSinonimo();
        consolidadUniversidadesSinonimos();

    }

    public static void comparaJSON(String[] fields, ArrayList universidad, String field){
        int contador = 0;
        boolean esSigla = false;
        int max = 0;
        String univSimil = "";

        //System.out.println("----------Universidad " + universidad.toString());
        for (int i = 0; i < js.size(); i++) {

            //System.out.println(i + ">" + js.get(i));
            

            try {
                Object ob = new JSONParser().parse(js.get(i).toString());
                JSONObject jsob = (JSONObject) ob;

                //System.out.println("---------------------------------");
                //System.out.println("" + jsob.get("código INEI") + "\t" + jsob.get("Nombre ") + "\t" + jsob.get("Siglas "));
                for (int y = 0; y < fields.length; y++) {
                    
                    if(fields[y].length() > 2 && !fields[y].equalsIgnoreCase("Universidad") && !fields[y].equalsIgnoreCase("Del")){
                        
                        if(fields[y].trim().equalsIgnoreCase("Instituto")){
                            break;
                        }                        
                        
                        if(eliminarTildes(jsob.get("Nombre ").toString().toLowerCase()).contains(eliminarTildes(fields[y].toLowerCase()))){
                            //System.out.println("" + jsob.get("Nombre ").toString() + " CONTIENE " + fields[y]);
                            if(!fields[y].toLowerCase().equalsIgnoreCase("s.a.c")){
                                contador++;
                            }
                            
                        }

                        if(jsob.get("Siglas ").toString().toLowerCase().equalsIgnoreCase(fields[y].toLowerCase().trim())){
                            univSimil = jsob.get("Nombre ").toString();
                            esSigla = true;
                            break;
                        }
                    }
                    
                }
                
                if(!esSigla){
                    if(contador > max){
                       max = contador;
                       if(max > 1){
                           univSimil = jsob.get("Nombre ").toString();
                       }
                    }                    
                }

                
                contador = 0;
                esSigla = false;

            } catch (ParseException e) {
                System.err.println("Error " + e.getMessage());
            }

        }
        
        if(universidad.size() == 1){
            universidadesHomologadas.add("," + universidad.get(0) + "," + univSimil);
        }else{
            universidadesHomologadas.add(universidad.get(0) + "," + universidad.get(1) + "," + univSimil);
        }
        
        if(univSimil.length() > 0){
            ArrayList<String> universidadSinonimo;
            universidadSinonimo = new ArrayList<>();
            universidadSinonimo.add(univSimil);
            universidadSinonimo.add(field);

            universidadesSinonimo.add(universidadSinonimo);
        }
        
        //System.out.println(universidad.get(1) + " > " + univSimil);
    }

    
    public static void verUniversidadesSinonimo(){     
        for(int i = 0; i < universidadesSinonimo.size(); i++){
            System.out.println(">>> " + universidadesSinonimo.get(i));
        }
    }

    public static void consolidadUniversidadesSinonimos(){
        boolean agrega = false;
        // [Universidad Tecnológica del Perú S.A.C., Utp] <-------
        for(int i = 0; i < universidadesSinonimo.size(); i++){
            
            // [Universidad Científica del Sur S.A.C.,  Cientifica  Del Sur]
            // [Universidad Tecnológica del Perú S.A.C., Utp, Tecnológica del Perú] <-----------
            for(int y = 0; y < universidadesSinonimoConsolidado.size(); y++){
                
                //System.out.println("COMPARA: " + universidadesSinonimo.get(i).get(0) + " <===> " + universidadesSinonimoConsolidado.get(y).get(0));
                if(universidadesSinonimo.get(i).get(0).equalsIgnoreCase(universidadesSinonimoConsolidado.get(y).get(0))){
                    
                    for(int z = 1; z < universidadesSinonimoConsolidado.get(y).size(); z++){
                        if(universidadesSinonimoConsolidado.get(y).get(z).equalsIgnoreCase(universidadesSinonimo.get(i).get(1).trim())){
                            agrega = true;
                            break;
                        }else if(z == universidadesSinonimoConsolidado.get(y).size()-1){
                            agrega = true;
                            //System.out.println("AGREGA SINONIMO " + universidadesSinonimo.get(i).get(1).trim());
                            universidadesSinonimoConsolidado.get(y).add(universidadesSinonimo.get(i).get(1).trim());
                        }
                    }
                    
                }
                
            }
            
            if(agrega){
                agrega = false;
            }else{
                //System.out.println("AGREGA " + universidadesSinonimo.get(i).toString());
                universidadesSinonimoConsolidado.add(universidadesSinonimo.get(i));
            }
            
        }
        
//        for(int i = 0; i < universidadesSinonimoConsolidado.size(); i++){
//            System.out.println("" + universidadesSinonimoConsolidado.get(i));
//        }
        
        setJSONUnivSinonimos();
    }
    
    public static void setJSONUnivSinonimos(){
        JSONArray jaUniversidadesSinonimos = new JSONArray();
        JSONObject joUnivSinonimo = new JSONObject();
        ArrayList<String> arraySinonimo = new ArrayList<>();
        
        for(int i = 0; i < universidadesSinonimoConsolidado.size(); i++){
            

            // [Universidad Peruana Unión, Universidad Peruana Unión, Universidad Peruana Unión - Upeu, Univresidad Peruana Unión]
            for(int y = 0; y < universidadesSinonimoConsolidado.get(i).size(); y++){
                if(y == 0){
                    joUnivSinonimo.put("nombre_universidad", universidadesSinonimoConsolidado.get(i).get(y));
                }else{
                    arraySinonimo.add(universidadesSinonimoConsolidado.get(i).get(y));
                }
            }
            
            joUnivSinonimo.put("sinonimos", arraySinonimo.toString());
            arraySinonimo = new ArrayList<>();
            
            jaUniversidadesSinonimos.add(joUnivSinonimo);
            //System.out.println("" + joUnivSinonimo.toJSONString());
            joUnivSinonimo = new JSONObject();
            
        }
        
        
        //System.out.println("" + jaUniversidadesSinonimos.toJSONString());
        setJSONFicheroUnivSinonimos(jaUniversidadesSinonimos.toJSONString());
        
        
    }
    
    public static void setCSVUnivHomologadas(){
        try{
            PrintWriter writer = new PrintWriter("...\\rsc\\universidades_homologadas.csv", "UTF-8");
            writer.println("candidateid, value, universidad homologada");
            
            for(int i = 0; i < universidadesHomologadas.size(); i++){
                //System.out.println("" + universidadesHomologadas.get(i));
                writer.println(universidadesHomologadas.get(i));
            }
            
            writer.close();
            System.out.println("Archivo universidades_homologadas.csv creado");
        
        }catch(FileNotFoundException | UnsupportedEncodingException e){
            System.err.println("" + e.getMessage());
        }
        
    }
    
    public static void setJSONFicheroUnivSinonimos(String ja){
        try{
            PrintWriter writer = new PrintWriter("...\\rsc\\sinonimo_universidades.json", "UTF-8");
            writer.println(ja);            
            writer.close();
            
            System.out.println("Archivo sinonimo_universidades.json creado");
        
        }catch(FileNotFoundException | UnsupportedEncodingException e){
            System.err.println("" + e.getMessage());
        }
        
    }
    
    public static String eliminarTildes(String texto){
        String textoActualizado;
        
        textoActualizado = texto.replaceAll("Á", "A");
        textoActualizado = textoActualizado.replaceAll("á", "a");
        textoActualizado = textoActualizado.replaceAll("É", "E");
        textoActualizado = textoActualizado.replaceAll("é", "e");
        textoActualizado = textoActualizado.replaceAll("Í", "I");
        textoActualizado = textoActualizado.replaceAll("í", "i");
        textoActualizado = textoActualizado.replaceAll("Ó", "O");
        textoActualizado = textoActualizado.replaceAll("ó", "o");
        textoActualizado = textoActualizado.replaceAll("Ú", "U");
        textoActualizado = textoActualizado.replaceAll("ú", "u");
        
        return textoActualizado;
    }

}
