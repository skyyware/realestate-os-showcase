# Product Brief: RealEstate OS fuer WEG-Selbstverwaltung

Stand: 4. Juni 2026

## Produktauftrag

RealEstate OS soll aus einer Demo-Anwendung zu einem wirklich nutzbaren
Arbeitsraum fuer deutsche WEG-Selbstverwaltung werden. Der Massstab ist nicht,
ob alle Screens vorhanden sind, sondern ob eine kleine oder mittlere WEG morgen
ihre echte Verwaltung beginnen kann: ohne Demodaten, mit klaren Rollen,
nachvollziehbaren Finanzen, belastbaren Beschluessen, Dokumenten und
Kommunikation.

## Nicht Verhandelbare Produktqualitaeten

- **Verstaendlich:** Fachliche Komplexitaet wird erklaert, nicht versteckt.
- **Nachvollziehbar:** Jede relevante Aktion, Zahl und Entscheidung hat Quelle
  und Verlauf.
- **Gefuehrt:** Nutzer sehen immer den naechsten sinnvollen Schritt.
- **Verbrauchernah:** Bedienung ist ruhig, mobil, klar und fehlertolerant.
- **Intern uebernehmbar:** Module, Tests, Docs und Architektur bleiben so
  pragmatisch, dass ein Produktteam sie weiterfuehren kann.

## Zielbild Nach Delta-Schliessung

### 1. WEG und Rollen

Funktion:
- WEG anlegen mit Adresse, Abrechnungsjahr, Bank-/Ruecklagenstruktur und
  Einheiten.
- Eigentuemer einladen, Einheiten zuordnen, Miteigentumsanteile pflegen.
- Rollen: Selbstverwalter, Beirat, Eigentuemer, externer Experte.

Akzeptanz:
- Ein neuer Account startet leer und wird durch eine echte WEG-Einrichtung
  gefuehrt.
- Schreibrechte sind rollenbasiert.
- Jede Einladung und Rollenveraenderung wird auditiert.

### 2. Finanzraum

Funktion:
- Bankumsatz, manuelle Buchung, Rechnung, Beleg, Kostenart,
  Verteilerschluessel und Einheit werden verbunden.
- Hausgeld-Soll, Zahlungseingang, Rueckstand und Ruecklage sind getrennt
  sichtbar.
- Eigentuemer sehen Gemeinschaftssicht und eigenen Anteil.

Akzeptanz:
- Keine Finanzkarte zeigt eine Zahl ohne Drilldown.
- Eine Buchung kann mit Beleg und Kostenart gespeichert werden.
- Offene Forderungen koennen je Einheit nachvollzogen werden.

### 3. Wirtschaftsplan und Jahresabschluss

Funktion:
- Jahresbezogener Wirtschaftsplan mit Ist-Basis, Kategorien, Ruecklage,
  Szenarien und Hausgeldwirkung.
- Jahresabrechnung mit Plausibilitaetspruefung, Nachzahlung/Guthaben und
  Vermoegensbericht.
- Beschlussvorlagen fuer Wirtschaftsplan und Abrechnung.

Akzeptanz:
- Nutzer koennen einen Plan aus Ist-Werten starten und anpassen.
- Das System zeigt Abweichungen und fehlende Belege.
- Export erzeugt ein pruefbares PDF-Artefakt pro Gemeinschaft und Einheit.

### 4. Versammlung und Beschluss

Funktion:
- Eigentuemerversammlung mit Modus Praesenz, hybrid oder virtuell.
- Tagesordnung, Einladung, Teilnehmer, Stimmrechte, Vollmachten,
  Abstimmungsergebnis und Protokoll.
- Beschlusssammlung mit fortlaufender Nummer, Wortlaut, Status und Anfechtung/
  Aufhebungshinweis.

Akzeptanz:
- Ein TOP kann in einen Beschluss und danach in Aufgaben uebergehen.
- Beschlussdetails sind exportierbar und mobil lesbar.
- Virtuelle Versammlung erfasst den erforderlichen Grundlagenbeschluss.

### 5. Dokumentenakte

Funktion:
- Upload echter Dokumente mit Typ, Objektbezug, Jahr, Sichtbarkeit und Version.
- Volltext/OCR als Ausbaupfad, aber Metadaten und Verknuepfungen sofort.
- Dokumente koennen an Buchungen, Beschluesse, Aufgaben und Vertraege gehaengt
  werden.

Akzeptanz:
- Jedes Dokument hat Eigentum, Sichtbarkeit und Audit.
- Suche findet nach Typ, Jahr, Objekt, Einheit, Person und Text.
- Exportpaket fuer Versammlung oder Beratung ist moeglich.

### 6. Kommunikation als Vorgang

Funktion:
- Eigentuemeranfrage, Schadensmeldung, Ankuendigung und interne Notiz werden
  als Vorgaenge gefuehrt.
- Status, SLA, Verantwortliche, Lesebestaetigung und Antwortverlauf.
- E-Mail-Benachrichtigung fuehrt in den passenden Vorgang.

Akzeptanz:
- Keine Anfrage verschwindet als freie Chatnachricht.
- Nutzer sehen Antwortstatus und Verantwortliche.
- Dringende Themen koennen eskaliert werden.

### 7. Instandhaltung und Vertraege

Funktion:
- Vertraege, Fristen, Wartungen, Angebote, Massnahmen, Auftraege und Abnahmen.
- Sanierungsvorhaben mit Finanzierungsbedarf, Beschlussbedarf und Fortschritt.

Akzeptanz:
- Eine Massnahme zeigt: Grund, Angebote, Beschluss, Kosten, Finanzierung,
  Verantwortliche und naechste Aktion.
- Vertragsfristen erzeugen rechtzeitig Aufgaben.

## Designauftrag

Das Produkt muss Verbraucher ansprechen, ohne seine Fachlichkeit zu verlieren:

- erste Ansicht als aktiver Arbeitsraum, nicht Landing Page
- grosse, klare Prioritaeten nur fuer echte Kernentscheidungen
- dichte, ruhige Detailansichten fuer wiederholte Arbeit
- mobile Nutzung fuer Lesen, Abstimmen, Antworten, Dokumente und Zahlfreigaben
- keine dekorative Ueberladung
- Zahlen, Status und naechste Schritte visuell vor Textwuesen

## Engineering Slice Reihenfolge

1. WEG-Onboarding mit Einheiten, Rollen und leerem Produktzustand vertiefen.
2. Finanzmodell auf Belegkette, Kostenarten, Verteilerschluessel und Hausgeld
   erweitern.
3. Beschluss- und Versammlungsmodul zum vollstaendigen Workflow ausbauen.
4. Dokumentenakte auf echte Dateiablage und Verknuepfung erweitern.
5. Kommunikation in Vorgangslogik umbauen.
6. Jahresabschluss, Wirtschaftsplan und Vermoegensbericht als gefuehrten
   Jahreszyklus liefern.
7. Instandhaltung, Angebote, Vertraege und Sanierungsvorhaben ergaenzen.

## Definition Of Ready

- Problem ist in `docs/research/weg-market-2026.md` oder Interviewnotizen
  belegt.
- User Journey und UI-Zustand sind in Figma oder FigJam dokumentiert.
- Datenmodell, Rechte und Audit sind beschrieben.
- Testfaelle fuer Backend, Frontend und QA-Smoke sind benannt.
- Datenschutz- und Exportwirkung sind geklaert.

## Definition Of Done

- `npm run ci` ist gruen.
- Betroffener Kernflow ist im lokalen QA-Smoke oder Playwright-Test abgedeckt.
- Desktop, Tablet und Mobile wurden visuell geprueft.
- Figma-Artefakt wurde mit implementiertem Screenshot abgeglichen.
- Dokumentation und Bewerbungsdeck-Notizen sind aktualisiert.

