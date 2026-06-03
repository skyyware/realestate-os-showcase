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
await page.getByRole('button', { name: 'Aktivierungslink senden' }).click();
await page.getByRole('heading', { name: 'Passwort vergeben' }).waitFor();
await page.getByLabel('Neues Passwort').fill(user.password);
await page.getByRole('button', { name: 'Account aktivieren' }).click();
await page.getByRole('heading', { name: 'Erste Immobilie hinzufügen' }).waitFor();

await page.getByLabel('Immobilie').fill('Musterstraße 12');
await page.getByLabel('Adresse').fill('Musterstraße 12');
await page.getByLabel('Stadt').fill('Stuttgart');
await page.getByLabel('Einheiten').fill('16');
await page.getByLabel('Kontostand').fill('125540.75');
await page.getByLabel('Rücklage').fill('256780.20');
await page.getByRole('button', { name: 'Immobilie anlegen' }).click();
await page.getByRole('heading', { name: 'Immobilienportfolio' }).waitFor();

await page.getByRole('button', { name: /Einheiten/ }).click();
await page.getByPlaceholder('Einheit 01').fill('Einheit 07');
await page.getByPlaceholder('Eigentümer').fill(user.fullName);
await page.getByPlaceholder('MEA').fill('84.50');
await page.getByRole('button', { name: 'Anlegen' }).click();
await page.getByText('Einheit 07').waitFor();

await page.getByRole('button', { name: /Finanzen/ }).click();
await page.getByPlaceholder('Rechnung oder Buchung').fill('Rechnung Hausmeisterservice');
await page.getByPlaceholder('Betrag').fill('-1250');
await page.getByPlaceholder('Kategorie').fill('Instandhaltung');
await page.locator('input[formcontrolname="bookedOn"]').fill('05.06.2026');
await page.locator('select[formcontrolname="status"]').selectOption('OPEN');
await page.getByRole('button', { name: 'Erfassen' }).click();
await page.getByText('Rechnung Hausmeisterservice').waitFor();

await page.getByRole('button', { name: /Dokumente/ }).click();
await page.getByPlaceholder('Dokumenttitel').fill('Protokoll JHV 2026');
await page.getByPlaceholder('Dateiname').fill('protokoll-jhv-2026.pdf');
await page.locator('input[formcontrolname="documentDate"]').fill('03.06.2026');
await page.getByRole('button', { name: 'Ablegen' }).click();
await page.getByText('Protokoll JHV 2026').waitFor();

await page.getByRole('button', { name: /Aufgaben/ }).click();
await page.getByPlaceholder('Aufgabe').fill('Eigentümerversammlung vorbereiten');
await page.locator('select[formcontrolname="priority"]').selectOption('HIGH');
if (await page.locator('input[formcontrolname="dueDate"]').getAttribute('type') === 'date') {
  throw new Error('Due-date input still uses native browser date UI instead of German date entry.');
}
await page.locator('input[formcontrolname="dueDate"]').fill('12.06.2026');
await page.getByPlaceholder('Beschreibung').fill('Einladungspaket finalisieren und Beschlüsse vorbereiten.');
await page.getByRole('button', { name: 'Anlegen' }).click();
await page.getByText('Eigentümerversammlung vorbereiten').waitFor();

await page.getByRole('button', { name: /Immobilien/ }).click();
await page.getByLabel('Immobilie').fill('Neckarblick 4');
await page.getByLabel('Adresse').fill('Neckarblick 4');
await page.getByLabel('Stadt').fill('Stuttgart');
await page.getByLabel('Einheiten').fill('8');
await page.getByLabel('Kontostand').fill('42000');
await page.getByLabel('Rücklage').fill('88000');
await page.getByRole('button', { name: 'Immobilie anlegen' }).click();
await page.getByRole('combobox', { name: 'Workspace auswählen' }).selectOption({ label: 'Neckarblick 4 · Stuttgart' });

await page.getByRole('button', { name: /Einheiten/ }).click();
await page.getByText('Noch keine Einheiten.').waitFor();
await page.getByPlaceholder('Einheit 01').fill('Einheit 02');
await page.getByPlaceholder('Eigentümer').fill('Beirat Stuttgart');
await page.getByPlaceholder('MEA').fill('92.00');
await page.getByRole('button', { name: 'Anlegen' }).click();
await page.getByText('Einheit 02').waitFor();
await Promise.all([
  page.waitForResponse(response => response.url().includes('/api/workspace/dashboard') && response.status() === 200),
  page.getByRole('combobox', { name: 'Workspace auswählen' }).selectOption({ label: 'Musterstraße 12 · Stuttgart' })
]);
await page.getByText('Einheit 07').waitFor();

await page.getByRole('button', { name: /Übersicht/ }).click();
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
await mobilePage.getByRole('heading', { name: 'Immobilienportfolio' }).waitFor();
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
    'output/qa/realestate-dashboard-desktop.png',
    'output/qa/realestate-dashboard-mobile.png'
  ]
}, null, 2));
