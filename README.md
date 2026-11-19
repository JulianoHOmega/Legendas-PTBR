# Legendas PT-BR v1.4

Aplicativo Windows 10/11 para buscar legendas PT-BR em vídeos individuais ou pastas inteiras.

## Novidades da versão 1.4
- Ícone personalizado no executável, janela e barra de tarefas.
- Coluna de idioma PT-BR.
- Limite de 150 resultados por vídeo para evitar travamentos com listas enormes.
- Remoção de IDs duplicados do mesmo provedor.
- Continua com os modos automático e manual.

## Compilar sem Python local
1. Extraia o ZIP e envie **o conteúdo** para a raiz do repositório GitHub, incluindo `.github` e `assets`.
2. Na aba Actions execute **Compilar Windows EXE** (ou faça push na branch principal).
3. Baixe o artefato `Legendas-PTBR-Windows-10`.
4. Extraia e execute `Legendas-PTBR.exe`.

O GitHub instala Python e dependências temporariamente no runner Windows. O usuário final não precisa de Python.

## Observações
- As legendas dependem dos provedores externos e podem não corresponder ao vídeo.
- A pontuação é heurística; não garante sincronização.
- Em modo automático, o programa salva a melhor pontuação, sem confirmação manual.
- Registro: `%USERPROFILE%\Legendas-PTBR\legendas.log`.
- O progresso exibido dentro do aplicativo representa arquivos processados, não progresso geral do desenvolvimento.
