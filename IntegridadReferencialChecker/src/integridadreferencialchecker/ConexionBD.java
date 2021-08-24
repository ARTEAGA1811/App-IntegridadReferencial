/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integridadreferencialchecker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author David Arteaga
 */
public class ConexionBD {

    public ConexionBD() {

    }

    //****************************************************
    public static Connection conectarConLaBD() {

        
        String connectionUrl =
                "jdbc:sqlserver://DESKTOP-D65SKR6:1433;"
                        + "database=pubs;"
                        + "user=sa;"
                        + "password=123456;"
                        + "trustServerCertificate=false;"
                        + "loginTimeout=30;";
        
        try {
            Connection con = DriverManager.getConnection(connectionUrl);
            System.out.println("Conexion exitosa a la base de datos.");
            return con;

        } catch (SQLException ex) {
            System.out.println("Error.");
            System.out.println(ex.toString());
            return null;
        }
    }
    
    
    

}
