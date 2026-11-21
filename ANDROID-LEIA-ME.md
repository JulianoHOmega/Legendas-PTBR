# OmegaSub para Android — primeira versão de testes

Este pacote **adiciona apenas** `android/`, `.github/workflows/build-android.yml` e este guia. Não altera nenhum arquivo desktop.

## Instalação em um commit

Extraia o ZIP e envie **o conteúdo**, não a pasta externa, para a raiz do repositório existente. Preserve os diretórios `android/` e `.github/workflows/`. Faça um commit na branch `main`.

Em **Actions → Compilar OmegaSub Android APK**, aguarde a compilação. No resumo da execução, baixe o artefato `OmegaSub-Android-debug`, extraia e instale `app-debug.apk` no Android (habilite instalação de fontes autorizadas quando solicitado). É um APK **debug** assinado com chave de desenvolvimento; não é uma versão de loja.

## O que funciona nesta primeira implementação

- Interface nativa Android em tema escuro, com o mesmo nome e arte original do desktop.
- Seleção de um vídeo por vez pelo seletor de documentos do Android.
- Busca PT-BR pela API **OpenSubtitles.com** (não a biblioteca Python Subliminal).
- Lista de resultados com releases; escolha manual ou tentativa automática da primeira opção.
- Download de `.srt` e seleção da pasta de destino pelo seletor de documentos do Android.
- API key salva nas preferências locais do aparelho.

## Configurar provedor

Crie sua chave de API em https://www.opensubtitles.com/ e insira no campo do aplicativo. A API pode impor cotas, autenticação adicional ou alterar seus requisitos; erros HTTP serão mostrados na tela. Não coloque uma chave pessoal no repositório público.

## Limitações honestas

- **Não foi compilado/testado neste ambiente.** O GitHub Actions será o primeiro teste de compilação.
- Não há paridade integral com o desktop: seleção de pasta/lote, identificação avançada de episódios, pontuação Subliminal e vários provedores ainda não foram portados.
- O modo automático escolhe o primeiro resultado da API, **não garante sincronização**.
- O Android salva por SAF; o usuário escolhe o destino, e não há sobrescrita silenciosa.
- Se a API devolver ZIP ou outro formato que não seja SRT/GZIP, será necessária adaptação adicional.
- O workflow usa `gradle` disponível no runner; se a imagem do GitHub Actions mudar, podemos adicionar o Gradle Wrapper ao projeto.

## Arquivos existentes

Os workflows Windows/Linux, `main.py`, `subtitle_engine.py`, testes e demais arquivos do desktop permanecem intocados.
