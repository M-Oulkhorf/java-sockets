package Socket;

import java.io.*;
import java.net.Socket;

public class Client3 {
    public static void main(String[] zero) {
        try {
            Socket socket = new Socket("localhost", 2024);

            // Flux pour lire les messages du serveur
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Flux pour envoyer des messages au serveur
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Demander le login et le mot de passe
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(in.readLine()); // Affiche "Entrez votre login :"
            String login = consoleInput.readLine();
            out.println(login); // Envoyer le login au serveur

            System.out.print(in.readLine()); // Affiche "Entrez votre mot de passe :"
            String password = consoleInput.readLine();
            out.println(password); // Envoyer le mot de passe au serveur

            // Lire la réponse du serveur (authentification réussie ou échouée)
            String authResponse = in.readLine();
            System.out.println(authResponse);

            if (authResponse.startsWith("Authentification réussie")) {
                // Afficher la liste des clients connectés
                System.out.println(in.readLine()); // Affiche "Clients connectés : ..."

                // Afficher l'historique des messages publics
                System.out.println(in.readLine()); // Affiche l'historique des messages publics

                // Thread pour lire les messages du serveur
                Thread readThread = new Thread(() -> {
                    try {
                        String serverMessage;
                        while ((serverMessage = in.readLine()) != null) {
                            System.out.println(serverMessage);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                readThread.start();

                // Menu pour envoyer des messages ou des commandes
                while (true) {
                    System.out.println("Entrez une commande (/createsalon, /invite, /joinsalon, /listsalons) ou un message (destinataire:message) :");
                    String input = consoleInput.readLine();

                    // Envoyer l'entrée au serveur
                    out.println(input);
                }
            } else {
                System.out.println("Connexion refusée. Veuillez vérifier votre login et mot de passe.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}