# Visite CLI — README

> **Nota sul package**: in questo esempio il package base è `it.unibs.visite`.

## Obiettivo del progetto (V1)
Applicazione **stand‑alone** a riga di comando per configurare l’organizzazione delle visite: **login** del configuratore, **wizard di inizializzazione una tantum** (ambito territoriale, max persone per iscrizione, luoghi, tipi di visita e volontari), **persistenza su file** senza DBMS.

## Struttura del progetto
```
visite-cli/
├─ pom.xml
└─ src/main/java/
   └─ it/unibs/visite/
      ├─ Main.java
      ├─ cli/
      │  └─ InitWizardCLI.java
      ├─ core/
      │  ├─ DomainException.java
      │  └─ Preconditions.java
      ├─ model/
      │  ├─ DataStore.java
      │  ├─ Luogo.java
      │  ├─ ParametriSistema.java
      │  ├─ StatoVisita.java
      │  ├─ TipoVisita.java
      │  └─ Volontario.java
      ├─ persistence/
      │  └─ FilePersistence.java
      ├─ security/
      │  ├─ AuthService.java
      │  ├─ CredentialsStore.java
      │  └─ PasswordHasher.java
      └─ service/
         ├─ ConfigService.java
         └─ InitWizardService.java
```

---

## Flusso V1 
1. **Login**: utente predefinito `admin/admin` → forzato cambio password al primo accesso.
2. **Wizard**: imposta ambito territoriale, max persone per iscrizione; crea luoghi, volontari, tipi di visita; **ogni tipo di visita deve avere ≥1 volontario**.
3. **Persistenza**: dati e credenziali sono salvati in `~/.visite-cli/` (serializzazione Java).

---

## Pacchetti e classi

### `it.unibs.visite`
- **`Main`**
  - **Cosa fa**: avvia la CLI; gestisce login, cambio password, avvio del wizard se non inizializzato.
  - **A cosa serve**: punto di ingresso dell’app; orchestra `AuthService`, `ConfigService` e `InitWizardCLI`.

### `it.unibs.visite.cli`
- **`InitWizardCLI`**
  - **Cosa fa**: interfaccia testuale del **wizard di inizializzazione una tantum** (ambito, max, luoghi, volontari, tipi di visita e associazioni).
  - **A cosa serve**: guida l’utente nella raccolta dati e chiama i servizi di dominio applicando le regole.

### `it.unibs.visite.core`
- **`DomainException`**
  - **Cosa fa**: eccezione unchecked per violazioni di regole/invarianti di dominio.
  - **A cosa serve**: segnalare errori di business in modo esplicito e distinto dagli errori tecnici.
- **`Preconditions`**
  - **Cosa fa**: utility di validazione (`notBlank`, `notNull`, `check`).
  - **A cosa serve**: centralizzare pre/post‑condizioni e rendere il codice più leggibile e difensivo.

### `it.unibs.visite.model`
- **`ParametriSistema`**
  - **Cosa fa**: mantiene **ambito territoriale** (settabile una sola volta), **max persone per iscrizione** (modificabile) e flag **initialized**.
  - **A cosa serve**: configurazione globale del sistema impostata dal wizard.
- **`Luogo`**
  - **Cosa fa**: rappresenta un luogo con `id`, `nome`, `descrizione`.
  - **A cosa serve**: contenitore logico per i **tipi di visita**.
- **`TipoVisita`**
  - **Cosa fa**: rappresenta un tipo di visita legato a un `Luogo` con `titolo`, `descrizione` e l’elenco dei **volontari** (per nickname).
  - **A cosa serve**: definire l’offerta di visite; **invariante**: ogni tipo di visita ha **≥1 volontario** (`ensureInvariants()`).
- **`Volontario`**
  - **Cosa fa**: entity di un volontario identificato da `nickname` univoco.
  - **A cosa serve**: definire chi può condurre un tipo di visita.
- **`StatoVisita` (enum)**
  - **Cosa fa**: enum degli stati visita: `PROPOSTA`, `COMPLETA`, `CONFERMATA`, `CANCELLATA`, `EFFETTUATA`.
  - **A cosa serve**: base per fasi future (liste/archivio); in V1 è preparatorio.
- **`DataStore`**
  - **Cosa fa**: **aggregato** principale che contiene `ParametriSistema`, mappe di `Luogo`, `TipoVisita`, `Volontario`; include metodi di aggiunta/lettura.
  - **A cosa serve**: **modello persistito** su file; garantisce l’unicità dei nickname e fornisce accesso centralizzato ai dati.

### `it.unibs.visite.persistence`
- **`FilePersistence`**
  - **Cosa fa**: persiste/ricarica `DataStore` e le credenziali con **serializzazione Java**; salvataggio **atomico** e creazione directory dati.
  - **A cosa serve**: sostituisce un DBMS per la V1; unico punto di I/O con il filesystem.

### `it.unibs.visite.security`
- **`PasswordHasher`**
  - **Cosa fa**: calcola hash **PBKDF2WithHmacSHA256** con salt casuale; parametri: 120k iterazioni, 256 bit.
  - **A cosa serve**: memorizzare password **in forma derivata** in modo robusto senza librerie esterne.
- **`CredentialsStore`**
  - **Cosa fa**: archivio in‑memory/serializzato di utenti → `(salt, hash, mustChangePassword)`; include CRUD basilare e verifica.
  - **A cosa serve**: rappresentare le credenziali e lo stato del primo accesso.
  - **Sottoclasse/Inner class**: `Entry` — DTO serializzabile per una credenziale (`username`, `saltB64`, `hashB64`, `mustChangePassword`).
- **`AuthService`**
  - **Cosa fa**: coordina autenticazione e persistenza credenziali; bootstrap di `admin/admin` con obbligo di cambio password.
  - **A cosa serve**: API di alto livello per `login`, `mustChangePassword`, `changePassword`, `createConfigurator`.

### `it.unibs.visite.service`
- **`ConfigService`**
  - **Cosa fa**: carica/salva il `DataStore`; espone operazioni su `ParametriSistema` (ambito una tantum, max persone) e `initialized`.
  - **A cosa serve**: **fa da “application service”** per lo strato di configurazione e centralizza il salvataggio.
- **`InitWizardService`**
  - **Cosa fa**: operazioni di dominio per il wizard: aggiunge `Luogo`, `Volontario`, `TipoVisita`, associa volontari e valida invarianti.
  - **A cosa serve**: incapsulare la logica di business dell’inizializzazione, separandola dalla CLI.

---

## Build & Run
```bash
mvn clean package
java -jar target/visite-cli-1.0.0.jar
```
- Credenziali iniziali: **admin / admin** → cambio password obbligatorio.
- Dati salvati in `~/.visite-cli/`.

## Estensioni possibili (post‑V1)
- Preclusioni del calendario (mese *i+3*), viste per stato, modifica max a regime, esportazione JSON/CSV, logging/audit, test automatici.
