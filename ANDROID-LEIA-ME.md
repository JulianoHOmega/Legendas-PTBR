# OmegaSub Android — busca sem chave (protótipo experimental v0.2)

Este pacote contém **somente dois arquivos alterados**. Não altera Windows/Linux, logo ou nome do aplicativo.

## Instalação

Substitua no repositório:

- `android/app/src/main/java/com/omegasub/android/MainActivity.kt`
- `android/app/build.gradle.kts`

Faça um commit. Em Actions, execute o workflow Android existente e baixe o APK debug.

## Funcionamento

Ao escolher um vídeo, o aplicativo tenta pesquisar na página pública do Podnapisi, filtrando PT-BR, sem exigir conta nem chave. O download usa links publicados nas páginas encontradas e extrai `.srt` de ZIP quando necessário. A interface e o ícone originais são preservados.

## Limitações IMPORTANTES

- **Não foi compilado nem testado em dispositivo** neste ambiente.
- **Não foi possível validar o acesso ao Podnapisi** daqui. Mudanças no HTML, bloqueios ou ausência de legendas podem fazer a busca ou o download falhar.
- A filtragem por idioma e a estrutura de links dependem do site. Não se deve considerar os resultados garantidamente PT-BR sem confirmação.
- Este protótipo ainda não replica os múltiplos provedores e o ranqueamento do Subliminal no PC.
- Confira as regras do provedor antes de distribuir amplamente um cliente que automatiza consultas.
- Não há chave de API nem servidor próprio, mas há conexão à internet.

Se a compilação falhar, envie o log do Actions; se compilar e não encontrar legendas, envie a mensagem exibida no app para adaptarmos a integração.
