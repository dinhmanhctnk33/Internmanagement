import fs from "node:fs/promises";
import { FileBlob, SpreadsheetFile } from "@oai/artifact-tool";

const inputPath = "outputs/sprint2-jira/Sprint_2_Jira_Backlog_Chi_Tiet.xlsx";
const outDir = "outputs/sprint2-jira/inspection";
await fs.mkdir(outDir, { recursive: true });
const wb = await SpreadsheetFile.importXlsx(await FileBlob.load(inputPath));
const summary = await wb.inspect({ kind: "workbook,sheet,table,region", maxChars: 20000, tableMaxRows: 30, tableMaxCols: 20, tableMaxCellChars: 300 });
console.log(summary.ndjson);
const sheetLines = (await wb.inspect({ kind: "sheet", include: "id,name", maxChars: 5000 })).ndjson.trim().split(/\r?\n/).map(JSON.parse);
for (const [i, item] of sheetLines.entries()) {
  const name = item.name;
  if (!name) continue;
  const sheet = wb.worksheets.getItem(name);
  const used = sheet.getUsedRange();
  await fs.writeFile(`${outDir}/sheet-${i + 1}.json`, JSON.stringify({ name, values: used.values, formulas: used.formulas }, null, 2), "utf8");
  const preview = await wb.render({ sheetName: name, autoCrop: "all", scale: 1, format: "png" });
  await fs.writeFile(`${outDir}/sheet-${i + 1}.png`, new Uint8Array(await preview.arrayBuffer()));
}
