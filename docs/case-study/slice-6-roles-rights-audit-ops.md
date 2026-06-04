# Slice 6: Rollen, Rechte, Audit und Betrieb

Stand: 4. Juni 2026

## Ziel

Eine produktionsnahe WEG-App muss nicht nur Funktionen anbieten, sondern
Vertrauen schaffen: Wer darf was tun? Welche Aktion wurde wann von wem
ausgeloest? Ist das System betreibbar und review-faehig? Slice 6 macht Rollen,
Rechte und Audit deshalb sichtbar und erweitert den technischen Nachweis pro
WEG.

## Figma

- Case-Study-Board: https://www.figma.com/board/8L6TmSLizT6j06UaNHHrB8
- Artefakt: `RealEstate OS Slice 6 Roles Rights Audit Operations`

Das Artefakt zeigt Rollenmodell, serverseitige Rechtepruefung, Audit-Trail,
Produkt-Sichtbarkeit und Betriebsreadiness.

## Umsetzung

Backend:
- Passwort-Reset ist als eigener Auth-Flow umgesetzt und in der
  Security-Allowlist explizit oeffentlich freigegeben.
- Flyway V10 erweitert `audit_log` um `property_id` und einen
  WEG-Zeitindex.
- Audit-Logs speichern Akteur, Rolle, WEG, Aktion, Zieltyp, Ziel-ID,
  Zusammenfassung und Zeitpunkt.
- Workspace-Commands schreiben Audit nun mit WEG-Kontext.
- Aufgaben koennen aktualisiert und geloescht werden; Statuswechsel,
  Bearbeitung und Loeschung landen im Audit.
- Community-Mitglieder koennen in Rolle und Status verwaltet oder deaktiviert
  werden. Die primaere Administratorrolle bleibt gegen Deaktivierung
  geschuetzt.
- Das Dashboard liefert `access` mit aktueller Rolle, Admin-/Bearbeitungsrecht
  und erlaubten Command-Gruppen.
- Das Dashboard liefert die letzten technischen Audit-Eintraege der
  ausgewählten WEG.
- Tests pruefen Rollen-/Rechte-Readmodel und Audit-Eintraege im Workspace-Flow.

Frontend:
- Login-first fuer bekannte Nutzer, Passwort-vergessen-Flow und kompaktere
  Auth-Hero-Ansicht.
- Einstellungen zeigen Rolle, Bearbeitungsniveau und erlaubte Command-Gruppen.
- Einstellungen verwalten Nutzerrollen, Zugriffsstatus und Schnellzugriff pro
  Nutzer.
- Aufgaben koennen direkt aus der Liste bearbeitet, geloescht und per Status
  verschoben werden.
- Die Aktivitaetsansicht zeigt zusaetzlich einen technischen Audit-Nachweis.
- Lokale QA prueft, dass Audit und Rechte im Produkt sichtbar sind.
- `/set-password` ist als Route registriert, damit direkte Aktivierungslinks
  keine Router-Fehler erzeugen.

## Akzeptanz

- Jede schreibende Workspace-Aktion landet im Audit mit WEG-Kontext.
- Nutzer sehen ihre aktuelle Rolle und ihre erlaubten Arbeitsbereiche.
- Admins koennen Rollen und Zugriffe intuitiv verwalten.
- Aufgaben sind ohne Umwege bearbeitbar, verschiebbar und loeschbar.
- Wiederkehrende Nutzer sehen zuerst den Login und koennen ihr Passwort
  eigenstaendig zuruecksetzen.
- Audit-Eintraege sind im Produkt sichtbar und filterbar ueber die globale
  Suche.
- Direkte Passwort-Setup-Links erzeugen keine Browser-Console-Fehler.
- CI, lokale Browser-QA und Stage-Smoke sind Teil des Release-Ablaufs.

## Tests

- `npm run backend:test`
- `npm run frontend:build`
- `npm run qa:local`
- `npm run ci`

Screenshot-Artefakte:
- `output/qa/realestate-audit-desktop.png`
- `output/qa/realestate-dashboard-mobile.png`

## Naechster Schritt

Die Case Study kann nun ins Bewerbungsdeck uebernommen werden: Marktproblem,
Produktthese, Figma-Artefakte, Architektur, Live-App, Code und getesteter
Release-Prozess.
