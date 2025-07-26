# 📦 Job Application Management System – Projektübersicht

## 🧩 Architektur (Microservices)

| Service               | Funktion                                                            |
|------------------------|---------------------------------------------------------------------|
| **API Gateway**        | Leitet Anfragen an Services weiter, Auth via Keycloak              |
| **Keycloak**           | Benutzerverwaltung, Login, Rollen (APPLICANT, HR, ADMIN)           |
| **Job-Service**        | Verwaltung von Stellenanzeigen                                     |
| **Application-Service**| Bewerbungen verwalten, Status setzen, CV verknüpfen                |
| **Storage-Service**    | Speichert Lebensläufe (PDF)                                        |
| **Notification-Service** | Versendet Mails bei Bewerbung & Statuswechsel (Event-basiert)    |

---

## 🔐 Authentifizierung (Keycloak)

- Rollen:
  - `APPLICANT`: Bewerbung einreichen & ansehen
  - `HR`: Jobs posten, Bewerbungen verwalten
  - `ADMIN`: Alle Rechte

- Authentifizierung über JWT (Bearer Token) via Keycloak

---

## 🧾 Datenmodell (Kern-Entities)

### Job (Job-Service)
```java
UUID id;
String title;
String description;
String location;
UUID createdBy;
boolean active;
LocalDateTime createdAt;
```

### Application (Application-Service)
```java
UUID id;
UUID userId;
UUID jobId;
String cvFileId;
ApplicationStatus status;
boolean viewedByHr;
LocalDateTime createdAt;
```

### ApplicationStatus (Enum)
```java
RECEIVED, VIEWED, UNDER_REVIEW, INVITED, REJECTED, WITHDRAWN
```

---

## 🌍 REST-API – Endpunkte

### 🔐 Auth (Keycloak – extern verwaltet)

| HTTP | Pfad             | Beschreibung                  |
|------|------------------|-------------------------------|
| POST | `/auth/login`    | Login über Keycloak UI        |
| POST | `/auth/register` | Registrierung                 |
| GET  | `/auth/userinfo` | Aktuellen Benutzer auslesen   |

---

### 📘 Job-Service

| HTTP   | Pfad                          | Beschreibung                     |
|--------|-------------------------------|----------------------------------|
| GET    | `/jobs`                       | Alle Jobs anzeigen               |
| GET    | `/jobs/{jobId}`               | Einzelnen Job anzeigen           |
| POST   | `/jobs`                       | Job erstellen (HR)               |
| PUT    | `/jobs/{jobId}`               | Job bearbeiten                   |
| PATCH  | `/jobs/{jobId}/deactivate`    | Job deaktivieren                 |
| DELETE | `/jobs/{jobId}`               | Job löschen (Admin)              |

---

### 📄 Application-Service

| HTTP | Pfad                                                   | Beschreibung                          |
|------|----------------------------------------------------------|---------------------------------------|
| POST | `/jobs/{jobId}/applications`                           | Bewerbung einreichen                  |
| GET  | `/jobs/{jobId}/applications/me`                        | Eigene Bewerbung für Job ansehen      |
| GET  | `/applications/me`                                     | Alle eigenen Bewerbungen              |
| GET  | `/applications/{appId}`                                | Bewerbung im Detail                   |
| GET  | `/jobs/{jobId}/applications`                           | Alle Bewerbungen für Job (HR)         |
| PATCH| `/applications/{appId}/status`                         | Bewerbungsstatus aktualisieren        |

---

### 📂 Storage-Service

| HTTP   | Pfad                             | Beschreibung                        |
|--------|----------------------------------|-------------------------------------|
| POST   | `/storage/files`                | PDF hochladen                       |
| GET    | `/storage/files/{fileId}`       | PDF herunterladen                   |
| GET    | `/storage/files/{fileId}/meta`  | Datei-Metadaten anzeigen            |
| DELETE | `/storage/files/{fileId}`       | PDF löschen                         |

---

### 📬 Notification-Service (intern)

| HTTP | Pfad                     | Beschreibung                        |
|------|--------------------------|-------------------------------------|
| POST | `/notifications/email`  | Mail versenden (eventbasiert)       |

---

## 🔄 Datenfluss: Lebenslauf (CV)

1. Bewerber lädt PDF bei `/storage/files` hoch → erhält `fileId`
2. Bewerbung bei `/jobs/{jobId}/applications` einreichen, `cvFileId` wird gespeichert
3. HR ruft Bewerbung auf → Frontend lädt `/storage/files/{fileId}` nach

---

## 📌 Projektstruktur (Beispiel: `application-service`)

```
application-service/
├── controller/
├── service/
├── repository/
├── dto/
├── model/
│   └── Application.java
├── enums/
│   └── ApplicationStatus.java
├── db/changelog/
│   └── application.changelog-create-table.yaml
└── ApplicationServiceApplication.java
```

---

## 🧠 Hinweise

- ✅ Zugriffsschutz mit `@PreAuthorize` (Rollen aus JWT)
- ✅ Kommunikation REST + Events (z. B. Kafka/SQS)
- ✅ Validierung mit DTOs & Enum für Status
- ✅ Keycloak verwaltet alle Benutzer – kein eigener User-Service nötig