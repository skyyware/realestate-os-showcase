import { mkdirSync } from "node:fs";
import { resolve } from "node:path";
import { pathToFileURL } from "node:url";
import { chromium } from "playwright";

const root = process.cwd();
const source = resolve(root, "docs", "application", "application.html");
const output = resolve(root, "output", "pdf", "sascha-dobrochynskyy-dotega-bewerbung.pdf");
mkdirSync(resolve(root, "output", "pdf"), { recursive: true });

const browser = await chromium.launch({ headless: true });
const page = await browser.newPage();
await page.goto(pathToFileURL(source).href, { waitUntil: "networkidle" });
await page.pdf({
  path: output,
  format: "A4",
  printBackground: true,
  preferCSSPageSize: true
});
await browser.close();
console.log(`Application PDF written to ${output}`);
