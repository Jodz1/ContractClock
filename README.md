# Recka

Recka je JavaFX desktop aplikacija za praćenje vremena, briefova i zarade po contractima. Projekat je napravljen kao Maven aplikacija sa MySQL bazom i čistim JDBC DAO slojem, prema specifikaciji iz fajla `Recka_Time_Tracker_Specification(1).pdf`.

## Tehnologije

- Java 21
- JavaFX 21
- Maven
- MySQL
- JDBC, bez Hibernate/JPA
- DAO + Service layer
- CSS za UI
- OpenPDF za PDF export briefova

## Pokretanje

1. Ako već imaš postojeću bazu sa bitnim podacima, **ne pokreći** `schema.sql`, `seed.sql` ni `setup-database-windows.bat`. Samo proveri da `db.url`, `db.username` i `db.password` pokazuju na tvoju postojeću MySQL bazu.

   SQL fajlove koristi samo za potpuno praznu novu bazu:

   ```bash
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/seed.sql
   ```

2. Podesi konekciju u:

   ```text
   src/main/resources/application.properties
   ```

   Primer:

   ```properties
   db.url=jdbc:mysql://localhost:3306/time_tracker_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.username=root
   db.password=password
   ```

   Možeš napraviti i lokalni override fajl:

   ```text
   ~/.recka/application.properties
   ```

3. Pokreni aplikaciju preko Maven JavaFX plugina:

   ```bash
   mvn clean javafx:run
   ```

   Na Windows-u možeš samo pokrenuti:

   ```bat
   run-windows.bat
   ```

   Alternativno, ako IDE i dalje prijavljuje JavaFX runtime error, pokreni:

   ```bash
   mvn clean compile exec:java
   ```

   ili na Windows-u:

   ```bat
   run-windows-exec.bat
   ```

### Bitno za JavaFX runtime error

Nemoj pokretati aplikaciju direktno preko:

```bash
java -jar target/recka-1.0.0.jar
```

Taj `.jar` ne sadrži JavaFX runtime module, pa Java često izbaci poruku:

```text
Error: JavaFX runtime components are missing, and are required to run this application
```

Ispravno pokretanje je preko Maven-a: `mvn clean javafx:run`, `mvn clean compile exec:java`, ili preko priloženog `run-windows.bat` fajla.

## Šta je implementirano

- Dashboard sa contractima i sumama: ukupno sati, earned, paid, unpaid.
- Contract totals prozor na dashboardu: izbor contracta, filter od-do datuma i read-only ukupan earned iznos.
- CRUD za klijente/contracte kroz New/Edit formu i archive opciju.
- Compact time tracker prozor sa real-time timerom.
- Pravilo: samo jedan running timer u jednom trenutku.
- Recovery dialog za nezavršenu running sesiju pri startu aplikacije.
- Stop timer snima `duration_seconds`, `calculated_amount`, `final_amount`.
- `hourly_rate_snapshot` se čuva u svakoj work session sesiji.
- Change hourly rate zatvara staru rate stavku i kreira novu u `contract_rates`.
- Add manual time sa validacijom vremena, overlap upozorenjem, chargeable i override amount logikom.
- Work session details/edit prozor.
- Single brief TXT export.
- Combined brief TXT/PDF export.
- Payments: add payment i mark balance as paid.
- Search/filter contracta po nazivu ili klijentu.
- Filter sesija po datumu na contract details ekranu.
- Window state memento preko `window_state` tabele.
- JavaFX CSS za moderan desktop UI.

## Arhitektura i patterni

- **MVC/MVVM stil:** `view` klase grade UI, `service` klase nose biznis logiku, `dao` sloj radi sa bazom, `model` klase čuvaju podatke.
- **DAO pattern:** `ClientDao`, `ContractDao`, `RateDao`, `WorkSessionDao`, `PaymentDao`, `SettingsDao`, `ActivityTagDao`.
- **Singleton pattern:** `DatabaseConnectionManager`, `AppEventBus`, `SettingsManager`.
- **Factory pattern:** `DaoFactory`, `FlyweightFactory`, `WindowFactory`.
- **Observer pattern:** `AppEventBus` emituje `SessionStartedEvent`, `SessionStoppedEvent`, `ContractUpdatedEvent`, `PaymentAddedEvent`, `RateChangedEvent`, `BriefExportedEvent`.
- **State pattern:** `TimerContext` koristi `IdleState`, `RunningState`, `PausedState`, `StoppedState`.
- **Command pattern:** akcije su modelovane preko `Command` interfejsa i komandi za timer, manual time, payment, rate i export.
- **Memento pattern:** `WindowStateManager` čuva i vraća stanje prozora preko `window_state` tabele.
- **Strategy pattern:** `BriefExportStrategy`, `TxtBriefExportStrategy`, `PdfBriefExportStrategy`.
- **Flyweight pattern:** `FlyweightFactory` kešira `CurrencyFlyweight`, `ActivityTagFlyweight` i `StatusFlyweight` objekte.

## Napomena

Aplikacija koristi MySQL kao izvor istine. Ako MySQL nije dostupan, prikazuje se jasan error screen sa koracima šta treba podesiti.


## MySQL / Workbench fix

MySQL Workbench nije baza, nego samo alat za gledanje baze. Recka ne trazi da Workbench bude otvoren; treba da radi **MySQL Server** servis.

Automatsko kreiranje baze je namerno isključeno po defaultu da se postojeća baza ne dira:

```properties
db.autoInitialize=false
```

Sa ovom postavkom aplikacija samo otvara konekciju i čita/piše kroz postojeće funkcionalnosti aplikacije. Novi Contract totals prozor radi samo read-only `SELECT` kalkulaciju.

Ako ti je root password prazan, u `src/main/resources/application.properties` stavi:

```properties
db.password=
```

Ako zelis rucno da napravis bazu, pokreni:

```bat
setup-database-windows.bat
```

Preporuka: koristi Java 21 u IntelliJ-u. Java 25 moze da prikaze warning poruke za JavaFX native access; to nisu fatalne greske.
