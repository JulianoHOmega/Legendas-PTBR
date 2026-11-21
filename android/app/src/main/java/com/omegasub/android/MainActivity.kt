package com.omegasub.android

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.util.concurrent.Executors

/** Busca pública experimental: nenhuma credencial ou chave é solicitada ao usuário. */
class MainActivity : Activity() {
    private val bg = Color.rgb(23, 27, 36)
    private val panel = Color.rgb(34, 40, 53)
    private val accent = Color.rgb(67, 92, 143)
    private val white = Color.rgb(233, 237, 245)
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var selected: TextView
    private lateinit var status: TextView
    private lateinit var results: LinearLayout
    private lateinit var mode: Spinner
    private var videoName = ""
    private var selectedSub: Subtitle? = null
    private var pendingBytes: ByteArray? = null
    private val pickVideo = 100
    private val saveSubtitle = 101
    private data class Subtitle(val title: String, val index: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); isFillViewport = true }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(20))
        }
        scroll.addView(root)
        setContentView(scroll)
        root.addView(ImageView(this).apply {
            setImageResource(R.drawable.omegasub_logo)
            adjustViewBounds = true
            layoutParams = LinearLayout.LayoutParams(dp(90), dp(90))
        })
        root.addView(text("🎬  OmegaSub  ·  Legendas PT-BR", 22f, true))
        root.addView(text("Android • busca de legendas sem conta ou chave", 13f, false))
        root.addView(space())
        root.addView(button("Selecionar vídeo") {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE)
            }, pickVideo)
        })
        selected = text("Nenhum vídeo selecionado", 14f, false)
        root.addView(selected)
        root.addView(space())
        root.addView(text("Modo:", 14f, true))
        mode = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Automático (primeira opção)", "Manual"))
        }
        mode.setSelection(0)
        root.addView(mode)
        root.addView(button("Buscar legendas PT-BR") { search() })
        status = text("Selecione um vídeo para começar.", 13f, false)
        root.addView(status)
        root.addView(space())
        root.addView(text("Resultados", 17f, true))
        results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(results)
        root.addView(button("Baixar legenda selecionada") {
            if (selectedSub == null) message("Selecione uma legenda primeiro.") else download()
        })
        root.addView(text("Motor de busca: Subliminal (Python integrado). Os provedores disponíveis podem exigir credenciais ou estar indisponíveis. Salve a legenda na pasta desejada.", 12f, false))
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun space() = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(14)) }
    private fun text(value: String, size: Float, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size; setTextColor(white)
        if (bold) setTypeface(null, Typeface.BOLD)
        setPadding(0, dp(5), 0, dp(5))
    }
    private fun button(value: String, onClick: () -> Unit) = Button(this).apply {
        text = value; isAllCaps = false; setTextColor(white)
        background = GradientDrawable().apply { setColor(accent); cornerRadius = dp(7).toFloat() }
        setPadding(dp(10), dp(9), dp(10), dp(9))
        layoutParams = LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(9) }
        setOnClickListener { onClick() }
    }
    private fun message(value: String) { status.text = value }

    private fun pythonModule() : com.chaquo.python.PyObject {
        if (!Python.isStarted()) Python.start(AndroidPlatform(this))
        return Python.getInstance().getModule("omegasub_bridge")
    }

    private fun search() {
        if (videoName.isBlank()) { message("Selecione um vídeo."); return }
        selectedSub = null
        results.removeAllViews()
        message("Buscando legendas PT-BR em provedores compatíveis...")
        executor.execute {
            try {
                val entries = pythonModule().callAttr("search", videoName).asList()
                val subtitles = entries.mapIndexed { index, item ->
                    Subtitle(item.toString(), index)
                }
                runOnUiThread {
                    message(if (subtitles.isEmpty()) "Nenhuma legenda PT-BR encontrada pelos provedores disponíveis." else "${subtitles.size} legenda(s) encontrada(s).")
                    subtitles.forEachIndexed { index, sub ->
                        results.addView(button("${index + 1}. ${sub.title.take(130)}") {
                            selectedSub = sub
                            message("Selecionada: ${sub.title}")
                        })
                    }
                    if (subtitles.isNotEmpty() && mode.selectedItemPosition == 0) {
                        selectedSub = subtitles.first()
                        download()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread { message("Falha na busca: ${e.cause?.message ?: e.message}") }
            }
        }
    }

    private fun download() {
        val sub = selectedSub ?: return
        message("Baixando legenda PT-BR...")
        executor.execute {
            try {
                val bytes = pythonModule().callAttr("download", sub.index).toJava(ByteArray::class.java)
                if (bytes.isEmpty()) error("O provedor retornou legenda vazia")
                runOnUiThread {
                    pendingBytes = bytes
                    startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/x-subrip"
                        putExtra(Intent.EXTRA_TITLE, videoName.substringBeforeLast('.') + ".srt")
                    }, saveSubtitle)
                    message("Escolha onde salvar a legenda.")
                }
            } catch (e: Exception) {
                runOnUiThread { message("Falha no download: ${e.cause?.message ?: e.message}") }
            }
        }
    }

    @Deprecated("Activity result API")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) return
        val uri: Uri = data.data!!
        if (requestCode == pickVideo) {
            videoName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            } ?: uri.lastPathSegment.orEmpty()
            selected.text = "Selecionado: $videoName"
            search()
        } else if (requestCode == saveSubtitle) {
            try {
                val bytes = pendingBytes ?: error("Download não disponível")
                contentResolver.openOutputStream(uri)?.use { it.write(bytes) } ?: error("Destino indisponível")
                message("Legenda salva com sucesso.")
            } catch (e: Exception) { message("Falha ao salvar: ${e.message}") }
            finally { pendingBytes = null }
        }
    }
    override fun onDestroy() { executor.shutdownNow(); super.onDestroy() }
}
