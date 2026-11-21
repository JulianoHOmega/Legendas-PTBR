"""Experimental Subliminal integration for Android. No user API-key UI.
Searches using video filename, avoiding copying large video files from Android URIs.
Provider credentials are NOT bypassed: providers requiring authentication may fail.
"""
from babelfish import Language
from subliminal import list_subtitles, download_subtitles
from subliminal.video import Video

_RESULTS = []
_VIDEO = None
LANG = Language('por', 'BR')


def search(filename):
    global _RESULTS, _VIDEO
    _RESULTS = []
    _VIDEO = Video.fromname(filename)
    found = list_subtitles({_VIDEO}, {LANG}).get(_VIDEO, [])
    found = [sub for sub in found if sub.language == LANG]
    from subliminal.score import compute_score
    def score(sub):
        value = compute_score(sub, _VIDEO)
        return value[0] if isinstance(value, (tuple, list)) else value
    found.sort(key=score, reverse=True)
    seen = set()
    for sub in found:
        key = (sub.provider_name, str(getattr(sub, 'id', '')))
        if key in seen:
            continue
        seen.add(key)
        _RESULTS.append(sub)
        if len(_RESULTS) >= 100:
            break
    return [f"{sub.provider_name} | {getattr(sub, 'release_info', None) or getattr(sub, 'id', 'Legenda PT-BR')} | compatibilidade {score(sub)}" for sub in _RESULTS]


def download(index):
    sub = _RESULTS[int(index)]
    download_subtitles([sub])
    if not sub.content:
        raise RuntimeError('O provedor não retornou conteúdo.')
    if len(sub.content) > 8_000_000:
        raise RuntimeError('Legenda maior que o limite permitido.')
    return bytes(sub.content)
