# Relazione finale – Progetto “GLOBETROTTERS”
> Laboratorio di Programmazione di Sistemi Mobili - UNIBO | A.A. 2024-2025
---
### Componenti del gruppo:
* Stefano Tassinari
* Alessandro Cacchi
* Luca Zenobi
---
---
## 1. Scopo del progetto
Il progetto **“GLOBETROTTERS”** nasce con l’intento di offrire agli appassionati di viaggi uno strumento semplice, intuitivo e funzionale per documentare e conservare i propri ricordi. Si tratta di un'applicazione mobile nativa per Android, sviluppata in Kotlin, che consente di creare diari di viaggio digitali. L’utente può salvare per ogni tappa visitata informazioni come foto, note personali e posizione geografica, organizzando i contenuti in modo chiaro e immediato.

L'app è pensata per funzionare completamente offline e per essere utilizzata anche in contesti di scarsa connettività, con la possibilità di arricchire l’esperienza grazie a chiamate API verso servizi online. Durante lo sviluppo, sono state adottate le best practices dell’ecosistema Android, in particolare l’architettura **MVVM** (Model-View-ViewModel), l’utilizzo dei coroutines per la gestione asincrona e il supporto alla **persistenza locale** con Room.


## 2. Descrizione della struttura dei sorgenti
L’applicazione è stata strutturata seguendo il principio della separation of concerns, suddividendo il progetto in package distinti:

   ### -> activity

   Contiene le schermate principali dell’applicazione ad esempio:
* *MainActivity*: homepage che mostra l’elenco dei viaggi registrati sotto forma di card. Ogni card include il nome della città e le date del viaggio, con azioni per visualizzare i dettagli, eliminarlo, accedere alle impostazioni o alla mappa.
* *AddTravelActivity*: schermata per la creazione di un nuovo viaggio, in cui è possibile inserire il nome della città, selezionare/scattare una foto di copertina, impostare le date e rilevare o scegliere manualmente la posizione su mappa.
* *TravelDetailsActivity*: mostra il dettaglio di un viaggio. Da qui l’utente può aggiungere immagini, scrivere note, ed esplorare le informazioni sulla città (tramite Wikipedia API) o verificarne il meteo attuale (tramite OpenWeather API).
* *MapViewActivity*: visualizza tutti i viaggi su una mappa interattiva di Google Maps, con marker per ogni località visitata.

* *SettingsActivity*: consente la gestione dei permessi, la visualizzazione della versione dell’app, la condivisione dell’applicazione e il contatto con il supporto.

### -> *fragment*
Contiene fragment riutilizzabili, ad esempio per i moduli di inserimento foto o note.
### -> *viewmodel*
Raccoglie i ViewModel delle principali activity, che gestiscono la logica applicativa e lo stato dell’interfaccia in modo reattivo.
### -> *database*
Gestisce la persistenza locale dei dati utilizzando Room. Include le entità annotate con @Entity, i DAO per l'accesso ai dati e l’istanza singleton del database.
### -> *api*
Contiene le interfacce Retrofit e i data model per le chiamate a servizi esterni:
  * Wikipedia (info sulla città)
  * OpenWeather (meteo attuale)

### -> *adapter*
Comprende gli adapter utilizzati per popolare dinamicamente le RecyclerView (elenco viaggi, immagini, note, ecc.).

## 3. Funzionalità implementate
### Requisiti minimi soddisfatti:

* Architettura a pacchetti chiara e modulare.

* ViewModel per la gestione della business logic e del ciclo di vita.
* Utilizzo delle coroutines per operazioni asincrone.
* Salvataggio locale dei dati tramite Room, con supporto offline completo.
* Gestione dei permessi runtime per accedere a fotocamera, posizione e galleria.


### Requisiti opzionali implementati:
* **Wikipedia API**: per fornire informazioni culturali e turistiche sulla città inserita.

* **OpenWeather API**: per mostrare le condizioni meteo in tempo reale.
* **Mappa interattiva**: i viaggi sono visualizzabili su una mappa di Google con funzionalità di zoom, navigazione e selezione.
* **Gestione avanzata dei permessi** per fotocamera, posizione e galleria, con flusso dedicato di abilitazione.

## 4. Punti di forza
* **Architettura MVVM ben implementata**, con chiara separazione tra logica di interfaccia e logica applicativa.

* **Modularità e manutenibilità del codice**, che facilita l’estensione del progetto.
* **Esperienza utente fluida**, curata con Material Design, transizioni intuitive e interfaccia coerente.
* **Integrazione efficace delle API esterne** con Retrofit, parsing automatico delle risposte JSON e gestione degli stati (caricamento, errore, successo).
* **Visualizzazione geografica dei viaggi** tramite marker su Google Maps, con buona usabilità e integrazione delle classiche funzionalità di navigazione offerte da Google Maps
* **Persistenza dei dati offline**, fondamentale per un’app da utilizzare anche in viaggio.

## 5. Possibili migliorie future
Il progetto, pur essendo completo e funzionale nella sua forma attuale, può essere ulteriormente arricchito e perfezionato con l’aggiunta di nuove funzionalità:
* **Autenticazione utente e sincronizzazione cloud**: implementare un sistema di login permetterebbe agli utenti di salvare e sincronizzare i propri dati su un backend remoto (ad esempio con Firebase), garantendo così l’accesso da più dispositivi e un backup continuo dei dati.

* **Backup automatico su Google Drive o simili**: utile per mettere al sicuro i ricordi di viaggio anche in assenza di un sistema di login.
* **Funzionalità di ricerca e filtri**: una barra di ricerca per filtrare i viaggi per città, date o parole chiave renderebbe la navigazione più efficiente, specialmente per utenti con molti viaggi registrati.
* **Supporto multilingua**: tradurre l’app in altre lingue (inglese, spagnolo, francese) aprirebbe le porte a un pubblico internazionale, rendendo il progetto più inclusivo.
* **Modalità scura e temi personalizzati**: si dovrebbe implementare una modalità dark per offrire un'esperienza visiva più confortevole in ambienti con poca luce. Inoltre, sarebbe opportuno permettere la personalizzazione del tema grafico dell’app, con la possibilità di scegliere combinazioni di colori preferite e il supporto automatico al passaggio tra tema chiaro e scuro in base alle impostazioni di sistema.
* **Integrazione di funzionalità AI/ML**: sarebbe interessante esplorare l’uso dell’intelligenza artificiale per offrire suggerimenti di viaggio basati sui viaggi precedenti o per analizzare automaticamente le immagini e classificarle (es. “mare”, “montagna”, “monumenti”).


## 6. Considerazioni finali
Il progetto “GLOBETROTTERS” ha rappresentato per noi un’esperienza formativa significativa, che ci ha permesso di applicare in modo pratico e concreto le competenze acquisite durante il corso. L’adozione di tecnologie moderne come Kotlin, Jetpack, Room e Retrofit ci hanno messo alla prova, spingendoci a studiare e comprendere in profondità il funzionamento del framework Android e dei pattern architetturali più diffusi.

Oltre agli aspetti tecnici, abbiamo avuto modo di sperimentare il lavoro in team, la suddivisione dei compiti, l’uso degli strumenti di versionamento (Git/GitHub) e l’importanza della comunicazione efficace all’interno di un progetto condiviso.
