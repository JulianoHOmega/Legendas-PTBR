"""Subtitle discovery/download via Subliminal providers."""
from pathlib import Path
import re
from subliminal import scan_video, list_subtitles, download_subtitles, save_subtitles
from babelfish import Language

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


def search(path):
    video = scan_video(str(path))
    results = list(list_subtitles({video}, {LANGUAGE}).get(video, []))
    # Keep PT-BR and rank by release matching (Subliminal scoring).
    results = [s for s in results if s.language == LANGUAGE]
    results.sort(key=lambda s: subtitle_score(s, video), reverse=True)
    return video, results


def describe(sub, video):
    score = subtitle_score(sub, video)
    return {'provider': sub.provider_name, 'release': str(getattr(sub, 'release_info', '') or 'Não informado').replace('\n', ' ')[:140], 'score': score, 'subtitle': sub}


def download(path, video, subtitle):
    # Fetch subtitle content and save with the video's basename.
    download_subtitles([subtitle])
    if not subtitle.content:
        raise RuntimeError('O provedor não retornou o conteúdo da legenda.')
    save_subtitles(video, [subtitle], single=True)
    output = Path(path).with_suffix('.srt')
    if not output.exists():
        raise RuntimeError('A legenda foi baixada, mas não foi encontrada no destino esperado.')
    return output
