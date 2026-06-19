# SoundTribe - Progetto Ingegneria del Software (Traccia 2)
Autori: Lorenzo Marchiori (VR503895), Enzo Rollo (VR500994)
Anno Accademico: 2024/2025

## 1. Struttura del Progetto
```text
Soundtribe/
├── .idea/
├── .git/
├── .github/
├── .mvn/
├── src/
│   └── main/
│       ├── java/
│       │   ├── com/
│       │   │   └── example/
│       │   │       └── soundtribe/
│       │   │           ├── controller/
│       │   │           │   ├── AddSongController.java
│       │   │           │   ├── AdminController.java
│       │   │           │   ├── AdminUserController.java
│       │   │           │   ├── AuthenticationController.java
│       │   │           │   ├── DizionariController.java
│       │   │           │   ├── ExploreConcertController.java
│       │   │           │   ├── ExploreExecutionController.java
│       │   │           │   ├── ExploreSongController.java
│       │   │           │   ├── HomeController.java
│       │   │           │   ├── ProfileController.java
│       │   │           │   ├── RegistrationController.java
│       │   │           │   ├── SongsController.java
│       │   │           │   ├── UploadController.java
│       │   │           │   ├── UserProfileViewController.java
│       │   │           │   └── UsersController.java
│       │   │           ├── dao/
│       │   │           │   ├── CommentDAO.java
│       │   │           │   ├── ConcertDAO.java
│       │   │           │   ├── ExecutionDAO.java
│       │   │           │   ├── SongDAO.java
│       │   │           │   └── UserDAO.java
│       │   │           ├── entità/
│       │   │           │   ├── Comment.java
│       │   │           │   ├── Concert.java
│       │   │           │   ├── ConcertTrack.java
│       │   │           │   ├── Execution.java
│       │   │           │   ├── ExecutionSegment.java
│       │   │           │   ├── Instrument.java
│       │   │           │   ├── MusicItem.java
│       │   │           │   ├── Song.java
│       │   │           │   └── User.java
│       │   │           ├── item/
│       │   │           │   ├── AlertUtil.java
│       │   │           │   └── UserSession.java
│       │   │           ├── manager/
│       │   │           │   ├── CommentManager.java
│       │   │           │   ├── NavigationManager.java
│       │   │           │   └── SceneManager.java
│       │   │           └── Launcher.java
│       │   └── module-info.java
│       └── resources/
│           └── com/
│               └── example/
│                   └── soundtribe/
│                       ├── css/
│                       │   └── style.css
│                       ├── img/
│                       │   ├── soundtribe-logo.png
│                       │   └── user.png
│                       └── view/
│                           ├── aggiungiBrano.fxml
│                           ├── Amministrazione.fxml
│                           ├── AmministrazioneUtente.fxml
│                           ├── Autenticazione.fxml
│                           ├── braniMusicali.fxml
│                           ├── caricaMateriale.fxml
│                           ├── dizionari.fxml
│                           ├── esploraBrani.fxml
│                           ├── esploraConcerto.fxml
│                           ├── esploraEsecuzione.fxml
│                           ├── gestioneUtenti.fxml
│                           ├── GestioneProfilo.fxml
│                           ├── Home.fxml
│                           ├── profiloUtente.fxml
│                           └── Registrazione.fxml
├── target/
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## 2. Requisiti e Dipendenze
Per compilare ed eseguire correttamente l'applicazione, è necessario avere installato sul proprio ambiente:
* Java Development Kit (JDK) versione 21 (o superiore).
* JavaFX SDK (versione compatibile con il JDK in uso).
* Database PostgreSQL in esecuzione in locale.
* Driver JDBC per PostgreSQL (incluso nel `pom.xml`).
* Apache Maven.

## 3. Configurazione del Database (IMPORTANTE)
Prima di avviare l'applicazione, è obbligatorio configurare il database locale:
1. Creare un database PostgreSQL vuoto (es. `soundtribe`).
2. Aprire il file `src/main/java/com/example/soundtribe/dao/CredDAO.java`.
3. Modificare le costanti di connessione (`DB_URL`, `USER`, `PASSWORD`) inserendo le proprie credenziali locali.
*Nota: La creazione delle tabelle è gestita in automatico dai Data Access Object al primo avvio.*

## 4. Come Compilare ed Eseguire

### Opzione A: Tramite IDE (IntelliJ IDEA / Eclipse) - Consigliata
1. Estrarre l'archivio ZIP.
2. Aprire l'IDE e selezionare "Open" o "Import Project", scegliendo la cartella principale del progetto.
3. L'IDE dovrebbe riconoscere automaticamente il `pom.xml` e configurare il progetto come progetto Maven.
4. Eseguire la classe principale: `src/main/java/com/example/soundtribe/Launcher.java`.

### Opzione B: Tramite riga di comando (Terminale)
1. Aprire il terminale e navigare nella cartella radice del progetto.
2. Eseguire il comando di pulizia e compilazione:
   ```sh
   mvn clean install
   ```
3. Eseguire il comando di avvio dell'applicazione:
   ```sh
   mvn javafx:run
   ```
In alternativa, è possibile eseguire entrambi i comandi in un'unica riga:
```sh
.\mvnw.cmd clean javafx:run
```
