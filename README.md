# Serveur de Chat avec Authentification

Ce projet est un serveur de chat en Java qui utilise un fichier `utilisateurs.txt` pour gérer l'authentification des utilisateurs. Les utilisateurs doivent fournir un login et un mot de passe pour se connecter au serveur.

## Fonctionnalités

- **Authentification** : Les utilisateurs doivent fournir un login et un mot de passe valides pour se connecter.
- **Chat public et privé** : Les utilisateurs peuvent envoyer des messages à tous (`all`) ou à un utilisateur spécifique.
- **Salons de discussion** : Les utilisateurs peuvent créer des salons, inviter d'autres utilisateurs, et rejoindre des salons.
- **Historique des messages publics** : Les nouveaux utilisateurs reçoivent l'historique des messages publics dès leur connexion.

## Configuration

### Fichier `utilisateurs.txt`

Le fichier `utilisateurs.txt` contient les logins et mots de passe des utilisateurs autorisés. Chaque ligne doit être au format suivant :
login:motdepasse

#### Exemple de contenu

alice:1234 bob:password charlie:secure

### Emplacement du fichier

Placez le fichier `utilisateurs.txt` dans le même répertoire que votre projet Java. Si le fichier n'existe pas, le programme le créera automatiquement.

## Utilisation

### Démarrer le serveur

### Connexion d'un client
Le client sera invité à entrer un login et un mot de passe :

Entrez votre login :
alice
Entrez votre mot de passe :
1234
Si l'authentification réussit, le client peut commencer à chatter.

### Commandes disponibles
Envoyer un message à tous :  
`all:Bonjour tout le monde`  
Envoyer un message privé :  
`bob:Salut Bob`  
Créer un salon :  
`/createsalon Dev`  
Inviter un utilisateur à un salon :  
`/invite bob Dev`  
Rejoindre un salon :  
`/joinsalon Dev`  
Lister les salons disponibles :  
`/listsalons`  
### exemple de contenu de fichier utilisateurs.txt
`alice:1234`  
`bob:password`  
`charlie:secure`  
