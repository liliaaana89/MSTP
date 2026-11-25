package com.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ServerArchivio {

    // ESTENSIONI AMMESSE
    private static final String[] ESTENSIONI = {"txt", "md", "csv"};

    public static void main(String[] args) {

        // AVVIO DEL SERVER
        try {
            ServerSocket server = new ServerSocket(3000);

            System.out.println("Server avviato sulla porta 3000");

            // ACCETTAZIONE DELLE CONNESSIONI
            while (true) {

                Socket client = server.accept();

                try {
                    gestisciClient(client);
                } catch (IOException e) {
                    System.out.println("Errore client: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Errore server: " + e.getMessage());
        }
    }

    // GESTIONE DEL CLIENT
    public static void gestisciClient(Socket client) throws IOException {

        try {

            // CREAZIONE DEGLI STREAM
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    client.getInputStream(),
                    StandardCharsets.UTF_8
                )
            );

            PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                    client.getOutputStream(),
                    StandardCharsets.UTF_8
                ), true
            );

            // RICEZIONE DELLA RICHIESTA
            String richiesta = in.readLine();

            if (richiesta == null) {
                return;
            }

            System.out.println(richiesta);

            richiesta = richiesta.trim();

            // CONTROLLO DELLA SINTASSI
            if (richiesta.isEmpty()) {
                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            String[] parti = richiesta.split("\\s+");
            String comando = parti[0];

            // CONTROLLO DEL COMANDO
            if (!comando.equals("LEGGI")) {
                rispostaErrore(out, "ERR UNKNOWNCMD");
                return;
            }

            if (parti.length != 2) {
                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            // CONTROLLO DEL NOME SENZA ESTENSIONE
            String nomeFile = parti[1];

            if (nomeFile.contains(".") ||
                nomeFile.contains("/") ||
                nomeFile.contains("\\")) {

                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            // RICERCA DEL FILE CON LE ESTENSIONI AMMESSE
            File cartella = new File("demo/documenti");

            System.out.println("Cartella di lavoro: " + System.getProperty("user.dir"));
            System.out.println("Percorso documenti: " + cartella.getAbsolutePath());


            File file = null;
            String tipo = "";

            for (String estensione : ESTENSIONI) {

                File trovato = new File(
                    cartella, nomeFile + "." + estensione
                );

                if (trovato.isFile()) {
                    file = trovato;
                    tipo = estensione;
                    break;
                }
            }

            // CONTROLLO DELLA PRESENZA DEL FILE
            if (file == null) {
                rispostaErrore(out, "ERR NOTFOUND");
                return;
            }

            // APERTURA E INVIO DEL CONTENUTO
            boolean statoInviato = false;

            try (BufferedReader lettoreFile = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(file),
                    StandardCharsets.UTF_8
                )
            )) {

                // INVIO DELLO STATO E DELL'INTESTAZIONE
                out.println("OK FILE");
                out.println("TYPE " + tipo);
                out.println();

                statoInviato = true;

                // INVIO DELLE RIGHE DEL FILE
                String riga;

                while ((riga = lettoreFile.readLine()) != null) {
                    out.println(riga);
                }

            } catch (IOException e) {

                if (!statoInviato) {
                    rispostaErrore(out, "ERR READ");
                } else {
                    System.out.println("Errore lettura: " + e.getMessage());
                }
            }

        } finally {

            // CHIUSURA DELLA CONNESSIONE
            client.close();
        }
    }

    // GESTIONE DELLE RISPOSTE DI ERRORE
    public static void rispostaErrore(PrintWriter out, String errore) {

        out.println(errore);
        out.println();
    }
}
