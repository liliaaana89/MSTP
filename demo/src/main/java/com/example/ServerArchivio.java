package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerArchivio {
    public static void main(String[] args) {
        
        //AVVIO
        try {
            
            ServerSocket server = new ServerSocket(3000);

            System.out.println("Server avviato sulla porta 3000");

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

    //GESTIONE DEL CLIENT
    public static void gestisciClient(Socket client) throws IOException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));

            PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);

            //RICECIONE DELLA RICHIESTA
            String richiesta = in.readLine();

            if(richiesta == null) {
                return;
            }
            System.out.println(richiesta);
            richiesta = richiesta.trim();

            //CONTOLLO DELLA SINTASSI
            if (richiesta.isEmpty()){
                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            String[] parti = richiesta.split("\\s+");
            String comando = parti[0];

            //CONTROLLO DEL COMANDO
            if(!comando.equals("LEGGI")){
                rispostaErrore(out, "ERR UNKNOWWNCMD");
                return;
            }

            if(parti.length != 2) {
                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            //CONTROLLO DEL NOME DEL FILE
            String nomeFile = parti[1];

            if(nomeFile.contains("/") || nomeFile.contains("\\") || nomeFile.contains("..") || !nomeFile.endsWith(".txt")){
                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            //RICERCA DEL FILE
            File cartella = new File("documenti");
            File file = new File(cartella, nomeFile);

            if(!file.isFile()){
                rispostaErrore(out, "ERR SYNTAX");
                return;
            }

            //APERTUTA E INVIO DEL CONTENUTO
            boolean statoInviato = false;

            try(BufferedReader lettoreFile = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                out.println("OK FILE");
                out.println();;

                statoInviato = true;
                String riga;

                while ((riga = lettoreFile.readLine()) != null){
                    out.println(riga);
                }
                
            } catch (IOException e) {
                //Se OK FILE è gia stato inviato, non invio un altro stato
                if(!statoInviato) {
                    rispostaErrore(out, "ERR READ");
                } else {
                    System.out.println("Errore lettura: " + e.getMessage());
                }
            }
        } finally {
            client.close();
        }
    }

    //RISPOSTE DI ERRORE
    public static void rispostaErrore(PrintWriter out, String errore) {
        out.println(errore);
        out.println();
    }   
}
