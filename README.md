# Legendas PT-BR — v1.6

Aplicativo para Windows 10 com interface PySide6, busca de legendas PT-BR por Subliminal e download em modo automático ou manual.

## Melhorias v1.6

- Compara título, ano, temporada/episódio e informações de release quando disponíveis.
- Penaliza e sinaliza incompatibilidades explícitas.
- Ordena os resultados com pontuação adicional e apresenta coluna **Compatibilidade**; passe o mouse para ler o motivo.
- O modo automático evita candidatos com dados explicitamente incompatíveis.
- A avaliação não garante sincronização e pode ser limitada pelos metadados retornados pelos provedores.

## Compilar sem instalar Python no PC

1. Envie **o conteúdo** desta pasta para a raiz do repositório GitHub (incluindo `.github` e `assets`).
2. Acesse **Actions → Compilar Windows EXE → Run workflow**.
3. Ao terminar, baixe o artefato **Legendas-PTBR-Windows-10**, contendo `Legendas-PTBR-v1.6.exe`.

## Diagnóstico

Logs técnicos permanecem em `%USERPROFILE%\\Legendas-PTBR\\legendas.log`. O caminho do log não é exibido na interface.

## Limitações

A versão 1.6 ainda precisa de testes reais no Windows. A busca depende dos provedores disponíveis e da conexão com a internet. O download pode retornar legendas que necessitam de ajuste de sincronização.
