/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integridadreferencialchecker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

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

}
