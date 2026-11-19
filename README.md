# Legendas PT-BR — v1.7

Aplicativo Windows para encontrar e baixar legendas em português brasileiro. Modos automático e manual; vídeos individuais e pastas com subpastas.

## Compilar sem instalar Python no computador

1. Extraia o ZIP e envie **o conteúdo** para a raiz do repositório GitHub, incluindo `.github/workflows/build-windows.yml` e `assets/`.
2. Abra **Actions > Compilar Windows EXE > Run workflow**.
3. Quando a execução terminar, baixe o artefato `Legendas-PTBR-Windows-10`.
4. Extraia e execute `Legendas-PTBR-v1.7.exe` no Windows 10.

## Alterações da v1.7

Baseada diretamente na v1.5 para evitar a regressão observada nas versões 1.6 e 1.6.1. Busca e download da v1.5 foram preservados.

- **Cancelar após a operação atual:** o cancelamento é cooperativo; aguarda a pesquisa ou download atual terminar e impede que novos vídeos sejam iniciados.
- **Proteção de arquivos:** `.srt` já existente nunca é sobrescrito pelo aplicativo.
- **Pastas:** vídeos em subpastas continuam sendo processados.
- **Registro:** mensagens visíveis para resultados, erros e cancelamento; a linha de caminho do log de diagnóstico não é exibida.

Para logs técnicos, veja `%USERPROFILE%\\Legendas-PTBR`.

## Limitações

A qualidade e disponibilidade das legendas dependem dos provedores externos. A sincronização não é garantida. Esta versão não implementa novo algoritmo de classificação; mantém o da v1.5.
