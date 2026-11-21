# OmegaSub Android v0.3 — teste experimental Subliminal

Este pacote **altera apenas `android/`, o workflow Android e este documento**. Os arquivos desktop não são modificados.

## Instalação
Copie os arquivos e pastas deste ZIP para a raiz do repositório, preservando caminhos. Commit único; GitHub Actions gerará `OmegaSub.apk` se a compilação tiver sucesso.

## O que foi implementado
- Chaquopy com Python 3.11 e `subliminal==2.2.1` no APK.
- Busca pelo nome do arquivo de vídeo sem copiar o vídeo para a memória.
- Filtragem de legendas PT-BR, classificação por compatibilidade e download pelo Subliminal.
- Modo automático selecionado por padrão; modo manual disponível.
- Nenhum campo de chave de API; nome do APK `OmegaSub.apk`.

## Limitações importantes
- **Não compilado nem testado em dispositivo** neste ambiente. O empacotamento pode falhar por dependências Python nativas, incompatibilidade de versões ou provedores. Envie o log do Actions caso falhe.
- Nem todos os provedores Subliminal funcionam sem credenciais; não há promessa de resultados em todos os títulos.
- O aplicativo usa metadados do **nome do vídeo**, não hash nem inspeção de seu conteúdo.
- O modo automático escolhe o primeiro resultado e abre o seletor de destino para salvar. O app não modifica o arquivo de vídeo.
- Não foi implementada compatibilidade completa com a lógica customizada do PC (detecção de episódios atípicos e classificação adicional).
