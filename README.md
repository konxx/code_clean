# 代码清理工具

这是一个用于扫描指定项目目录中的源代码文件，并生成 Word 文档材料的工具。项目保留了 Java Swing 桌面界面，同时提供命令行入口 `codeclean`。

当前界面已调整为自用版本：窗口标题为“代码清理”，默认文件后缀为 `py,sql,tsx,ts`，默认密钥为 `937599`。

## 目录结构

- `src/main/java`：应用源码，包含界面、文件扫描和文档生成逻辑。
- `src/main/resources`：应用资源文件，包括 Word 模板、图标和日志配置。
- `lib`：本地依赖 jar 包，构建和运行时需要保留。
- `logo.png`：窗口图标，程序启动时从仓库根目录读取。
- `build.ps1`：编译脚本，输出到 `target/classes`。
- `build-cli.ps1`：编译并生成可加入 `PATH` 的 CLI 程序。
- `download-packaging-tools.ps1`：下载带 `jpackage` 的 JDK 和 WiX。
- `build-standalone.ps1`：生成免安装 Java 的 jpackage 产物。
- `install-cli.ps1`：将 CLI 程序目录加入用户级 `PATH`。
- `package.json`：npm 全局安装配置。
- `run.ps1`：先编译，再启动桌面程序。
- `codeclean.cmd`：CLI 命令启动器。

## 主要代码

- `com.source.SourceDocxApplication`：程序入口。
- `com.source.ui.Ui`：Swing 界面、表单默认值、按钮事件和窗口设置。
- `com.source.docx.SourceWord`：扫描源代码文件并生成 Word 文档。

## 构建

在仓库根目录执行：

```powershell
.\build.ps1
```

构建产物会生成到 `target/classes`。该目录属于编译产物，已在 `.gitignore` 中忽略。

## 桌面界面运行

在仓库根目录执行：

```powershell
.\run.ps1
```

脚本会先执行构建，然后启动桌面界面。

## CLI 构建与安装

生成可加入环境变量的 CLI 程序：

```powershell
.\build-cli.ps1
```

构建完成后会生成 `dist/codeclean` 目录，里面包含：

- `codeclean.exe`：Windows CLI 启动程序。
- `codeclean.cmd`：命令入口，加入 `PATH` 后可直接执行 `codeclean`。
- `codeclean.jar`：CLI 主程序。
- `lib`：运行依赖。

将 `dist/codeclean` 加入环境变量 `PATH` 后，重新打开 PowerShell，即可在任意目录直接执行 `codeclean`。

也可以执行安装脚本自动加入用户级 `PATH`：

```powershell
.\install-cli.ps1
```

也可以使用 npm 全局安装：

```powershell
npm install -g .
```

npm 安装时会自动构建免 Java 单文件版本，并注册全局 `codeclean` 命令。安装后的命令会优先调用 `dist/native/single-file/codeclean.exe`。

## 免 Java 运行产物

下载打包工具并生成自带 Java runtime 的版本：

```powershell
.\build-standalone.ps1
```

该脚本会下载 Microsoft Build of OpenJDK 21，并生成：

- `dist/native/app-image/codeclean/codeclean.exe`：自带 runtime 的应用目录入口，不要求目标机器安装 Java。
- `dist/native/single-file/codeclean.exe`：单文件自解压 exe，不要求目标机器安装 Java。
- `dist/native/installer/*.exe`：单文件安装包 exe，执行 `-BuildInstaller` 时会下载并使用 WiX 生成。

说明：`jpackage` 在 Windows 上的单文件 exe 是安装包形式；绿色免安装运行可使用 `dist/native/app-image/codeclean` 整个目录，或使用本项目额外生成的 `dist/native/single-file/codeclean.exe` 自解压单文件。

如需额外尝试生成 Windows 安装包 exe：

```powershell
.\build-standalone.ps1 -BuildInstaller
```

## CLI 使用

最小命令：

```powershell
codeclean "软件名称" "项目路径"
```

完整命令：

```powershell
codeclean "软件名称" "版本号" "项目路径" "指定文件后缀" "密匙" 1/2
```

参数说明：

- `软件名称`：必填。
- `版本号`：可选，默认 `V1.0`。
- `项目路径`：必填，要扫描的源码目录。
- `指定文件后缀`：可选，默认 `py,sql,tsx,ts`，多个后缀用英文逗号分隔。
- `密匙`：可选，默认 `937599`。
- `1/2`：可选，`1` 表示打乱顺序，`2` 表示不打乱，默认 `1`。

命令行生成的 Word 文档会输出到当前执行命令的目录。

## 使用说明

1. 填写软件名称和版本号。
2. 点击“选择”选择要扫描的项目目录。
3. 确认文件后缀，默认扫描 `py,sql,tsx,ts`。
4. 保持默认密钥 `937599` 可生成“源代码前后30页”文档。
5. 不填写或填写错误密钥时，生成全量备查源代码文档。
6. 如需随机打乱文件顺序，可打开“是否打乱顺序”开关。
7. 点击“生成”开始生成 Word 文档。

## 注意事项

- `lib` 目录是当前项目的本地依赖来源，不要删除或忽略提交。
- `logs` 和 `target` 是运行/构建产生的目录，不需要提交。
- 项目使用本地 Java 17 编译环境，未配置 Maven 或 Gradle。
- 当前源码来自反编译后的项目，部分代码保留了原有 UI Designer 生成风格。
