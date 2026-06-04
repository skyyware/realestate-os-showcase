import { chromium } from 'playwright';
import { mkdir } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';

const baseUrl = process.env.QA_BASE_URL ?? 'https://realestate.localhost';
const outputDir = new URL('../output/qa/', import.meta.url);
const stamp = Date.now();
const user = {
  fullName: 'Sascha Dobrochynskyy',
  email: `qa+${stamp}@skyyware.com`,
  organizationName: 'SKYYWARE Product Engineering',
  password: `Ready2ship-${stamp}!`
};

await mkdir(outputDir, { recursive: true });

const browser = await chromium.launch();
const context = await browser.newContext({
  viewport: { width: 1440, height: 1100 },
  locale: 'de-DE',
  ignoreHTTPSErrors: true
});
const page = await context.newPage();
const desktopErrors = [];
page.on('console', message => {
  if (message.type() === 'error') desktopErrors.push(message.text());
});
page.on('pageerror', error => desktopErrors.push(error.message));

await page.goto(baseUrl, { waitUntil: 'networkidle' });
await page.screenshot({ path: fileURLToPath(new URL('realestate-register-desktop.png', outputDir)), fullPage: true });

await page.getByLabel('Name').fill(user.fullName);
await page.getByLabel('E-Mail').fill(user.email);
await page.getByLabel('Organisation').fill(user.organizationName);
const registrationResponse = page.waitForResponse(response => response.url().includes('/api/auth/register') && response.status() === 200);
await page.getByRole('button', { name: 'Aktivierungslink senden' }).click();
const registrationResult = await (await registrationResponse).json();
if (registrationResult.localSetupLink) {
  const token = new URL(registrationResult.localSetupLink).searchParams.get('token');
  await page.goto(`${baseUrl}/set-password?token=${token}`, { waitUntil: 'networkidle' });
}
await page.getByRole('heading', { name: 'Passwort vergeben' }).waitFor();
await page.getByLabel('Neues Passwort').fill(user.password);
await page.getByRole('button', { name: 'Account aktivieren' }).click();
await page.getByRole('heading', { name: 'Erste Immobilie hinzufügen' }).waitFor();

await page.getByLabel('Immobilie').fill('Musterstraße 12');
await page.getByLabel('Adresse').fill('Musterstraße 12');
await page.getByLabel('Stadt').fill('Stuttgart');
await page.getByLabel('Einheiten').fill('1');
await page.getByLabel('Wirtschaftsjahr').fill('2026');
await page.getByLabel('Kontostand').fill('125540.75');
await page.getByRole('spinbutton', { name: 'Rücklage', exact: true }).fill('256780.20');
await page.getByRole('spinbutton', { name: 'Ziel-Rücklage', exact: true }).fill('300000');
await page.getByLabel('MEA gesamt').fill('1000');
await page.getByLabel('Verwaltungsmodell').selectOption('SELF_MANAGED');
await page.getByRole('button', { name: 'Immobilie anlegen', exact: true }).click();
await page.getByRole('heading', { name: 'Ihr Immobilienportfolio' }).waitFor();
await page.getByText('Eigentümerstruktur fehlt').waitFor();

await page.getByRole('button', { name: 'Einheiten', exact: true }).click();
await page.getByPlaceholder('Einheit 01').fill('Einheit 07');
await page.getByPlaceholder('Eigentümer').fill(user.fullName);
await page.locator('form.units-form').getByPlaceholder('name@example.de').fill(user.email);
await page.getByPlaceholder('MEA').fill('1000');
await page.getByPlaceholder('Stimmgewicht').fill('1000');
await page.getByLabel('Nutzung').selectOption('OWNER_OCCUPIED');
await page.locator('form.units-form').getByRole('button', { name: 'Anlegen', exact: true }).click();
await page.getByText('Einheit 07').waitFor();
await page.getByText('Finanzraum bereit').waitFor();
await page.locator('form.member-form').getByRole('textbox', { name: 'Name', exact: true }).fill('Beirat Stuttgart');
await page.locator('form.member-form').getByPlaceholder('name@example.de').fill(`beirat+${stamp}@skyyware.com`);
await page.getByLabel('Rolle').selectOption('BOARD_MEMBER');
await page.getByRole('button', { name: 'Einladen', exact: true }).click();
await page.getByText('Rolle wurde eingeladen und dokumentiert.').waitFor();
await page.getByText('Beirat Stuttgart').waitFor();
await page.evaluate(() => window.scrollTo(0, 0));
await page.screenshot({ path: fileURLToPath(new URL('realestate-units-desktop.png', outputDir)), fullPage: true });

await page.getByRole('button', { name: 'Finanzen', exact: true }).click();
const firstUnitValue = await page.getByLabel('Einheit für Hausgeld').locator('option').nth(1).getAttribute('value');
await page.getByLabel('Einheit für Hausgeld').selectOption(firstUnitValue);
await page.getByLabel('Hausgeldjahr').fill('2026');
await page.getByPlaceholder('Hausgeld mtl.').fill('410');
await page.getByPlaceholder('Rücklage mtl.').fill('95');
await page.getByLabel('Gültig ab').fill('01.01.2026');
await page.getByLabel('Sollstatus').selectOption('ACTIVE');
await page.getByRole('button', { name: 'Soll anlegen', exact: true }).click();
await page.getByText('Hausgeld-Soll wurde für die Einheit angelegt.').waitFor();
await page.getByText('Offen 6.060,00').waitFor();
await page.getByPlaceholder('Betrag').fill('1250');
await page.getByLabel('Kategorie').selectOption('Instandhaltung');
await page.getByLabel('Verteilerschlüssel').selectOption('MEA');
await page.locator('input[formcontrolname="bookedOn"]').fill('05.06.2026');
await page.getByLabel('Fälligkeitsdatum').fill('20.06.2026');
await page.getByPlaceholder('Beschreibung').fill('Rechnung Hausmeisterservice');
await page.getByPlaceholder('Gegenpartei').fill('Hausmeisterservice Stuttgart');
await page.getByPlaceholder('Belegnummer').fill('HM-2026-118');
await page.getByPlaceholder('Dokumentreferenz').fill('rechnung-hm-2026-118.pdf');
await page.getByLabel('Buchungsstatus').selectOption('OPEN');
await page.getByRole('button', { name: 'Buchung erfassen', exact: true }).click();
await page.getByText('Rechnung Hausmeisterservice').waitFor();
await page.locator('form.plan-form').getByLabel('Wirtschaftsjahr').fill('2026');
await page.locator('form.plan-form').getByPlaceholder('Hausgeld-Budget').fill('64000');
await page.locator('form.plan-form').getByPlaceholder('Instandhaltung').fill('18500');
await page.locator('form.plan-form').getByPlaceholder('Rücklagenzuführung').fill('12000');
await page.locator('form.plan-form').getByLabel('Planstatus').selectOption('APPROVED');
await page.getByRole('button', { name: 'Wirtschaftsplan anlegen', exact: true }).click();
await page.getByText('Wirtschaftsplan 2026').waitFor();
await page.screenshot({ path: fileURLToPath(new URL('realestate-finances-desktop.png', outputDir)), fullPage: true });

await page.getByRole('button', { name: 'Dokumente', exact: true }).click();
await page.getByPlaceholder('Dokumenttitel').fill('Rechnung Hausmeisterservice');
await page.getByLabel('Dokumenttyp').selectOption('Rechnung');
await page.getByPlaceholder('Dateiname').fill('rechnung-hm-2026-118.pdf');
await page.locator('input[formcontrolname="documentDate"]').fill('03.06.2026');
await page.getByLabel('Dokumentenstatus').selectOption('APPROVED');
await page.getByLabel('Sichtbarkeit').selectOption('ALL_OWNERS');
await page.getByLabel('Quelle').selectOption('UPLOAD');
await page.getByLabel('Zuordnung').selectOption('FINANCE');
const financeDocumentTarget = await page.getByLabel('Zielobjekt').locator('option').nth(1).getAttribute('value');
await page.getByLabel('Zielobjekt').selectOption(financeDocumentTarget);
await page.getByPlaceholder('Kontext für Suche und Prüfung').fill('Geprüfter Beleg zur offenen Forderung.');
await page.getByRole('button', { name: 'Ablegen', exact: true }).click();
await page.locator('.row').filter({ hasText: 'Rechnung Hausmeisterservice' }).filter({ hasText: 'Geprüfter Beleg' }).waitFor();

await page.getByRole('button', { name: 'Beschlüsse', exact: true }).click();
await page.locator('form.meeting-form').getByPlaceholder('Versammlungstitel').fill('Eigentümerversammlung 2026');
await page.locator('form.meeting-form').getByLabel('Versammlungsdatum').fill('10.07.2026');
await page.locator('form.meeting-form').getByPlaceholder('Ort oder Videolink').fill('Stuttgart und digital');
await page.locator('form.meeting-form').getByLabel('Einladungsdatum').fill('10.06.2026');
await page.locator('form.meeting-form').getByLabel('Rückmeldefrist').fill('01.07.2026');
await page.locator('form.meeting-form').getByPlaceholder('Quorum / Mehrheit').fill('Einfache Mehrheit nach MEA');
await page.locator('form.meeting-form').getByLabel('Versammlungsstatus').selectOption('INVITED');
await page.locator('form.meeting-form').getByPlaceholder('Tagesordnung, Unterlagen und offene Beschlussvorlagen').fill('Jahresabrechnung, Wirtschaftsplan, Treppenhaus-Sanierung');
await page.getByRole('button', { name: 'Versammlung planen', exact: true }).click();
await page.locator('.meeting-list').getByText('Eigentümerversammlung 2026', { exact: true }).waitFor();
const meetingTarget = await page.getByLabel('Versammlungsbezug').locator('option').nth(1).getAttribute('value');
await page.getByLabel('Versammlungsbezug').selectOption(meetingTarget);
await page.getByPlaceholder('Beschlusstitel').fill('Sanierung Treppenhaus beauftragen');
await page.getByLabel('Beschlussdatum').fill('03.06.2026');
await page.getByPlaceholder('Ort oder Format').fill('Eigentümerversammlung');
await page.getByPlaceholder('Tagesordnungspunkt').fill('TOP 3 Treppenhaus-Sanierung');
await page.getByLabel('Umsetzungsfrist').fill('30.09.2026');
await page.getByPlaceholder('Verantwortlich').fill('Verwaltung');
await page.getByPlaceholder('Kostenwirkung').fill('18500');
await page.getByLabel('Ja-Stimmen').fill('14');
await page.getByLabel('Nein-Stimmen').fill('1');
await page.getByLabel('Enthaltungen').fill('1');
await page.getByPlaceholder('Wortlaut des Beschlusses').fill('Die Eigentümergemeinschaft beschließt, die Sanierung des Treppenhauses auf Basis des Angebots Nr. 24-118 zu beauftragen.');
await page.getByRole('button', { name: 'Eintragen', exact: true }).click();
await page.getByText('Sanierung Treppenhaus beauftragen').waitFor();
await page.locator('.decision-row').filter({ hasText: 'Sanierung Treppenhaus beauftragen' }).getByRole('button', { name: 'Umgesetzt' }).click();
await page.getByText('Beschluss als umgesetzt markiert.').waitFor();
await page.screenshot({ path: fileURLToPath(new URL('realestate-decisions-desktop.png', outputDir)), fullPage: true });

await page.getByRole('button', { name: 'Dokumente', exact: true }).click();
await page.getByPlaceholder('Dokumenttitel').fill('Protokoll JHV 2026');
await page.getByLabel('Dokumenttyp').selectOption('Protokoll');
await page.getByPlaceholder('Dateiname').fill('protokoll-jhv-2026.pdf');
await page.locator('input[formcontrolname="documentDate"]').fill('03.06.2026');
await page.getByLabel('Dokumentenstatus').selectOption('APPROVED');
await page.getByLabel('Sichtbarkeit').selectOption('ALL_OWNERS');
await page.getByLabel('Quelle').selectOption('UPLOAD');
await page.getByLabel('Zuordnung').selectOption('DECISION');
const decisionDocumentTarget = await page.getByLabel('Zielobjekt').locator('option').nth(1).getAttribute('value');
await page.getByLabel('Zielobjekt').selectOption(decisionDocumentTarget);
await page.getByPlaceholder('Kontext für Suche und Prüfung').fill('Beschlussprotokoll zur Sanierung.');
await page.getByRole('button', { name: 'Ablegen', exact: true }).click();
await page.locator('.row').filter({ hasText: 'Protokoll JHV 2026' }).filter({ hasText: 'Beschlussprotokoll' }).waitFor();
await page.screenshot({ path: fileURLToPath(new URL('realestate-documents-desktop.png', outputDir)), fullPage: true });

await page.getByRole('button', { name: 'Kommunikation', exact: true }).click();
await page.getByLabel('Empfänger').selectOption('Beirat');
await page.getByLabel('Kanal').selectOption('EMAIL');
await page.getByLabel('Mitteilungsstatus').selectOption('READY_TO_SEND');
await page.getByLabel('Versandbereit ab').fill('10.06.2026');
await page.getByLabel('Mitteilungskontext').selectOption('MEETING');
const messageTarget = await page.getByLabel('Mitteilungs-Zielobjekt').locator('option').nth(1).getAttribute('value');
await page.getByLabel('Mitteilungs-Zielobjekt').selectOption(messageTarget);
await page.getByPlaceholder('Betreff').fill('Eigentümerinformation zur Versammlung');
await page.getByPlaceholder('Nachricht an die Gemeinschaft').fill('Die Unterlagen für die nächste Eigentümerversammlung sind vorbereitet.');
await page.locator('form.communication-form').getByPlaceholder('Folgeaufgabe', { exact: true }).fill('Eigentümerversammlung vorbereiten');
await page.getByLabel('Priorität der Folgeaufgabe').selectOption('HIGH');
await page.locator('form.communication-form').getByPlaceholder('Verantwortlich').fill('Verwaltung');
await page.getByLabel('Folgeaufgabe fällig am').fill('12.06.2026');
await page.getByLabel('Folgeaufgabe Wiedervorlage').fill('09.06.2026');
await page.getByPlaceholder('Arbeitsanweisung für die Folgeaufgabe').fill('Einladungspaket finalisieren und Beschlüsse vorbereiten.');
await page.getByRole('button', { name: 'Mitteilung vorbereiten', exact: true }).click();
await page.getByText('Mitteilung und Folgeaufgabe wurden vorbereitet.').waitFor();
await page.getByText('Eigentümerinformation zur Versammlung').waitFor();
await page.getByText('Folgeaufgabe: Eigentümerversammlung vorbereiten').waitFor();
await page.screenshot({ path: fileURLToPath(new URL('realestate-communication-desktop.png', outputDir)), fullPage: true });

await page.getByRole('button', { name: 'Aufgaben', exact: true }).click();
if (await page.locator('input[formcontrolname="dueDate"]').getAttribute('type') === 'date') {
  throw new Error('Due-date input still uses native browser date UI instead of German date entry.');
}
await page.getByText('Eigentümerversammlung vorbereiten').waitFor();
let taskRow = page.locator('.task-row').filter({ hasText: 'Eigentümerversammlung vorbereiten' });
await taskRow.getByRole('button', { name: 'In Prüfung' }).click();
await page.getByText('Aufgabe ist in Prüfung.').waitFor();
taskRow = page.locator('.task-row').filter({ hasText: 'Eigentümerversammlung vorbereiten' });
await taskRow.getByRole('button', { name: 'Erledigen' }).click();
await page.getByText('Aufgabe erledigt.').waitFor();
await page.getByText('0 Vorgänge im Board').waitFor();

await page.getByRole('button', { name: 'Immobilien', exact: true }).click();
await page.getByLabel('Immobilie').fill('Neckarblick 4');
await page.getByLabel('Adresse').fill('Neckarblick 4');
await page.getByLabel('Stadt').fill('Stuttgart');
await page.getByLabel('Einheiten').fill('1');
await page.getByLabel('Wirtschaftsjahr').fill('2026');
await page.getByLabel('Kontostand').fill('42000');
await page.getByRole('spinbutton', { name: 'Rücklage', exact: true }).fill('88000');
await page.getByRole('spinbutton', { name: 'Ziel-Rücklage', exact: true }).fill('120000');
await page.getByLabel('MEA gesamt').fill('1000');
await page.getByLabel('Verwaltungsmodell').selectOption('SELF_MANAGED');
await page.getByRole('button', { name: 'Immobilie anlegen', exact: true }).click();
await page.getByRole('combobox', { name: 'Workspace auswählen' }).selectOption({ label: 'Neckarblick 4' });

await page.getByRole('button', { name: 'Einheiten', exact: true }).click();
await page.getByText('Noch keine Einheiten.').waitFor();
await page.getByPlaceholder('Einheit 01').fill('Einheit 02');
await page.getByPlaceholder('Eigentümer').fill('Beirat Stuttgart');
await page.locator('form.units-form').getByPlaceholder('name@example.de').fill(`beirat+${stamp}@skyyware.com`);
await page.getByPlaceholder('MEA').fill('1000');
await page.getByPlaceholder('Stimmgewicht').fill('1000');
await page.getByLabel('Nutzung').selectOption('OWNER_OCCUPIED');
await page.locator('form.units-form').getByRole('button', { name: 'Anlegen', exact: true }).click();
await page.getByText('Einheit 02').waitFor();
await Promise.all([
  page.waitForResponse(response => response.url().includes('/api/workspace/dashboard') && response.status() === 200),
  page.getByRole('combobox', { name: 'Workspace auswählen' }).selectOption({ label: 'Musterstraße 12' })
]);
await page.getByText('Einheit 07').waitFor();

await page.getByRole('button', { name: 'Übersicht', exact: true }).click();
await page.getByPlaceholder('Dokumente, Aufgaben oder Einheiten suchen').fill('Neckarblick');
await page.locator('.property-row').filter({ hasText: 'Neckarblick 4' }).waitFor();
await page.getByPlaceholder('Dokumente, Aufgaben oder Einheiten suchen').fill('');

await page.getByRole('button', { name: 'Aktivität', exact: true }).click();
await page.getByText('Technischer Nachweis').waitFor();
await page.getByText('Mitteilung vorbereitet: Eigentümerinformation zur Versammlung', { exact: true }).waitFor();
await page.getByText('Folgeaufgabe aus Mitteilung angelegt: Eigentümerversammlung vorbereiten', { exact: true }).waitFor();
await page.screenshot({ path: fileURLToPath(new URL('realestate-audit-desktop.png', outputDir)), fullPage: true });

await page.getByRole('button', { name: 'Einstellungen', exact: true }).click();
await page.getByText('Rollen & Rechte').waitFor();
await page.getByText('WEG-Admin').waitFor();
await page.getByText('Finanzen steuern').waitFor();
await page.getByRole('button', { name: 'Einstellungen speichern', exact: true }).click();
await page.getByText('Einstellungen wurden gespeichert.').waitFor();

await page.getByRole('button', { name: 'Übersicht', exact: true }).click();
await page.getByText('125.540,75').waitFor();
await page.screenshot({ path: fileURLToPath(new URL('realestate-dashboard-desktop.png', outputDir)), fullPage: true });

const token = await page.evaluate(() => localStorage.getItem('realestate.token'));
await context.close();

const mobile = await browser.newContext({
  viewport: { width: 390, height: 900 },
  isMobile: true,
  locale: 'de-DE',
  ignoreHTTPSErrors: true
});
await mobile.addInitScript((accessToken) => {
  localStorage.setItem('realestate.token', accessToken);
}, token);
const mobilePage = await mobile.newPage();
const mobileErrors = [];
mobilePage.on('console', message => {
  if (message.type() === 'error') mobileErrors.push(message.text());
});
mobilePage.on('pageerror', error => mobileErrors.push(error.message));
await mobilePage.goto(baseUrl, { waitUntil: 'networkidle' });
await mobilePage.getByRole('heading', { name: 'Ihr Immobilienportfolio' }).waitFor();
await mobilePage.screenshot({ path: fileURLToPath(new URL('realestate-dashboard-mobile.png', outputDir)), fullPage: true });
await mobile.close();
await browser.close();

if (desktopErrors.length || mobileErrors.length) {
  throw new Error(`Browser errors detected: ${[...desktopErrors, ...mobileErrors].join(' | ')}`);
}

console.log(JSON.stringify({
  baseUrl,
  email: user.email,
  screenshots: [
    'output/qa/realestate-register-desktop.png',
    'output/qa/realestate-units-desktop.png',
    'output/qa/realestate-finances-desktop.png',
    'output/qa/realestate-documents-desktop.png',
    'output/qa/realestate-decisions-desktop.png',
    'output/qa/realestate-communication-desktop.png',
    'output/qa/realestate-audit-desktop.png',
    'output/qa/realestate-dashboard-desktop.png',
    'output/qa/realestate-dashboard-mobile.png'
  ]
}, null, 2));
