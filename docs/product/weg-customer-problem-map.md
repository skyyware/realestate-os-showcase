# WEG Problem- und Loesungslandkarte

Stand: 4. Juni 2026

Diese Landkarte uebersetzt die Marktrecherche in konkrete Jobs, Schmerzen,
Produktantworten und Prioritaeten. Sie ist die Referenz fuer Backlog,
Designentscheidungen und Akzeptanztests.

## Zielkunden

| Rolle | Job To Be Done | Erfolgskriterium |
| --- | --- | --- |
| Interner Selbstverwalter | Die WEG rechtssicher und zeitsparend fuehren. | Alle Pflichtprozesse laufen mit Checklisten, Vorlagen, Fristen und Audit. |
| Verwaltungsbeirat | Verwaltung kontrollieren und Eigentuemer informieren. | Belege, Konten, Beschluesse, Aufgaben und Risiken sind schnell pruefbar. |
| Einzelner Eigentuemer | Verstehen, was passiert und was es kostet. | Persoenlicher Finanzanteil, Dokumente, Stimmen und offene Entscheidungen sind klar. |
| Vermietender Eigentuemer | Abrechnung und Nachweise fuer Mieter und Steuer nutzen. | Einzelabrechnung, Belege und relevante Auswertungen sind exportierbar. |
| Neuer Eigentuemer | Schnell in die bestehende WEG hineinfinden. | Einheit, Beschluesse, Hausgeld, Ruecklage, Dokumente und aktuelle Themen sind sichtbar. |
| Externer Experte | Fachlich helfen, ohne die WEG-Daten neu zu sortieren. | Geteilte Vorgangsakten, Dokumente und Status reduzieren Rueckfragen. |

## Problemcluster

| Prioritaet | Problem | Kundenschmerz | Produktantwort |
| --- | --- | --- | --- |
| P0 | WEG-Stammdaten und Rollen sind unklar | Niemand weiss genau, wer was darf und wer wofuer zustaendig ist. | Mandantenfaehige WEG-Struktur mit Einheiten, Miteigentumsanteilen, Rollen, Einladungen und Berechtigungen. |
| P0 | Finanzen sind nicht erklaerbar | Hausgeld, Ruecklage, Nachzahlung und Belege wirken wie eine Blackbox. | Doppelt nachvollziehbarer Finanzraum: Bankumsatz -> Buchung -> Kostenart -> Beleg -> Verteilerschluessel -> Einheit. |
| P0 | Wirtschaftsplan und Jahresabrechnung sind Jahresstress | Excel, Fristen, Fehlerangst und Diskussionen in der Versammlung. | Gefuehrter Jahreszyklus mit Ist-Basis, Planvorschlaegen, Plausibilitaet, Hausgeldwirkung und Beschlussvorlage. |
| P0 | Beschluesse werden nicht sauber gefuehrt | Anfechtungsrisiko, fehlende Historie, unklare Umsetzung. | Versammlungs- und Beschlussmodul mit Einladung, TOPs, Abstimmung, Protokoll, Beschlusssammlung und Aufgabenableitung. |
| P0 | Dokumente sind verteilt | Belege, Protokolle, Versicherungen und Vertraege muessen gesucht werden. | Dokumentenakte mit Upload, Typisierung, Version, Volltextsuche, Objektbezug und Verknuepfung zu Finanzen/Beschluessen/Aufgaben. |
| P0 | Kommunikation bleibt folgenlos | Mails und Chats erzeugen keine Verantwortung. | Vorgangsbasierte Kommunikation mit Status, SLA, Verantwortlichen, internen Notizen und Eigentuemeransicht. |
| P1 | Instandhaltung ist reaktiv | Schaeden, Angebote und Sanierungskosten laufen auseinander. | Instandhaltungsplanung mit Objektzustand, Massnahmen, Angeboten, Beschlussbedarf, Finanzierung und Fortschritt. |
| P1 | Eigentuemer sind unterschiedlich digital | Manche lesen alles mobil, andere brauchen einfache E-Mail und PDF. | Barrierearme Oberflaeche, klare Sprache, E-Mail-Bruecke, PDF-Exports und mobile First-Tasks. |
| P1 | Externe Hilfe ist schwer einzubinden | Rechts-, Bau- oder Steuerfragen brauchen Kontext. | Expertenzugang auf Vorgangsebene, Exportpakete und Beratungsnotizen. |
| P2 | KI ohne Vertrauen hilft nicht | Nutzer wollen keine Blackbox bei Recht und Geld. | Assistenz nur mit Quellen, Erklaerung, Unsicherheit und manueller Freigabe. |

## Produktthesen

1. Eine WEG-App gewinnt Vertrauen, wenn sie Geld und Entscheidungen besser
   erklaert als eine klassische Verwaltung.
2. Selbstverwaltung wird kaufbar, wenn das Produkt Verantwortung sichtbar macht
   und Arbeit in kleine, sichere Schritte zerlegt.
3. Der groesste Nutzen entsteht nicht durch isolierte Module, sondern durch
   Verknuepfung: Beschluss erzeugt Aufgabe, Aufgabe erzeugt Auftrag, Auftrag
   erzeugt Rechnung, Rechnung erklaert Hausgeld.
4. Consumer Design ist im WEG-Kontext kein Marketinglack. Es ist notwendig,
   weil viele Nutzer keine Verwaltungsprofis sind.

## Akzeptanzregeln fuer Neue Features

- Jede schreibende Aktion hat Rolle, Audit-Eintrag und sichtbare Aktivitaet.
- Jede Finanzzahl ist bis zur Quelle nachvollziehbar.
- Jede gesetzlich relevante Entscheidung erzeugt ein exportierbares Artefakt.
- Jeder Workflow hat leeren Zustand, Fehlerzustand, Erfolg, Mobilansicht und
  mindestens einen automatisierten Test.
- Kein Produkttext darf juristische Sicherheit suggerieren, wenn nur eine
  technische Hilfestellung vorliegt.
- Jede neue UI muss vor Implementierung als Figma-Artefakt oder FigJam-Flow
  dokumentiert und nach Implementierung mit Screenshots abgeglichen werden.

## Metriken

| Metrik | Warum sie zaehlt |
| --- | --- |
| Time to first WEG | Wie schnell ein neuer Nutzer seine echte Gemeinschaft startklar bekommt. |
| Anteil verknuepfter Buchungen | Misst, ob Finanztransparenz wirklich entsteht. |
| Beschluss-Umsetzungsquote | Misst, ob Entscheidungen in Arbeit uebergehen. |
| Durchschnittliche Antwortzeit je Vorgang | Misst Servicequalitaet, nicht Chat-Aktivitaet. |
| Anzahl offener Pflichtschritte | Zeigt, ob die WEG rechtlich-operativ auf Kurs ist. |
| Mobile Task Completion | Zeigt, ob Eigentuemer ohne Desktop produktiv teilnehmen koennen. |

