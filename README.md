# Legendas PT-BR — v2.0

Programa Windows 10 para pesquisar e baixar legendas PT-BR em modo automatico ou manual. Aceita filmes, episodios de series e pastas inteiras.

## Compilar no GitHub, sem instalar Python no PC

1. Extraia o ZIP e envie o **conteudo da pasta** `Legendas-PTBR-v2.0` para a raiz do repositorio GitHub. Inclua `.github/workflows/build-windows.yml` e `assets/`.
2. Acesse **Actions > Compilar Windows EXE > Run workflow**.
3. Baixe o artefato `Legendas-PTBR-Windows-10` e extraia `Legendas-PTBR-v2.0.exe`.

## Atualizacoes visuais da v2.0

- Cabecalho da tabela sem quadrado branco no canto superior esquerdo.
- Barra de titulo escura quando suportada pelo Windows 10 (usa DWM, sem alterar a janela nativa).
- Mantem icone personalizado, modos automatico e manual, identificacao de series por pastas, cancelamento e protecao de legendas existentes.
- A linha de caminho do log de diagnostico nao aparece na interface; arquivos de log tecnico continuam em `%USERPROFILE%\Legendas-PTBR`.

## Limites

Os provedores podem falhar ou nao disponibilizar legendas. A pontuacao e uma estimativa, e a sincronizacao deve ser conferida no player. A barra de titulo escura depende da versao e das configuracoes do Windows.
