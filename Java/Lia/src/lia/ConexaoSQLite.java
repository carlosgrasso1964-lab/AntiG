package lia;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexaoSQLite {

    private static final String URL = "jdbc:sqlite:lia_memoria.db";

    public static Connection conectar() {

        try {

            Class.forName("org.sqlite.JDBC"); // força carregar driver

            Connection conn = DriverManager.getConnection(URL);

            return conn;

        } catch (Exception e) {

            System.out.println("Erro ao conectar SQLite: " + e.getMessage());
            return null;

        }

    }
}