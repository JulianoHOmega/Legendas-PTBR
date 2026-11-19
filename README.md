# Legendas PT-BR — v1.5

Aplicativo Windows para pesquisar e baixar legendas PT-BR de vídeos individuais ou pastas inteiras, com modos automático e manual.

## Novidades v1.5
- Apresentação mais consistente de metadados de release, quando o provedor os informa.
- Remoção de resultados repetidos por ID do provedor; fallback para release/idioma quando não há ID.
- Aviso de que a pontuação é uma estimativa e não garante sincronização.
- Removida da interface a linha inicial que exibia o caminho do log de diagnóstico. Os logs técnicos permanecem disponíveis para investigar falhas.
- Ícone personalizado preservado e configuração de compilação atualizada.

## Compilar no GitHub
Envie o conteúdo desta pasta para a raiz do repositório, incluindo `.github/workflows/build-windows.yml` e `assets`. Abra **Actions**, execute **Compilar Windows EXE** e baixe o artefato gerado. Não é necessário instalar Python no computador.

## Observações
A pesquisa depende dos provedores externos e pode falhar por indisponibilidade ou restrições de acesso. Teste as legendas no player; resultados com pontuação alta podem estar incorretos. O modo automático salva a primeira opção classificada. Use o modo manual para conferir as alternativas.
