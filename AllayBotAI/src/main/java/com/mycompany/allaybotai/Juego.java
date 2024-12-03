package com.mycompany.allaybotai;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.json.JSONObject;

public class Juego extends javax.swing.JFrame {

    private int jugadorFila = 0;
    private int jugadorColumna = 0;
    public int turnosRestantes = 25;

    private final int gridSize = 5; // Tamaño del mapa 5x5
    private final int[][] mapa = new int[gridSize][gridSize];
    private int tesorosEncontrados = 0;
    private final Random random = new Random();

    public Juego() {
        initComponents();
        generarMapa();
        Botones();
        setIconImage(new ImageIcon(getClass().getResource("/images/tesoro1_icon.png")).getImage());
    }

    private void Botones() {
        crearBotones(0, 1);
    }

    // Metodo recursivo para los botones
    private void crearBotones(int index, int numero) {
        if (index >= gridSize * gridSize) {
            return; // Caso base cuando ya se han creado todos los botones
        }

        final int x = index / gridSize;
        final int y = index % gridSize;

        javax.swing.JButton boton = new javax.swing.JButton(Integer.toString(numero));
        boton.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                explorarCelda(x, y);
            }
        });

        Mapa.add(boton);

        crearBotones(index + 1, numero + 1);
    }

    private void generarMapa() {
        generarTesoros(5);
        generarTrampas(3);
    }

    // Metodo recursivo para los tesoros
    private void generarTesoros(int cantidad) {
        if (cantidad == 0) {
            return; // Caso base cuando ya se generaron todos los tesoros
        }
        int x = random.nextInt(gridSize);
        int y = random.nextInt(gridSize);
        if (mapa[x][y] == 0) {
            mapa[x][y] = 1;
            generarTesoros(cantidad - 1); // Llamada recursiva
        } else {
            generarTesoros(cantidad);
        }
    }

    // Metodo recursivo para las trampas
    private void generarTrampas(int cantidad) {
        if (cantidad == 0) {
            return; // Caso base cuando ya se generaron todas las trampas
        }
        int x = random.nextInt(gridSize);
        int y = random.nextInt(gridSize);
        if (mapa[x][y] == 0) {
            mapa[x][y] = -1;
            generarTrampas(cantidad - 1); // Llamada recursiva
        } else {
            generarTrampas(cantidad);
        }
    }

    private void enviarSolicitudIA(String texto) throws ProtocolException, IOException {
        String nombremodelo = "llama3.2";
        StringBuilder mapaTexto = new StringBuilder("Mapa del Juego (5x5):\n");
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (mapa[i][j] == 1) {
                    mapaTexto.append(". ");
                } else if (mapa[i][j] == -1) {
                    mapaTexto.append(". ");
                } else if (i == jugadorFila && j == jugadorColumna) {
                    mapaTexto.append("P ");
                } else {
                    mapaTexto.append(". ");
                }
            }
            mapaTexto.append("\n");
        }

        // Calcular la celda actual
        int celdaActual = (jugadorFila * gridSize) + jugadorColumna + 1;
        StringBuilder adyacentes = new StringBuilder();

        // Direcciones: arriba, abajo, izquierda, derecha
        if (jugadorFila > 0) {
            adyacentes.append("Arriba: Celda " + ((jugadorFila - 1) * gridSize + jugadorColumna + 1) + "\n");
        }
        if (jugadorFila < gridSize - 1) {
            adyacentes.append("Abajo: Celda " + ((jugadorFila + 1) * gridSize + jugadorColumna + 1) + "\n");
        }
        if (jugadorColumna > 0) {
            adyacentes.append("Izquierda: Celda " + (jugadorFila * gridSize + jugadorColumna - 1 + 1) + "\n");
        }
        if (jugadorColumna < gridSize - 1) {
            adyacentes.append("Derecha: Celda " + (jugadorFila * gridSize + jugadorColumna + 1 + 1) + "\n");
        }

        // Direcciones diagonales
        if (jugadorFila > 0 && jugadorColumna > 0) {
            adyacentes.append("Diagonal superior-izquierda: Celda " + ((jugadorFila - 1) * gridSize + jugadorColumna - 1 + 1) + "\n");
        }
        if (jugadorFila > 0 && jugadorColumna < gridSize - 1) {
            adyacentes.append("Diagonal superior-derecha: Celda " + ((jugadorFila - 1) * gridSize + jugadorColumna + 1 + 1) + "\n");
        }
        if (jugadorFila < gridSize - 1 && jugadorColumna > 0) {
            adyacentes.append("Diagonal inferior-izquierda: Celda " + ((jugadorFila + 1) * gridSize + jugadorColumna - 1 + 1) + "\n");
        }
        if (jugadorFila < gridSize - 1 && jugadorColumna < gridSize - 1) {
            adyacentes.append("Diagonal inferior-derecha: Celda " + ((jugadorFila + 1) * gridSize + jugadorColumna + 1 + 1) + "\n");
        }

        StringBuilder advertencias = new StringBuilder();
        if (jugadorFila > 0 && mapa[jugadorFila - 1][jugadorColumna] == -1) {
            advertencias.append("Evita moverte hacia la celda arriba.\n");
        }
        if (jugadorFila < gridSize - 1 && mapa[jugadorFila + 1][jugadorColumna] == -1) {
            advertencias.append("Evita moverte hacia la celda abajo.\n");
        }
        if (jugadorColumna > 0 && mapa[jugadorFila][jugadorColumna - 1] == -1) {
            advertencias.append("Evita moverte hacia la celda izquierda.\n");
        }
        if (jugadorColumna < gridSize - 1 && mapa[jugadorFila][jugadorColumna + 1] == -1) {
            advertencias.append("Evita moverte hacia la celda derecha.\n");
        }
        if (jugadorFila > 0 && mapa[jugadorFila - 1][jugadorColumna] == 1) {
            advertencias.append("Quizás quieras explorar la celda superior.\n");
        }
        if (jugadorFila < gridSize - 1 && mapa[jugadorFila + 1][jugadorColumna] == 1) {
            advertencias.append("Quizás quieras explorar la celda inferior.\n");
        }
        if (jugadorColumna > 0 && mapa[jugadorFila][jugadorColumna - 1] == 1) {
            advertencias.append("Quizás quieras explorar la celda izquierda.\n");
        }
        if (jugadorColumna < gridSize - 1 && mapa[jugadorFila][jugadorColumna + 1] == 1) {
            advertencias.append("Quizás quieras explorar la celda derecha.\n");
        }

        advertencias.append("Vas a decirme la celda actual en la que estoy sin decir que yo te lo dije, la cual es " + celdaActual + " y quiero que no supongas que estoy mirando a alguna celda necesito que me ayudes en mi aventura, Necesito que no seas tan directa con las pistas, pero que tambien me ayudes a donde no tengo que ir y a donde si tengo que ir SIN decirlo directamente osea que no me vas a decir las cosas tan directo, las diras de manera de pista o sutiles, no me digas que hacer, quiero que me des pistas y cosas utiles que me sirvan en esta aventura, no me hagas preguntas en tu respuesta porque no podre responderlas, recuerda no me hagas preguntas, sin darme mas preguntas por favor, nada de preguntas hacia mi, recuerda no hacerme preguntas, recuerda no hacerme preguntas a mi.\n");

        String mapa = "\n1   2   3   4   5\n"
                + "6   7   8   9   10\n"
                + "11  12  13  14  15\n"
                + "16  17  18  19  20\n"
                + "21  22  23  24  25\n";

        advertencias.append("\nUn ejemplo del mapa donde estoy yo es asi: " + mapa + " recuerda que es el mapa donde estoy yo\n");
        advertencias.append("\nTambien recuerda que me vas a dar pistas e indicaciones de donde puedo ir yo, no me hagas preguntas a mi de ningun tipo, solo dime pistas e indicaciones sin ser tan directo de donde puedo ir, dime con pistas informacion de las celdas vecinas.\n");

        String mensajeFinal = mapaTexto.toString() + "\nPosición actual: Celda " + celdaActual + "\n" + adyacentes.toString() + "\n" + advertencias.toString() + "\nSigue explorando, ten cuidado con los obstáculos.\n";

        try {
            URL url = new URL("http://localhost:11434/api/generate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            JSONObject json = new JSONObject();
            json.put("model", nombremodelo);
            json.put("prompt", mensajeFinal);
            json.put("stream", false);

            String jsonInputString = json.toString();
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code >= 400) {
                JOptionPane.showMessageDialog(this, "Error al conectarse a la IA. Código: " + code);
            } else {
                BufferedReader in = new BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String responseText = jsonResponse.getString("response");

                // Mostrar la respuesta de la IA
                Allay.setText("[AI] Allay Bot: " + responseText + "\n");
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            Allay.append("[ERROR] URL inválida: " + e.getMessage() + "\n");
        } catch (IOException e) {
            Allay.append("[ERROR] Conexión fallida: " + e.getMessage() + "\n");
        }
    }

    private void explorarCelda(int fila, int columna) {
        jugadorFila = fila;
        jugadorColumna = columna;

        // Actualizar la etiqueta con el número de la celda
        Posicion.setText("Celda: " + (fila * gridSize + columna + 1));

        if (turnosRestantes <= 0) {
            JOptionPane.showMessageDialog(null, "¡No tienes más turnos! Fin del juego.");
            finalizarJuego();
            return;
        }

        int celdaActual = mapa[fila][columna]; // Valor de la celda en el mapa

        if (celdaActual == 1) { // Tesoro encontrado
            int decrementoms = 1;//para usar en la funcion recursiva

            tesorosEncontrados = tesorosmas(tesorosEncontrados, decrementoms);//recursividad
            mapa[fila][columna] = 0; // Eliminar el tesoro del mapa
            JOptionPane.showMessageDialog(null, "¡Encontraste un tesoro!");
            Tesoros.setText("Tesoros encontrados: " + tesorosEncontrados);
            turnosRestantes = trampa(turnosRestantes, decrementoms);
            Turnos.setText("Turnos restantes: " + turnosRestantes);

        } else if (celdaActual == -1) { // Trampa encontrada
            int decrementot = 2;// Decrementos para la funciones recursivas de los reductores de los turnos en las trampas
            int decrementom = 1;// Decrementoss para la funcion recursiva de los reductores de tesoros en la trampas

            JOptionPane.showMessageDialog(null, "¡Pisas una trampa! Pierdes un turno");
            turnosRestantes = trampa(turnosRestantes, decrementot);
            Turnos.setText("Turnos restantes: " + turnosRestantes);

            if (tesorosEncontrados > 0) {
                JOptionPane.showMessageDialog(null, "Pierdes un tesoro."); //para decontar los tesoros aparte
                tesorosEncontrados = tesorosmenos(tesorosEncontrados, decrementom);
                Tesoros.setText("Tesoros encontrados: " + tesorosEncontrados);
            }

            if (turnosRestantes <= 0) {
                JOptionPane.showMessageDialog(null, "¡No tienes más turnos! Fin del juego.");
                finalizarJuego();
                return;
            }

        } else { // Celda vacía
            int decrementosv = 1;
            JOptionPane.showMessageDialog(null, "Nada en esta celda.");
            turnosRestantes = trampa(turnosRestantes, decrementosv);//recursividad
            Turnos.setText("Turnos restantes: " + turnosRestantes);
        }

        // Verificar si se han encontrado todos los tesoros
        if (tesorosEncontrados == 5) {
            JOptionPane.showMessageDialog(null, "¡Has encontrado todos los tesoros! ¡Ganaste!");
            finalizarJuego();
        }
    }

    //Funcion recursiva para ir quitando turnos extra cuando caes en una trampa
    private static int trampa(int turnosRestantes, int decremento) {
        if (decremento <= 0) {
            return turnosRestantes;
        }
        return trampa(turnosRestantes - 1, decremento - 1);
    }

    //Funcion recursiva para ir quitando los tesoros cuando caes en una trampa
    private static int tesorosmenos(int tesorosEncontrados, int decrementosm) {
        if (decrementosm <= 0) {
            return tesorosEncontrados;
        }
        return tesorosmenos(tesorosEncontrados - 1, decrementosm - 1);
    }

    //Funcion recursiva para ir dando los tesoros cuando lso encuentras
    private static int tesorosmas(int tesorosEncontrados, int decrementosms) {
        if (decrementosms <= 0) {
            return tesorosEncontrados;
        }
        return tesorosmenos(tesorosEncontrados + 1, decrementosms - 1);
    }

    private void finalizarJuego() {
        deshabilitarComponentes(0);
    }

    // Metodo recursivo para los componentes
    private void deshabilitarComponentes(int index) {
        if (index >= Mapa.getComponentCount()) {
            return; // Caso base cuando ya se recorrieron todos los componentes
        }
        Mapa.getComponent(index).setEnabled(false);
        deshabilitarComponentes(index + 1); // Llamada recursiva
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Mapa = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        Allay = new javax.swing.JTextArea();
        Pistas = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        Tesoros = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        Regresar = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        Posicion = new javax.swing.JLabel();
        Turnos = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AllayQuest");

        Mapa.setBackground(new java.awt.Color(53, 53, 53));
        Mapa.setPreferredSize(new java.awt.Dimension(250, 250));
        Mapa.setLayout(new java.awt.GridLayout(5, 5));

        Allay.setEditable(false);
        Allay.setFocusable(false);
        Allay.setColumns(20);
        Allay.setRows(5);
        jScrollPane1.setViewportView(Allay);

        Pistas.setBackground(new java.awt.Color(0, 153, 153));
        Pistas.setBorder(new javax.swing.border.MatteBorder(null));
        Pistas.setForeground(new java.awt.Color(0, 153, 153));
        Pistas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                PistasMouseClicked(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel1.setText("Pedir Pistas");

        javax.swing.GroupLayout PistasLayout = new javax.swing.GroupLayout(Pistas);
        Pistas.setLayout(PistasLayout);
        PistasLayout.setHorizontalGroup(
            PistasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PistasLayout.createSequentialGroup()
                .addContainerGap(73, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(65, 65, 65))
        );
        PistasLayout.setVerticalGroup(
            PistasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PistasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Tesoros.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        Tesoros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        Tesoros.setText("Tesoros encontrados: 0");

        jLabel3.setFont(new java.awt.Font("Arial", 1, 36)); // NOI18N
        jLabel3.setText("Chronicles of Allay");

        Regresar.setBackground(new java.awt.Color(45, 45, 45));
        Regresar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RegresarMouseClicked(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        jLabel4.setText("Regresar");

        javax.swing.GroupLayout RegresarLayout = new javax.swing.GroupLayout(Regresar);
        Regresar.setLayout(RegresarLayout);
        RegresarLayout.setHorizontalGroup(
            RegresarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RegresarLayout.createSequentialGroup()
                .addContainerGap(33, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(24, 24, 24))
        );
        RegresarLayout.setVerticalGroup(
            RegresarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RegresarLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addContainerGap())
        );

        Posicion.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        Posicion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        Posicion.setText("Celda: Ninguna");
        Posicion.setToolTipText("");

        Turnos.setFont(new java.awt.Font("sansserif", 1, 14)); // NOI18N
        Turnos.setForeground(new java.awt.Color(255, 153, 51));
        Turnos.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        Turnos.setText("Turnos restantes: 25");
        Turnos.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(Pistas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(Posicion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(143, 143, 143)
                        .addComponent(Turnos, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(Tesoros, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Regresar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Mapa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(Regresar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Tesoros, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Posicion, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Turnos, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(Pistas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Mapa, javax.swing.GroupLayout.PREFERRED_SIZE, 476, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 476, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void PistasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PistasMouseClicked
        if (tesorosEncontrados < 5) {
            try {
                enviarSolicitudIA("Dame una pista sobre este área.");
            } catch (IOException ex) {
                Logger.getLogger(Juego.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            JOptionPane.showMessageDialog(this, "¡El juego ha terminado! No necesitas más pistas.");
        }
    }//GEN-LAST:event_PistasMouseClicked

    private void RegresarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RegresarMouseClicked
        MenuBienvenida menu = new MenuBienvenida();
        menu.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_RegresarMouseClicked

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Juego().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea Allay;
    private javax.swing.JPanel Mapa;
    private javax.swing.JPanel Pistas;
    private javax.swing.JLabel Posicion;
    private javax.swing.JPanel Regresar;
    private javax.swing.JLabel Tesoros;
    private javax.swing.JLabel Turnos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
