package com.example.va_prisecommande.database;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import com.google.gson.Gson;

public class DatabaseToJson {
    public static void main(String[] args) {
        // Paramètres de connexion
        String serverName = "192.168.100.11";
        String dbName = "VITAL";
        String url = "jdbc:jtds:sqlserver://" + serverName + ":1433;DatabaseName=" + dbName + ";encrypt=true;trustServerCertificate=true;";
        String user = "apps";
        String password = "apps@VITAL31";

        // Requête SQL (à remplacer par votre requête spécifique)
        String sql = "SELECT prenom, nom FROM votre_table";

        try {
            // Connexion à la base de données
            Connection conn = DriverManager.getConnection(url, user, password);

            // Exécution de la requête
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // Traitement des résultats
            ArrayList<Commercial> commerciaux = new ArrayList<>();
            while (rs.next()) {
                Commercial commercial = new Commercial(rs.getString("prenom"), rs.getString("nom"));
                commerciaux.add(commercial);
            }

            // Conversion en JSON
            String json = new Gson().toJson(commerciaux);

            // Chemin où le fichier sera enregistré
            String filePath = "C:\\Users\\Sabri\\Desktop\\IUT\\Alternance\\Fichiers JSON\\commerciaux.json";

            // Écriture des données dans le fichier
            try (FileWriter file = new FileWriter(filePath)) {
                file.write(json);
                file.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Fermeture des connexions
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Classe pour représenter les données commerciales
    static class Commercial {
        String prenom;
        String nom;

        public Commercial(String prenom, String nom) {
            this.prenom = prenom;
            this.nom = nom;
        }

    }
}
