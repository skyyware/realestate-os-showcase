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

await page.goto(baseUrl, { waitUntil: 'networkidle' });
await page.screenshot({ path: fileURLToPath(new URL('realestate-register-desktop.png', outputDir)), fullPage: true });

await page.getByLabel('Name').fill(user.fullName);
await page.getByLabel('E-Mail').fill(user.email);
await page.getByLabel('Organisation').fill(user.organizationName);
await page.getByRole('button', { name: 'Aktivierungslink senden' }).click();
await page.getByRole('heading', { name: 'Passwort vergeben' }).waitFor();
await page.getByLabel('Neues Passwort').fill(user.password);
await page.getByRole('button', { name: 'Account aktivieren' }).click();
await page.getByRole('heading', { name: 'Aufgabensteuerung' }).waitFor();

await page.getByPlaceholder('Aufgabe').fill('Eigentümerbeschluss für Solarprüfung vorbereiten');
await page.locator('select[formcontrolname="priority"]').selectOption('HIGH');
await page.getByPlaceholder('TT.MM.JJJJ').fill('08.06.2026');
await page.getByPlaceholder('Beschreibung').fill('Beschlussvorlage, Budgetrahmen und Angebote in einem Vorgang bündeln.');
await page.getByRole('button', { name: 'Anlegen' }).click();
await page.locator('.list .row strong', { hasText: 'Eigentümerbeschluss für Solarprüfung vorbereiten' }).first().waitFor();
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
await mobilePage.goto(baseUrl, { waitUntil: 'networkidle' });
await mobilePage.getByRole('heading', { name: 'Aufgabensteuerung' }).waitFor();
await mobilePage.screenshot({ path: fileURLToPath(new URL('realestate-dashboard-mobile.png', outputDir)), fullPage: true });
await mobile.close();
await browser.close();

console.log(JSON.stringify({
  baseUrl,
  email: user.email,
  screenshots: [
    'output/qa/realestate-register-desktop.png',
    'output/qa/realestate-dashboard-desktop.png',
    'output/qa/realestate-dashboard-mobile.png'
  ]
}, null, 2));
