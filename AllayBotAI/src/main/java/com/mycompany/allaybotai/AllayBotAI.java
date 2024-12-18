/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.allaybotai;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.swing.UIManager;
import org.json.JSONObject;

/**
 *
 * @author jez
 */
public class AllayBotAI {

    private void intentoAPI() {
        try {
            //variables para el modelo
            String nombremodelo = "llama3.2";
            String promptText = "What is milk?";

            URL url = new URL("http://localhost:11434/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = String.format(
                    "{\"model\": \"%s\", \"prompt\": \"%s\", \"stream\": false}", nombremodelo, promptText
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            System.out.println("Codigo de respuesta: " + code);

            if (code == 404) {
                System.out.println("El recurso solicitado no fue encontrado (404). Verifica el endpoint y el servidor.");
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                //imprimir la respuesta
                System.out.println("Response body " + response.toString());
                JSONObject jsonResponse = new JSONObject(response.toString());
                String responseText = jsonResponse.getString("response");
                System.out.println("Response " + responseText);
            }

            //cerrar la conexión
            conn.disconnect();

        } catch (MalformedURLException e) {
            System.out.println("La URL es inválida: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        //UI
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        MenuBienvenida menu = new MenuBienvenida();
        menu.setVisible(true);
    }
}
