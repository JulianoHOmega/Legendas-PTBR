import sys
from pathlib import Path
from PySide6.QtCore import QObject, QThread, Signal, Slot, Qt
from PySide6.QtWidgets import (QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QPushButton, QLabel, QFileDialog, QComboBox, QTableWidget, QTableWidgetItem,
    QHeaderView, QProgressBar, QTextEdit, QMessageBox, QCheckBox)
from subtitle_engine import videos_in, subtitle_exists, search, describe, download


class Worker(QObject):
    log = Signal(str)
    progress = Signal(int, int)
    candidates = Signal(object)
    finished = Signal()

    def __init__(self, paths, automatic, skip_existing):
        super().__init__()
        self.paths = paths
        self.automatic = automatic
        self.skip_existing = skip_existing

    @Slot()
    def run(self):
        try:
            total = len(self.paths)
            for index, path in enumerate(self.paths, 1):
                self.log.emit(f'[{index}/{total}] {path.name}')
                try:
                    if self.skip_existing and subtitle_exists(path):
                        self.log.emit('  Ignorado: legenda externa já existente.')
                        continue
                    video, subtitles = search(path)
                    if not subtitles:
                        self.log.emit('  Nenhuma legenda PT-BR encontrada.')
                        continue
                    if self.automatic:
                        dest = download(path, video, subtitles[0])
                        self.log.emit(f'  Salvo: {dest.name}')
                    else:
                        rows = [describe(sub, video) for sub in subtitles]
                        self.candidates.emit((str(path), video, rows))
                        self.log.emit(f'  {len(rows)} opção(ões) encontradas.')
                except Exception as exc:
                    self.log.emit(f'  Erro: {exc}')
                finally:
                    self.progress.emit(index, total)
        finally:
            self.finished.emit()


class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle('Legendas PT-BR — 1.0')
        self.resize(940, 660)
        self.paths = []
        self.matches = []
        self.thread = None
        root = QWidget()
        self.setCentralWidget(root)
        layout = QVBoxLayout(root)
        layout.setSpacing(10)
        title = QLabel('Legendas PT-BR')
        title.setStyleSheet('font-size: 24px; font-weight: 700;')
        layout.addWidget(title)
        line = QHBoxLayout()
        self.file_btn = QPushButton('Selecionar vídeos')
        self.folder_btn = QPushButton('Selecionar pasta')
        self.file_btn.clicked.connect(self.pick_files)
        self.folder_btn.clicked.connect(self.pick_folder)
        line.addWidget(self.file_btn)
        line.addWidget(self.folder_btn)
        layout.addLayout(line)
        self.selected = QLabel('Nenhum vídeo selecionado')
        self.selected.setWordWrap(True)
        layout.addWidget(self.selected)
        options = QHBoxLayout()
        options.addWidget(QLabel('Modo:'))
        self.mode = QComboBox()
        self.mode.addItems(['Automático', 'Manual'])
        options.addWidget(self.mode)
        self.skip = QCheckBox('Ignorar vídeos com legenda externa existente')
        self.skip.setChecked(True)
        options.addWidget(self.skip)
        options.addStretch()
        layout.addLayout(options)
        self.start = QPushButton('Buscar legendas')
        self.start.clicked.connect(self.begin)
        layout.addWidget(self.start)
        self.progress = QProgressBar()
        layout.addWidget(self.progress)
        layout.addWidget(QLabel('Resultados (modo manual):'))
        self.table = QTableWidget(0, 4)
        self.table.setHorizontalHeaderLabels(['Vídeo', 'Provedor', 'Release', 'Pontuação'])
        self.table.horizontalHeader().setSectionResizeMode(0, QHeaderView.ResizeMode.Stretch)
        self.table.horizontalHeader().setSectionResizeMode(2, QHeaderView.ResizeMode.Stretch)
        self.table.setSelectionBehavior(QTableWidget.SelectionBehavior.SelectRows)
        self.table.setSelectionMode(QTableWidget.SelectionMode.SingleSelection)
        layout.addWidget(self.table)
        self.get_selected = QPushButton('Baixar legenda selecionada')
        self.get_selected.clicked.connect(self.download_selected)
        self.get_selected.setEnabled(False)
        layout.addWidget(self.get_selected)
        layout.addWidget(QLabel('Registro:'))
        self.log = QTextEdit()
        self.log.setReadOnly(True)
        self.log.setMaximumHeight(145)
        layout.addWidget(self.log)
        self.setStyleSheet('''QWidget { background: #171b24; color: #e9edf5; font-size: 13px; }
            QPushButton { background: #344c7d; border: 0; border-radius: 6px; padding: 10px; }
            QPushButton:hover { background: #4565a5; }
            QPushButton:disabled { background: #353945; color: #8e929b; }
            QComboBox, QTableWidget, QTextEdit { background: #222835; border: 1px solid #394154; border-radius: 5px; padding: 4px; }
            QHeaderView::section { background: #2c3547; padding: 6px; }
            QProgressBar { border: 1px solid #394154; border-radius: 5px; text-align: center; }
            QProgressBar::chunk { background: #5287d7; }''')

    def pick_files(self):
        files, _ = QFileDialog.getOpenFileNames(self, 'Selecionar vídeos', '', 'Vídeos (*.mkv *.mp4 *.avi *.mov *.wmv *.m4v *.ts *.webm)')
        if files:
            self.paths = [Path(x) for x in files]
            self.selected.setText(f'{len(self.paths)} vídeo(s) selecionado(s)')

    def pick_folder(self):
        folder = QFileDialog.getExistingDirectory(self, 'Selecionar pasta')
        if folder:
            self.paths = videos_in(folder)
            self.selected.setText(f'{len(self.paths)} vídeo(s) encontrados em {folder} (incluindo subpastas)')

    def begin(self):
        if not self.paths:
            QMessageBox.information(self, 'Aviso', 'Selecione vídeos ou uma pasta primeiro.')
            return
        self.matches.clear()
        self.table.setRowCount(0)
        self.get_selected.setEnabled(False)
        self.progress.setMaximum(len(self.paths))
        self.progress.setValue(0)
        self.start.setEnabled(False)
        self.file_btn.setEnabled(False)
        self.folder_btn.setEnabled(False)
        self.thread = QThread()
        self.worker = Worker(self.paths[:], self.mode.currentIndex() == 0, self.skip.isChecked())
        self.worker.moveToThread(self.thread)
        self.thread.started.connect(self.worker.run)
        self.worker.log.connect(self.log.append)
        self.worker.progress.connect(lambda i, total: self.progress.setValue(i))
        self.worker.candidates.connect(self.add_matches)
        self.worker.finished.connect(self.thread.quit)
        self.worker.finished.connect(self.worker.deleteLater)
        self.thread.finished.connect(self.thread.deleteLater)
        self.thread.finished.connect(self.done)
        self.thread.start()

    @Slot(object)
    def add_matches(self, result):
        path, video, rows = result
        for item in rows:
            row = self.table.rowCount()
            self.table.insertRow(row)
            self.matches.append((path, video, item['subtitle']))
            for col, value in enumerate((Path(path).name, item['provider'], item['release'], str(item['score']))):
                cell = QTableWidgetItem(value)
                cell.setFlags(cell.flags() & ~Qt.ItemFlag.ItemIsEditable)
                self.table.setItem(row, col, cell)
        self.get_selected.setEnabled(bool(self.matches))

    @Slot()
    def done(self):
        self.start.setEnabled(True)
        self.file_btn.setEnabled(True)
        self.folder_btn.setEnabled(True)
        self.log.append('Pesquisa concluída.')
        self.thread = None

    def download_selected(self):
        row = self.table.currentRow()
        if row < 0 or row >= len(self.matches):
            QMessageBox.information(self, 'Aviso', 'Selecione uma linha da tabela.')
            return
        path, video, subtitle = self.matches[row]
        try:
            output = download(path, video, subtitle)
            self.log.append(f'Legenda salva: {output}')
            QMessageBox.information(self, 'Sucesso', f'Legenda salva em:\n{output}')
        except Exception as exc:
            self.log.append(f'Falha ao baixar: {exc}')
            QMessageBox.warning(self, 'Erro', str(exc))


if __name__ == '__main__':
    app = QApplication(sys.argv)
    window = MainWindow()
    window.show()
    sys.exit(app.exec())
