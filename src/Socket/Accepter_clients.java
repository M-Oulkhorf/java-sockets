package Socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

class Accepter_clients implements Runnable {
    private ServerSocket socketserver;
    private Map<String, PrintWriter> clients = new HashMap<>(); // Stocke les clients avec leur nom
    private Map<String, Set<String>> salons = new HashMap<>(); // Stocke les salons et leurs membres
    private Map<String, String> pendingInvitations = new HashMap<>(); // Stocke les invitations en attente
    private List<String> publicMessageHistory = new ArrayList<>(); // Historique des messages publics
    private Map<String, String> users = new HashMap<>(); // Stocke les utilisateurs valides (login:password)

    public Accepter_clients(ServerSocket s) {
        socketserver = s;
        loadUsers(); // Charger les utilisateurs depuis le fichier
    }

    // Charger les utilisateurs depuis le fichier utilisateurs.txt
    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader("utilisateurs.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier utilisateurs.txt : " + e.getMessage());
        }
    }

    // Vérifier si le login et le mot de passe sont valides
    private boolean authenticate(String login, String password) {
        return users.containsKey(login) && users.get(login).equals(password);
    }

    public void run() {
        try {
            while (true) {
                Socket socket = socketserver.accept(); // Un client se connecte
                System.out.println("Un client est connecté !");

                // Créer un BufferedReader et un PrintWriter pour ce client
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // Demander le login et le mot de passe
                out.println("Entrez votre login :");
                String login = in.readLine();
                out.println("Entrez votre mot de passe :");
                String password = in.readLine();

                // Vérifier l'authentification
                if (authenticate(login, password)) {
                    out.println("Authentification réussie. Bienvenue, " + login + " !");
                    clients.put(login, out); // Ajouter le client à la liste

                    System.out.println("Le client " + login + " est connecté.");

                    // Envoyer l'historique des messages publics au nouveau client
                    sendPublicMessageHistory(out);

                    // Envoyer la liste des clients connectés à ce client
                    sendClientList(out);

                    // Notifier tous les clients de la nouvelle connexion
                    broadcast(login + " s'est connecté.", "all");

                    // Démarrer un nouveau thread pour gérer ce client
                    Thread clientThread = new Thread(() -> handleClient(socket, in, out, login));
                    clientThread.start();
                } else {
                    out.println("Erreur : Login ou mot de passe incorrect.");
                    socket.close(); // Fermer la connexion si l'authentification échoue
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket, BufferedReader in, PrintWriter out, String clientName) {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                // Traiter les commandes spéciales
                if (message.startsWith("/")) {
                    String[] parts = message.split(" ", 2);
                    String command = parts[0];
                    String argument = parts.length > 1 ? parts[1] : "";

                    switch (command) {
                        case "/createsalon":
                            createSalon(clientName, argument);
                            break;
                        case "/invite":
                            inviteToSalon(clientName, argument);
                            break;
                        case "/joinsalon":
                            joinSalon(clientName, argument);
                            break;
                        case "/listsalons":
                            listSalons(out);
                            break;
                        default:
                            out.println("Commande inconnue : " + command);
                            break;
                    }
                } else {
                    // Envoyer un message à un salon ou à un utilisateur
                    String[] parts = message.split(":", 2);
                    if (parts.length == 2) {
                        String recipient = parts[0];
                        String content = parts[1];

                        if (recipient.equalsIgnoreCase("all")) {
                            // Ajouter le message à l'historique des messages publics
                            String publicMessage = clientName + " (à tous) : " + content;
                            publicMessageHistory.add(publicMessage);

                            // Diffuser le message à tous les clients
                            broadcast(publicMessage, clientName);
                        } else if (salons.containsKey(recipient) && salons.get(recipient).contains(clientName)) {
                            // Envoyer le message aux membres du salon
                            sendToSalon(recipient, clientName + " : " + content);
                        } else {
                            // Envoyer un message privé à un utilisateur spécifique
                            sendPrivateMessage(clientName, recipient, content);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Retirer le client de la liste lorsqu'il se déconnecte
            clients.remove(clientName);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Le client " + clientName + " s'est déconnecté.");

            // Notifier tous les clients de la déconnexion
            broadcast(clientName + " s'est déconnecté.", "all");

            // Retirer le client de tous les salons
            for (Set<String> members : salons.values()) {
                members.remove(clientName);
            }

            // Mettre à jour la liste des clients pour tous
            updateClientListForAll();
        }
    }
    private void createSalon(String clientName, String salonName) {
        if (salons.containsKey(salonName)) {
            clients.get(clientName).println("Erreur : Le salon " + salonName + " existe déjà.");
        } else {
            salons.put(salonName, new HashSet<>(Collections.singletonList(clientName)));
            clients.get(clientName).println("Salon " + salonName + " créé avec succès.");
        }
    }

    private void inviteToSalon(String inviter, String inviteeAndSalon) {
        String[] parts = inviteeAndSalon.split(" ", 2);
        if (parts.length == 2) {
            String invitee = parts[0];
            String salonName = parts[1];

            if (salons.containsKey(salonName) && salons.get(salonName).contains(inviter)) {
                if (clients.containsKey(invitee)) {
                    pendingInvitations.put(invitee, salonName);
                    clients.get(invitee).println("Invitation à rejoindre le salon " + salonName + " par " + inviter + ". Tapez /joinsalon " + salonName + " pour accepter.");
                } else {
                    clients.get(inviter).println("Erreur : L'utilisateur " + invitee + " n'existe pas.");
                }
            } else {
                clients.get(inviter).println("Erreur : Vous n'êtes pas membre du salon " + salonName);
            }
        } else {
            clients.get(inviter).println("Usage : /invite <nom_utilisateur> <nom_salon>");
        }
    }

    private void joinSalon(String clientName, String salonName) {
        if (pendingInvitations.containsKey(clientName) && pendingInvitations.get(clientName).equals(salonName)) {
            salons.get(salonName).add(clientName);
            clients.get(clientName).println("Vous avez rejoint le salon " + salonName);
            pendingInvitations.remove(clientName);
        } else {
            clients.get(clientName).println("Erreur : Vous n'avez pas d'invitation pour le salon " + salonName);
        }
    }

    private void listSalons(PrintWriter out) {
        if (salons.isEmpty()) {
            out.println("Aucun salon disponible.");
        } else {
            out.println("Salons disponibles :");
            for (String salon : salons.keySet()) {
                out.println("- " + salon);
            }
        }
    }

    private void sendToSalon(String salonName, String message) {
        String formattedMessage = "[" + salonName + "] " + message;
        for (String member : salons.get(salonName)) {
            clients.get(member).println(formattedMessage);
        }
    }

    private void broadcast(String message, String sender) {
        for (Map.Entry<String, PrintWriter> entry : clients.entrySet()) {
            if (!entry.getKey().equals(sender)) { // Ne pas envoyer le message à l'expéditeur
                entry.getValue().println(message);
            }
        }
    }

    private void sendPrivateMessage(String sender, String recipient, String message) {
        PrintWriter recipientWriter = clients.get(recipient);
        if (recipientWriter != null) {
            recipientWriter.println(sender + " (privé) : " + message);
        } else {
            clients.get(sender).println("Erreur : Le destinataire " + recipient + " n'existe pas.");
        }
    }

    private void sendClientList(PrintWriter out) {
        StringBuilder clientList = new StringBuilder("Clients connectés : ");
        for (String client : clients.keySet()) {
            clientList.append(client).append(", ");
        }
        out.println(clientList.toString());
    }

    private void updateClientListForAll() {
        for (PrintWriter out : clients.values()) {
            sendClientList(out);
        }
    }

    private void sendPublicMessageHistory(PrintWriter out) {
        out.println("=== Historique des messages publics ===");
        for (String message : publicMessageHistory) {
            out.println(message);
        }
        out.println("=====================================");
    }
}