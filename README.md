# Rubrica Telefonica

Applicazione Java per la gestione di una rubrica telefonica.
Architettura **Spring Boot 3.2.3** con interfaccia desktop **Swing** e **REST API** documentata.

---

## Requisiti di sistema

| Requisito | Versione |
|-----------|---------|
| Java (JDK) | **17** o superiore |
| Maven | **3.6+** |
| Sistema operativo | Windows / macOS / Linux con display (per Swing) |

> **Nota:** Per la modalità solo API (senza UI Swing) non è necessario un display grafico.

---

## Come avviare l'applicazione

### 1. Clona o scarica il progetto

```bash
git clone <url-repository>
cd interview-project
```

### 2. Verifica Java 17

```bash
java -version
# Output atteso: openjdk 17.x.x o simile
```

Se hai più versioni di Java, imposta `JAVA_HOME`:

```bash
# macOS (con JDK Oracle)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home

# Linux
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Windows (PowerShell)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
```

### 3. Avvia l'applicazione (profilo dev - H2 su file)

```bash
mvn spring-boot:run
```

### 3b. Creare il JAR eseguibile

```bash
mvn clean package -DskipTests
```

Il file `Rubrica.jar` viene generato nella **root del progetto**.

### 3c. Eseguire il JAR

```bash
java -Djava.awt.headless=false -jar Rubrica.jar
```

I dati vengono salvati nella cartella `./data/` (H2 su file). La persistenza sopravvive ai riavvii.

Al primo avvio:
- Spring Boot si avvia sulla porta **8080**
- Liquibase crea automaticamente le tabelle `persona` e `utente`
- Viene inserito un utente di default: **admin / admin**
- Si apre la finestra di **Login** (Swing)

### 4. Login

Inserire le credenziali:
- **Username:** `admin`
- **Password:** `admin`

Dopo il login si apre la finestra principale della rubrica.

---

## Profili disponibili

| Profilo | Database | Comando |
|---------|---------|---------|
| `dev` (default) | H2 su file (`./data/rubricadb`) — dati persistenti | `mvn spring-boot:run` |
| `prod` | MySQL | `mvn spring-boot:run -Dspring.profiles.active=prod` |

### Configurazione MySQL (profilo prod)

Modifica `src/main/resources/application-prod.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rubrica?...
spring.datasource.username=TUO_USERNAME
spring.datasource.password=TUA_PASSWORD
```

Poi crea il database MySQL:

```sql
CREATE DATABASE rubrica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Le tabelle vengono create automaticamente da Liquibase al primo avvio.

---

## Interfacce disponibili

| Interfaccia | URL | Descrizione |
|-------------|-----|-------------|
| Swing GUI | (applicazione desktop) | Finestra principale + editor |
| Swagger UI | http://localhost:8080/swagger-ui.html | Documentazione interattiva API |
| OpenAPI JSON | http://localhost:8080/api-docs | Spec OpenAPI 3.0 in JSON |
| H2 Console (dev) | http://localhost:8080/h2-console | Console database H2 (solo dev) |

### Accesso H2 Console

- **JDBC URL:** `jdbc:h2:file:./data/rubricadb`
- **Username:** `sa`
- **Password:** *(lasciare vuoto)*

---

## Eseguire i test

```bash
mvn test
```

I test usano H2 in-memory e girano in modalità headless (senza UI Swing).

```
Tests run: 24
  - PersonaServiceTest:  14 test
  - UtenteServiceTest:   10 test
  - ApplicationTests:     1 test (context load)
```

---

## Importare la collection Postman

1. Aprire **Postman**
2. Click **Import** → selezionare il file `rubrica-postman-collection.json`
3. La variabile `baseUrl` è preimpostata a `http://localhost:8080`
4. Eseguire prima **Auth → Login** per verificare la connessione

---

## Struttura del progetto

```
src/
├── main/
│   ├── java/com/interview/app/
│   │   ├── Application.java              # Entry point Spring Boot
│   │   ├── config/
│   │   │   ├── SecurityConfig.java       # BCrypt + HTTP security
│   │   │   ├── OpenApiConfig.java        # Swagger/OpenAPI config
│   │   │   └── SwingAppRunner.java       # Lancia Swing dopo Spring
│   │   ├── entity/
│   │   │   ├── Persona.java              # Entità JPA
│   │   │   └── Utente.java              # Entità JPA
│   │   ├── dto/                         # Data Transfer Objects (8 classi)
│   │   ├── mapper/                      # MapStruct mappers
│   │   ├── repository/                  # Spring Data JPA repositories
│   │   ├── specification/
│   │   │   └── PersonaSpecification.java # Filtri dinamici
│   │   ├── service/
│   │   │   ├── PersonaService.java       # Interface
│   │   │   ├── UtenteService.java        # Interface
│   │   │   ├── FileExportService.java    # Interface (EXTRA #1)
│   │   │   └── impl/                    # Implementazioni
│   │   ├── controller/
│   │   │   ├── PersonaController.java    # REST /api/v1/persone
│   │   │   └── AuthController.java      # REST /api/v1/auth
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java  # @RestControllerAdvice
│   │   │   └── ...                      # Custom exceptions
│   │   └── ui/                          # Swing UI
│   │       ├── LoginWindow.java          # Finestra login (EXTRA #2)
│   │       ├── MainWindow.java           # Finestra principale + JToolBar
│   │       ├── EditorPersonaDialog.java  # Dialog crea/modifica
│   │       └── PersonaTableModel.java    # AbstractTableModel
│   └── resources/
│       ├── application.properties        # Config base
│       ├── application-dev.properties    # Config H2 (dev)
│       ├── application-prod.properties   # Config MySQL (prod)
│       └── db/changelog/                # Liquibase migrations
└── test/
    └── java/com/interview/app/
        ├── ApplicationTests.java
        └── service/
            ├── PersonaServiceTest.java   # 14 unit test
            └── UtenteServiceTest.java    # 10 unit test
```

---

## API REST — Riepilogo endpoint

### Persona

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `GET` | `/api/v1/persone` | Elenco con filtri e paginazione |
| `GET` | `/api/v1/persone/{id}` | Dettaglio per ID |
| `POST` | `/api/v1/persone` | Crea nuova persona |
| `PUT` | `/api/v1/persone/{id}` | Aggiorna (parziale, null ignorati) |
| `DELETE` | `/api/v1/persone/{id}` | Elimina |
| `POST` | `/api/v1/persone/{id}/export` | Esporta in file TXT |

#### Parametri di filtro (GET /api/v1/persone)

| Parametro | Tipo | Descrizione |
|-----------|------|-------------|
| `nome` | string | Contains case-insensitive |
| `cognome` | string | Contains case-insensitive |
| `telefono` | string | Contains |
| `etaMin` | integer | Età >= etaMin |
| `etaMax` | integer | Età <= etaMax |
| `page` | integer | Numero pagina (default: 0) |
| `size` | integer | Elementi per pagina (default: 10) |
| `sortBy` | string | Campo di ordinamento (default: cognome) |
| `sortDir` | string | `asc` o `desc` (default: asc) |

### Autenticazione

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| `POST` | `/api/v1/auth/login` | Login utente |
| `POST` | `/api/v1/auth/register` | Registra nuovo utente |

---

## Descrizione dei requisiti dell'esercizio

### Requisiti base (Progetto_Rubrica.pdf)

#### Classe Persona
La persona contiene le seguenti informazioni:
- `nome` — stringa, obbligatorio
- `cognome` — stringa, obbligatorio
- `indirizzo` — stringa, opzionale
- `telefono` — stringa, obbligatorio
- `eta` — intero, opzionale (0-150)

**Implementazione:** entità JPA `Persona.java` persistita su database tramite Spring Data JPA.

#### Interfaccia grafica Swing

**Finestra principale (`MainWindow`):**
- Mostra una `JTable` con tutte le persone (colonne: ID, Nome, Cognome, Indirizzo, Telefono, Età)
- Supporta ordinamento per colonna (click sull'intestazione)
- Toolbar con bottoni: **Nuovo**, **Modifica**, **Elimina**, **Esporta**, **Aggiorna**
- Double-click su una riga apre l'editor

**Finestra editor persona (`EditorPersonaDialog`):**
- Dialog modale con campi `JLabel` + `JTextField` per ogni dato
- Modalità **CREATE** (campi vuoti) e **UPDATE** (campi precaricati)
- Bottoni: **Salva** e **Annulla**
- Validazione client-side: nome, cognome e telefono obbligatori; età intero 0-150

**Comportamento bottoni:**

| Bottone | Comportamento |
|---------|--------------|
| Nuovo | Apre `EditorPersonaDialog` con campi vuoti |
| Modifica | Se nessuna riga selezionata → warning. Se riga selezionata → apre editor precaricato |
| Elimina | Se nessuna riga selezionata → warning. Se riga selezionata → `JOptionPane.showConfirmDialog` "Eliminare NOME COGNOME?" → se SI elimina e aggiorna tabella |
| Salva (in editor) | Salva i dati e chiude il dialog. La tabella si aggiorna automaticamente |
| Annulla (in editor) | Chiude il dialog senza salvare |

#### Persistenza
Implementata tramite **Spring Data JPA** + **H2** (dev) / **MySQL** (prod).
I dati sono persistiti nel database e sopravvivono al riavvio dell'applicazione.
Le migrazioni dello schema sono gestite da **Liquibase**.

---

### Requisiti Extra (Evoluzioni EXTRA)

#### EXTRA #1 — Salvataggio per-persona in cartella `informazioni/`
Ogni persona può essere esportata in un file dedicato:
```
informazioni/NOME-COGNOME-{id}.txt
```
La cartella viene creata automaticamente se non esiste.
Accessibile dal bottone **Esporta** nella toolbar o tramite `POST /api/v1/persone/{id}/export`.

#### EXTRA #2 — Classe Utente + Login
- Entità `Utente` con `username` (univoco) e `password` (BCrypt)
- Finestra di login (`LoginWindow`) mostrata all'avvio
- Credenziali default: **admin / admin** (inserite da Liquibase)
- Login fallito → messaggio di errore, nessuna apertura della finestra principale
- Login riuscito → finestra di login si chiude, si apre `MainWindow`

#### EXTRA #3 — JToolBar
`MainWindow` ha una `JToolBar` con i bottoni principali:
- Icone caricate da `src/main/resources/icons/` (fallback a testo se non trovate)
- Tooltip descrittivi su ogni bottone

#### EXTRA #5 — Database
Persistenza su database tramite **Spring Data JPA**:
- **Dev:** H2 su file (`./data/rubricadb`) — dati persistenti tra i riavvii
- **Prod:** MySQL (dati persistenti — selezionare con `--spring.profiles.active=prod`)

Schema database gestito da **Liquibase** con changelog versionati.

---

## Tecnologie utilizzate

| Tecnologia | Versione | Scopo |
|-----------|---------|-------|
| Spring Boot | 3.2.3 | Framework applicativo |
| Spring Data JPA | (Boot managed) | Persistenza + repository |
| Spring Specification | (Boot managed) | Filtri dinamici |
| Spring Security | (Boot managed) | BCryptPasswordEncoder |
| Liquibase | (Boot managed) | Migrazioni database |
| MapStruct | 1.5.5.Final | Mapping Entity ↔ DTO |
| Lombok | 1.18.30 | Riduzione boilerplate |
| springdoc-openapi | 2.3.0 | Documentazione API |
| H2 | (Boot managed) | Database dev |
| MySQL | 8.x | Database prod |
| Java Swing | (JDK built-in) | Interfaccia desktop |
| JUnit 5 + Mockito | (Boot managed) | Unit test |

---

## Credenziali di default

| Campo | Valore |
|-------|--------|
| Username | `admin` |
| Password | `admin` |

La password è memorizzata come hash BCrypt nel database. Per aggiungere altri utenti usare `POST /api/v1/auth/register`.

---

## Aggiungere icone alla toolbar

Inserire i file PNG (24×24 px consigliato) in:

```
src/main/resources/icons/
  add.png       → bottone Nuovo
  edit.png      → bottone Modifica
  delete.png    → bottone Elimina
  export.png    → bottone Esporta
  refresh.png   → bottone Aggiorna
```

Se i file non sono presenti, i bottoni mostrano il testo come fallback.
Icone gratuite: [Google Material Icons](https://fonts.google.com/icons), [FlatIcon](https://www.flaticon.com).
