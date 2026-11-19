# Legendas PT-BR — v2.1 (Windows + Linux)

Mesma interface e motor de legendas da versao 2.0; compilacao multiplataforma pelo GitHub Actions.

## Compilar sem instalar Python

1. Extraia o ZIP e envie **o conteudo da pasta** para a raiz do repositorio (inclua `.github/`, `assets/` e `packaging/`).
2. Em **Actions**, execute **Compilar Windows EXE** e/ou **Compilar Linux AppImage**.
3. Apos a execucao bem-sucedida, baixe os arquivos em **Artifacts**.

**Windows:** `Legendas-PTBR-v2.1.exe`.

**Linux (x86_64):** `Legendas-PTBR-v2.1-Linux-x86_64.AppImage`. Em um terminal na pasta do arquivo:

```bash
chmod +x Legendas-PTBR-v2.1-Linux-x86_64.AppImage
./Legendas-PTBR-v2.1-Linux-x86_64.AppImage
```

Se o AppImage reclamar de FUSE, instale a biblioteca `libfuse2` (Ubuntu 22.04 / derivados) ou execute com `APPIMAGE_EXTRACT_AND_RUN=1 ./Legendas-PTBR-v2.1-Linux-x86_64.AppImage`.

## Compatibilidade e observacoes

- Linux x86_64; compilado no Ubuntu 22.04 para ampliar compatibilidade com distribuicoes atuais. Nao ha garantia de compatibilidade com todas as distribuicoes/versoes, Wayland, ARM ou ambientes sem bibliotecas Qt/XCB.
- A barra de titulo escura e gerenciada pelo ambiente grafico no Linux. O ajuste DWM e executado somente no Windows.
- Logs tecnicos em `~/Legendas-PTBR/`, sem linha de diagnostico na interface.
- A busca e o download dependem de provedores externos; a sincronizacao nao e garantida.
- O GitHub Actions disponibiliza artefatos para download. **Nao anexa automaticamente os binarios a uma GitHub Release.**
- Se o GitHub reportar falha na etapa AppImage, confira o log do workflow; isso nao implica falha no programa Windows.

## O que foi alterado

Somente identificacao da versao, workflows de compilacao e arquivos de empacotamento Linux. Motor de busca, download e interface preservados da v2.0.

## Linux (TAR.GZ — v2.1.1)

Em **Actions → Compilar Linux TAR.GZ → Run workflow**, baixe o artefato `Legendas-PTBR-Linux-TAR-GZ` e extraia o ZIP do artefato. Extraia o `.tar.gz` resultante e execute, dentro da pasta extraída:

```bash
chmod +x iniciar.sh Legendas-PTBR
./iniciar.sh
```

O pacote usa PyInstaller `--onedir` e inclui as bibliotecas Python. O Linux ainda deve fornecer bibliotecas de sistema compatíveis. O workflow Windows foi preservado.
