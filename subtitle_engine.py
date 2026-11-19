"""Subtitle discovery/download via Subliminal providers."""
from pathlib import Path
import re
from subliminal import scan_video, list_subtitles, download_subtitles, save_subtitles
from babelfish import Language
from matching import evaluate, choose_automatic

EXTENSIONS = {'.mkv', '.mp4', '.avi', '.mov', '.wmv', '.m4v', '.ts', '.webm'}
LANGUAGE = Language('por', 'BR')


def videos_in(path):
    target = Path(path)
    if target.is_file():
        return [target] if target.suffix.lower() in EXTENSIONS else []
    return sorted(p for p in target.rglob('*') if p.is_file() and p.suffix.lower() in EXTENSIONS)


def subtitle_exists(path):
    p = Path(path)
    return any(p.with_suffix(ext).exists() for ext in ('.srt', '.ass', '.ssa', '.sub')) or any(
        x.is_file() and x.suffix.lower() in {'.srt', '.ass', '.ssa'} and x.stem.lower().startswith(p.stem.lower() + '.')
        for x in p.parent.iterdir()
    )


def subtitle_score(subtitle, video):
    """Subliminal versions return either an int or a (score, matches) tuple."""
    from subliminal.score import compute_score
    result = compute_score(subtitle, video)
    return result[0] if isinstance(result, (tuple, list)) else result


def release_label(sub):
    """Prefer provider release metadata; never invent a release."""
    for attr in ('release_info', 'release', 'filename'):
        value = getattr(sub, attr, None)
        if value:
            if isinstance(value, (list, tuple, set)):
                value = ', '.join(map(str, value))
            label = re.sub(r'\s+', ' ', str(value)).strip()
            if label:
                return label[:140]
    return 'Não informado'


def candidate_key(sub):
    """Keep distinct IDs; deduplicate exact provider/release pairs when ID absent."""
    provider = str(getattr(sub, 'provider_name', '')).casefold()
    identifier = getattr(sub, 'id', None)
    if identifier is not None:
        return (provider, 'id', str(identifier))
    release = release_label(sub)
    if release == 'Não informado':
        return (provider, 'object', id(sub))
    return (provider, 'release', release.casefold(), str(getattr(sub, 'language', '')))


def search(path):
    video = scan_video(str(path))
    results = list(list_subtitles({video}, {LANGUAGE}).get(video, []))
    results = [s for s in results if s.language == LANGUAGE]
    unique = []
    seen = set()
    for item in results:
        key = candidate_key(item)
        if key in seen:
            continue
        seen.add(key)
        try:
            score = subtitle_score(item, video)
            assessment = evaluate(video, item, score, release_label(item))
            unique.append({'subtitle': item, 'provider': item.provider_name,
                           'release': release_label(item), 'language': 'PT-BR',
                           'score': score, 'assessment': assessment})
        except Exception:
            # One malformed result must not abort the entire search.
            continue
    unique.sort(key=lambda item: item['assessment']['rank'], reverse=True)
    return video, unique[:150]


def describe(row, video=None):
    return row


def best_automatic(rows):
    return choose_automatic(rows)


def download(path, video, subtitle):
    # Fetch subtitle content and save with the video's basename.
    download_subtitles([subtitle])
    if not subtitle.content:
        raise RuntimeError('O provedor não retornou o conteúdo da legenda.')
    output = Path(path).with_suffix('.srt')
    if output.exists():
        raise FileExistsError(f'Já existe uma legenda em {output.name}; arquivo preservado.')
    save_subtitles(video, [subtitle], single=True)
    if not output.exists():
        raise RuntimeError('A legenda foi baixada, mas não foi encontrada no destino esperado.')
    return output
