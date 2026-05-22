#!/usr/bin/env node

const { spawnSync } = require("child_process");
const path = require("path");

const nativeExe = path.resolve(__dirname, "..", "dist", "native", "app-image", "codeclean", "codeclean.exe");
const singleFileExe = path.resolve(__dirname, "..", "dist", "native", "single-file", "codeclean.exe");
const javaLauncherExe = path.resolve(__dirname, "..", "dist", "codeclean", "codeclean.exe");
const fs = require("fs");
const exe = fs.existsSync(singleFileExe) ? singleFileExe : fs.existsSync(nativeExe) ? nativeExe : javaLauncherExe;
const result = spawnSync(exe, process.argv.slice(2), { stdio: "inherit" });

if (result.error) {
  console.error(`failed to start codeclean.exe: ${result.error.message}`);
  process.exit(1);
}

process.exit(result.status ?? 1);
