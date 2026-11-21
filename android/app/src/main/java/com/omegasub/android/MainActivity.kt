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
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors
import java.util.zip.ZipInputStream
import java.util.zip.GZIPInputStream

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
    private data class Subtitle(val title: String, val pageUrl: String)

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
                listOf("Manual", "Automático (primeira opção)"))
        }
        mode.setSelection(1)
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
        root.addView(text("Fonte experimental: Podnapisi. A disponibilidade depende do site e das legendas publicadas. O arquivo é salvo na pasta escolhida por você.", 12f, false))
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

    private fun fetch(url: String, maxBytes: Int = 8_000_000): Pair<ByteArray, String> {
        require(url.startsWith("https://www.podnapisi.net/") || url.startsWith("https://podnapisi.net/")) { "Endereço de origem não permitido" }
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 12000
        connection.readTimeout = 20000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("User-Agent", "OmegaSubAndroid/0.2 (subtitle client)")
        return try {
            val status = connection.responseCode
            if (status in 300..399) {
                val location = connection.getHeaderField("Location") ?: error("Redirecionamento sem destino")
                val resolved = URL(URL(url), location).toString()
                connection.disconnect()
                return fetch(resolved, maxBytes)
            }
            if (status !in 200..299) error("Servidor retornou HTTP $status")
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val n = input.read(buffer)
                    if (n < 0) break
                    if (output.size() + n > maxBytes) error("Arquivo excede limite de tamanho")
                    output.write(buffer, 0, n)
                }
                output.toByteArray()
            }
            bytes to (connection.contentType ?: "")
        } finally { connection.disconnect() }
    }

    private fun search() {
        if (videoName.isBlank()) { message("Selecione um vídeo."); return }
        selectedSub = null
        results.removeAllViews()
        message("Pesquisando legendas PT-BR...")
        val query = videoName.substringBeforeLast('.').replace(Regex("[._]+"), " ").trim()
        executor.execute {
            try {
                val url = "https://www.podnapisi.net/subtitles/search/?keywords=" +
                    URLEncoder.encode(query, "UTF-8") + "&language=pb"
                val html = fetch(url).first.toString(Charsets.UTF_8)
                val document = Jsoup.parse(html, url)
                val found = linkedMapOf<String, Subtitle>()
                for (link in document.select("a[href*=/subtitles/]")) {
                    val href = link.absUrl("href")
                    val title = link.text().trim().replace(Regex("\\s+"), " ")
                    if (title.length < 4 || href.contains("/search/") || href.contains("/download")) continue
                    if (!href.startsWith("https://www.podnapisi.net/subtitles/") && !href.startsWith("https://podnapisi.net/subtitles/")) continue
                    // Only subtitle detail pages, not menu/category links.
                    if (URL(href).path.trimEnd('/').split('/').size < 3) continue
                    found[href] = Subtitle(title, href)
                    if (found.size >= 80) break
                }
                runOnUiThread {
                    val subtitles = found.values.toList()
                    message(if (subtitles.isEmpty()) "Nenhum resultado encontrado. A fonte pode ter alterado sua página ou não possuir legenda PT-BR." else "${subtitles.size} resultado(s) encontrado(s). Confira o título antes de baixar.")
                    subtitles.forEachIndexed { index, sub ->
                        results.addView(button("${index + 1}. ${sub.title.take(100)}") {
                            selectedSub = sub; message("Selecionada: ${sub.title}")
                        })
                    }
                    if (subtitles.isNotEmpty() && mode.selectedItemPosition == 1) {
                        selectedSub = subtitles.first(); download()
                    }
                }
            } catch (e: Exception) { runOnUiThread {
                val reason = if (e is java.net.UnknownHostException)
                    "Fonte de legendas indisponível: não foi possível localizar o servidor Podnapisi. Não é um problema com seu vídeo. Uma nova fonte precisa ser integrada."
                else "Falha na busca: ${e.message}"
                message(reason)
            } }
        }
    }

    private fun download() {
        val sub = selectedSub ?: return
        message("Obtendo legenda...")
        executor.execute {
            try {
                val page = Jsoup.parse(fetch(sub.pageUrl).first.toString(Charsets.UTF_8), sub.pageUrl)
                val candidates = page.select("a[href*=/download], a[download]")
                val downloadUrl = candidates.map { it.absUrl("href") }.firstOrNull {
                    it.startsWith("https://www.podnapisi.net/") || it.startsWith("https://podnapisi.net/")
                } ?: error("O site não disponibilizou um link de download reconhecido")
                val (raw, contentType) = fetch(downloadUrl)
                val subtitleBytes = unpack(raw, contentType)
                if (subtitleBytes.isEmpty()) error("Legenda vazia")
                val filename = videoName.substringBeforeLast('.') + ".srt"
                runOnUiThread {
                    pendingBytes = subtitleBytes
                    startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/x-subrip"
                        putExtra(Intent.EXTRA_TITLE, filename)
                    }, saveSubtitle)
                    message("Escolha onde salvar $filename")
                }
            } catch (e: Exception) { runOnUiThread { message("Falha no download: ${e.message}") } }
        }
    }

    private fun unpack(bytes: ByteArray, contentType: String): ByteArray {
        if (bytes.size >= 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()) {
            return GZIPInputStream(bytes.inputStream()).use { it.readNBytes(8_000_001) }.also {
                if (it.size > 8_000_000) error("Legenda grande demais")
            }
        }
        if (bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4b.toByte()) {
            ZipInputStream(bytes.inputStream()).use { zip ->
                while (true) {
                    val entry = zip.nextEntry ?: break
                    if (!entry.isDirectory && entry.name.substringAfterLast('/').endsWith(".srt", true)) {
                        val output = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val n = zip.read(buffer)
                            if (n < 0) break
                            if (output.size() + n > 8_000_000) error("Legenda grande demais")
                            output.write(buffer, 0, n)
                        }
                        return output.toByteArray()
                    }
                }
            }
            error("O arquivo ZIP não contém legenda .srt")
        }
        if (contentType.contains("html", true) || bytes.toString(Charsets.UTF_8).trimStart().startsWith("<!DOCTYPE", true)) {
            error("O provedor retornou uma página HTML, não uma legenda")
        }
        return bytes
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
