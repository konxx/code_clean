# codeclean CLI 使用文档

`codeclean` 是代码清理工具的命令行入口。npm 安装后会注册全局命令 `codeclean`，内部由 `npm-bin/codeclean.js` 转发到免 Java 单文件程序 `dist/native/single-file/codeclean.exe`。

## 安装

本地仓库安装：

```powershell
npm install -g .
```

发布到 npm 后安装：

```powershell
npm install -g @konxfun/codeclean
```

安装完成后重新打开 PowerShell，检查命令是否可用：

```powershell
codeclean
```

## 最小命令

```powershell
codeclean "软件名称" "项目路径"
```

示例：

```powershell
codeclean "库存管理系统" "D:\Projects\inventory"
```

## 完整命令

```powershell
codeclean "软件名称" "版本号" "项目路径" "指定文件后缀" "密匙" 1/2
```

示例：

```powershell
codeclean "库存管理系统" "V2.0" "D:\Projects\inventory" "py,sql,tsx,ts" "937599" 1
```

## 参数说明

| 参数 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- |
| 软件名称 | 是 | 无 | 生成文档使用的软件名称 |
| 版本号 | 否 | `V1.0` | 软件版本 |
| 项目路径 | 是 | 无 | 要扫描的源码目录 |
| 指定文件后缀 | 否 | `py,sql,tsx,ts` | 多个后缀用英文逗号分隔 |
| 密匙 | 否 | `937599` | 正确密匙生成前后 30 页文档，错误密匙生成全量备查文档 |
| 1/2 | 否 | `1` | `1` 表示打乱文件顺序，`2` 表示按路径排序 |

## 输出文件

命令会把 Word 文档生成到当前执行命令的目录。

密匙正确时：

```text
软件名称-代码(前后30页).docx
```

密匙错误或未匹配时：

```text
软件名称-代码(全量备份).docx
```

## 常用示例

使用默认版本、默认后缀、默认密匙、默认打乱顺序：

```powershell
codeclean "订单系统" "D:\Projects\order-system"
```

指定版本号和后缀：

```powershell
codeclean "订单系统" "V1.2" "D:\Projects\order-system" "py,sql"
```

不打乱文件顺序：

```powershell
codeclean "订单系统" "V1.2" "D:\Projects\order-system" "py,sql,tsx,ts" "937599" 2
```

## npm 入口说明

`package.json` 中的 `bin` 配置会注册命令：

```json
{
  "bin": {
    "codeclean": "npm-bin/codeclean.js"
  }
}
```

`npm-bin/codeclean.js` 会按优先级查找并执行：

1. `dist/native/single-file/codeclean.exe`
2. `dist/native/app-image/codeclean/codeclean.exe`
3. `dist/codeclean/codeclean.exe`

正常发布和安装时使用第一个，也就是免 Java 单文件版本。

## 发布前检查

生成免 Java 单文件 exe：

```powershell
npm run build
```

查看 npm 包内容：

```powershell
npm pack --dry-run
```

发布前需要确认 `package.json` 中没有：

```json
"private": true
```

首次公开发布 scoped 包：

```powershell
npm publish --access public
```

## 故障排查

如果提示找不到 `codeclean`：

```powershell
npm bin -g
```

确认输出目录是否已经在系统 `PATH` 中，然后重新打开 PowerShell。

如果 npm 发布失败提示未登录：

```powershell
npm adduser
npm whoami
```

如果生成文档失败，优先检查：

- 项目路径是否存在。
- 文件后缀是否写对。
- 当前目录是否有写入权限。
- `logs` 目录下是否有错误日志。
