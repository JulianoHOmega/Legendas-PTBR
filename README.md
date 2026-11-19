# Legendas PT-BR

Aplicativo Windows para localizar e baixar legendas externas em português brasileiro para filmes e séries.

## Funcionalidades

- Interface gráfica em português, tema escuro
- Seleção de um ou vários arquivos de vídeo
- Varredura recursiva de pastas
- Modo automático: baixa a legenda com maior pontuação
- Modo manual: mostra resultados e permite escolher
- Ignora arquivos com legenda externa existente (opcional)
- Salva o `.srt` com o mesmo nome do vídeo, na mesma pasta

## Instalação e execução

Instale Python 3.11 ou 3.12 no Windows e marque **Add Python to PATH**. Clique duas vezes em `iniciar.bat`. Na primeira execução, o script instala dependências (precisa de internet).

Alternativa via terminal:

```powershell
py -3 -m venv .venv
.venv\Scripts\python.exe -m pip install -r requirements.txt
.venv\Scripts\python.exe main.py
```

## Gerar EXE

Execute `compilar_windows.bat` em um computador Windows. Se a compilação for bem-sucedida, o arquivo estará em `dist\Legendas-PTBR.exe`. O EXE ainda precisará de internet para pesquisar e baixar legendas.

## Provedores e limitações

A pesquisa usa os provedores habilitados por padrão no projeto open source **Subliminal**. Disponibilidade, limites de uso, bloqueios regionais, mudanças de API e exigências de login são definidos por cada provedor. O programa não contorna autenticação, paywalls ou restrições. Provedores podem deixar de funcionar sem aviso. Não há garantia de encontrar legenda para todos os vídeos nem de sincronização perfeita.

O arquivo de vídeo é analisado localmente para extrair metadados e identificadores; o software consulta serviços externos para localizar legendas. Consulte as políticas de privacidade dos provedores.

**Importante:** esta versão não pesquisa o conteúdo de sites arbitrários e não integra APIs que exijam chave pessoal. O suporte aos provedores precisa ser testado na sua rede antes de considerarmos a busca validada em produção.

## Próximos passos

- Configuração de chaves/API e credenciais de provedores
- Melhorias de busca tolerante a erros de digitação
- Cancelamento de pesquisa e download manual em segundo plano
- Histórico de legendas e identificação de idioma embutido no MKV
- Integração contínua no GitHub Actions para compilar o EXE

## Licença

MIT. Consulte LICENSE.

## Diagnóstico v1.2
Esta versão registra erros em `%USERPROFILE%\Legendas-PTBR\legendas.log` e falhas nativas em `falhas-nativas.log`. Se o aplicativo fechar durante a pesquisa, anexe esses arquivos ao relato (remova caminhos pessoais se desejar). A atualização também modifica o encerramento da thread Qt para reduzir problemas de ciclo de vida. Ainda requer teste no Windows 10.
