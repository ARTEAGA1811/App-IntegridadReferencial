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
    
    
    public ArrayList<String[]> getAnomaliasCONdatos(ArrayList<String> miLista){
        try{
            
            ArrayList<String[]> listaAConDatos = new ArrayList<>();
            for(String bucle: miLista){
                String SQL = "DBCC CHECKCONSTRAINTS("+bucle+");";
                Statement stmt = this.con.createStatement();
                try{
                    ResultSet rs = stmt.executeQuery(SQL);
                    while (rs.next()) {
                        String arrAux [] = new String[3];
                        arrAux[0] = rs.getString("Table");
                        arrAux[1] = rs.getString("Constraint");
                        arrAux[2] = rs.getString("Where");
                        listaAConDatos.add(arrAux);
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
    
    
    
    public ArrayList<String[]> getAnomaliasSINdatos(){
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
            ArrayList<String[]> listaAnomaliasSINdatos = new ArrayList<>();
            
            while (rs.next()) {
                String arrAux []= new String[3];
                arrAux[0] = rs.getString("pk_name");
                arrAux[1] = rs.getString("columns");
                arrAux[2] = rs.getString("table_name");
                listaAnomaliasSINdatos.add(arrAux);
            }
            return listaAnomaliasSINdatos;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
         
    }
    
    
    
    
    public ArrayList<Object[]> getTriggers(){
        String SQL = ";WITH\n" +
"        TableTrigger\n" +
"        AS\n" +
"        (\n" +
"            Select \n" +
"                Sys.Tables.Name As TableName , \n" +
"                Sys.Tables.Object_Id As Table_Object_Id ,\n" +
"                Sys.Triggers.Name As Trigger_Name, \n" +
"                Sys.Triggers.Object_Id As Trigger_Object_Id \n" +
"            From Sys.Tables \n" +
"            INNER Join Sys.Triggers On ( Sys.Triggers.Parent_id = Sys.Tables.Object_Id )\n" +
"            Where ( Sys.Tables.Is_MS_Shipped = 0 )\n" +
"        ),\n" +
"  \n" +
"        AllObject\n" +
"        AS\n" +
"        (\n" +
"            SELECT * FROM TableTrigger\n" +
"        )\n" +
"\n" +
"\n" +
"    Select \n" +
"        a.Trigger_Name, a.TableName, tri.is_disabled, e.type_desc\n" +
"    From AllObject a\n" +
"	Left join sys.events e\n" +
"	ON a.Trigger_Object_Id = e.object_id\n" +
"	left join sys.triggers tri\n" +
"	ON a.Trigger_Object_Id = tri.object_id\n" +
"    Order By Trigger_Name;";
        
        try{
            Statement stmt = this.con.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            ArrayList<Object[]> listaTrigg = new ArrayList<>();
            ArrayList<String> nombres = new ArrayList<>();
            while (rs.next()) {
                if(nombres.contains(rs.getString("Trigger_Name")) == false){
                    Object arrAux[] = new Object[6];
                    arrAux[0] = rs.getString("Trigger_Name");
                    arrAux[1] = rs.getString("TableName");
                    if(rs.getInt("is_disabled") == 0){
                        arrAux[2] = true;
                    }else{
                        arrAux[2] = false;
                    }
                    
                    arrAux[3] = false;
                    arrAux[4] = false;
                    arrAux[5] = false;
                    
                    if(rs.getString("type_desc").equals("INSERT")){
                        arrAux[3] = true;
                    }else if(rs.getString("type_desc").equals("UPDATE")){
                        arrAux[4] = true;
                    }else{
                        arrAux[5] = true;
                    }
                    
                    listaTrigg.add(arrAux);
                    nombres.add(rs.getString("Trigger_Name"));
  
                }else{
                    int indEnc = nombres.indexOf(rs.getString("Trigger_Name"));
                    Object auxarrr[] = listaTrigg.get(indEnc);
                    if(rs.getString("type_desc").equals("INSERT")){
                        auxarrr[3] = true;
                    }else if(rs.getString("type_desc").equals("UPDATE")){
                        auxarrr[4] = true;
                    }else{
                        auxarrr[5] = true;
                    }
                }
  
            }
            
            return listaTrigg;
                
        }catch(Exception e){
            return null;
        }
        
        
    }
    
    
    
}

