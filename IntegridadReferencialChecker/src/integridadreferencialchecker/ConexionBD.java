/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integridadreferencialchecker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import java.sql.*;

/**
 *
 * @author David Arteaga
 */
public class ConexionBD {

    private String usuario, contra, nombreBD, nombreServidor, puerto;
    public Connection con;

    public ConexionBD(String usuario, String contra, String nombreBD, String nombreServidor, String puerto) {
        this.usuario = usuario;
        this.contra = contra;
        this.nombreBD = nombreBD;
        this.nombreServidor = nombreServidor;
        this.puerto = puerto;
        
        this.con = conectarConLaBD();
        
    }

    //****************************************************
    public Connection conectarConLaBD() {

        String connectionUrl = "jdbc:sqlserver://" + nombreServidor +":"+puerto+";"
                + "database="+nombreBD+";"
                + "user="+usuario+";"
                + "password="+contra+";" //123456
                + "trustServerCertificate=false;"
                + "loginTimeout=30;";

        try {
            Connection con = DriverManager.getConnection(connectionUrl);
            
            System.out.println("Conexion exitosa a la base de datos.");
            return con;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Ha ocurrido un error\nPor favor revise que los datos introducidos sean los correctos.");
            System.out.println("Error.");
            System.out.println(ex.toString());
            return null;
        }
    }

    public ArrayList <String> getListaIntegridadReferencial(){
        try{
            String SQL = "select name from sys.check_constraints " +
                        "UNION " +
                        "select name from sys.foreign_keys;";
            Statement stmt = this.con.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            ArrayList<String> listaIntRef = new ArrayList<>();
            
            while (rs.next()) {
                listaIntRef.add(rs.getString("name"));
            }
            
            return listaIntRef;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    
    public ArrayList<String> getAnomaliasCONdatos(ArrayList<String> miLista){
        try{
            
            ArrayList<String> listaAConDatos = new ArrayList<>();
            for(String bucle: miLista){
                String SQL = "DBCC CHECKCONSTRAINTS("+bucle+");";
                Statement stmt = this.con.createStatement();
                try{
                    ResultSet rs = stmt.executeQuery(SQL);
                    while (rs.next()) {
                    String aux = rs.getString("Table")+"| " + rs.getString("Constraint") +"| "+ rs.getString("Where");
                    listaAConDatos.add(aux);
                    }
                }catch(Exception e){
                    
                }
                
            }
            
            
            return listaAConDatos;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    
    
    public ArrayList<String> getAnomaliasSINdatos(){
        try{
            String SQL = "create view getClavesPrimarias\n" +
"	as\n" +
"	select \n" +
"		pk.[name] as pk_name,\n" +
"		substring(column_names, 1, len(column_names)-1) as [columns],\n" +
"		tab.[name] as table_name\n" +
"	from sys.tables tab\n" +
"		inner join sys.indexes pk\n" +
"			on tab.object_id = pk.object_id \n" +
"			and pk.is_primary_key = 1\n" +
"	   cross apply (select col.[name] + ', '\n" +
"						from sys.index_columns ic\n" +
"							inner join sys.columns col\n" +
"								on ic.object_id = col.object_id\n" +
"								and ic.column_id = col.column_id\n" +
"						where ic.object_id = tab.object_id\n" +
"							and ic.index_id = pk.index_id\n" +
"								order by col.column_id\n" +
"								for xml path ('') ) D (column_names)";
            Statement stmt = this.con.createStatement();
            stmt.execute(SQL);
            
        }catch(Exception e){
            System.out.println("Vista  ya Creada");
        }
        
        try{
            
            String SQLc = "select * from getClavesPrimarias\n" +
"where columns = (select columns from getClavesPrimarias GROUP BY columns HAVING COUNT(*)>1)";
            Statement stmt = this.con.createStatement();
            ResultSet rs = stmt.executeQuery(SQLc);
            ArrayList<String> listaAnomaliasSINdatos = new ArrayList<>();
            
            while (rs.next()) {
                String aux = rs.getString("pk_name") + "| " + rs.getString("columns") + "| " + rs.getString("table_name");
                listaAnomaliasSINdatos.add(aux);
            }
            return listaAnomaliasSINdatos;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
         
    }
    
    
    
    
}

