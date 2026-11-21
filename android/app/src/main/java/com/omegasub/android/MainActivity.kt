package com.omegasub.android

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val bg = Color.rgb(23,27,36)
    private val panel = Color.rgb(34,40,53)
    private val accent = Color.rgb(67,92,143)
    private val white = Color.rgb(233,237,245)
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var root: LinearLayout
    private lateinit var selected: TextView
    private lateinit var status: TextView
    private lateinit var list: LinearLayout
    private lateinit var keyInput: EditText
    private lateinit var mode: Spinner
    private var videoUri: Uri? = null
    private var videoName = ""
    private var options = mutableListOf<Subtitle>()
    private var chosen: Subtitle? = null
    private data class Subtitle(val fileId: Int, val release: String, val provider: String, val language: String)
    private val pickVideo = 100
    private val saveSubtitle = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); isFillViewport = true }
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18),dp(22),dp(18),dp(20)) }
        scroll.addView(root)
        setContentView(scroll)
        val logo = ImageView(this).apply { setImageResource(R.drawable.omegasub_logo); adjustViewBounds = true; layoutParams = LinearLayout.LayoutParams(dp(90),dp(90)) }
        root.addView(logo)
        root.addView(text("🎬  OmegaSub  ·  Legendas PT-BR", 22f, true))
        root.addView(text("Android • busca e download de legendas", 13f, false))
        root.addView(space())
        root.addView(button("Selecionar vídeo") {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*"; addCategory(Intent.CATEGORY_OPENABLE) }, pickVideo)
        })
        selected = text("Nenhum vídeo selecionado", 14f, false)
        root.addView(selected)
        root.addView(space())
        root.addView(text("Chave de API OpenSubtitles.com", 14f, true))
        keyInput = EditText(this).apply {
            setSingleLine(true); setTextColor(white); setHintTextColor(Color.LTGRAY); hint = "Cole sua API key"; setBackgroundColor(panel)
            setPadding(dp(12),dp(10),dp(12),dp(10))
            setText(getPreferences(MODE_PRIVATE).getString("api_key", ""))
        }
        root.addView(keyInput)
        root.addView(text("A chave fica armazenada apenas neste aparelho. A API exige uma chave gratuita do provedor.",12f,false))
        root.addView(space())
        root.addView(text("Modo:",14f,true))
        mode = Spinner(this).apply { adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Manual", "Automático (primeira opção)")) }
        root.addView(mode)
        root.addView(button("Buscar legendas PT-BR") { search() })
        status = text("Pronto para pesquisar.",13f,false)
        root.addView(status)
        root.addView(space())
        root.addView(text("Resultados",17f,true))
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(list)
        root.addView(button("Baixar legenda selecionada") {
            if (chosen == null) message("Selecione um resultado primeiro.") else download()
        })
        root.addView(text("O Android permite salvar a legenda na pasta escolhida por você. O nome sugerido é igual ao do vídeo.",12f,false))
    }
    private fun dp(x:Int)= (x*resources.displayMetrics.density).toInt()
    private fun space()= View(this).apply { layoutParams = LinearLayout.LayoutParams(1,dp(14)) }
    private fun text(s:String,size:Float,bold:Boolean): TextView = TextView(this).apply {
        text=s; textSize=size; setTextColor(white); if(bold) setTypeface(null,Typeface.BOLD)
        setPadding(0,dp(5),0,dp(5))
    }
    private fun button(s:String,click:()->Unit): Button = Button(this).apply {
        text=s; isAllCaps=false; setTextColor(white); background=GradientDrawable().apply { setColor(accent); cornerRadius=dp(7).toFloat() }
        setPadding(dp(10),dp(9),dp(10),dp(9)); layoutParams=LinearLayout.LayoutParams(-1,dp(48)).apply { bottomMargin=dp(9) }
        setOnClickListener { click() }
    }
    private fun message(s:String){ status.text=s }
    private fun network(url:String, method:String="GET", body:String?=null, key:String):String {
        val c=URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod=method; c.connectTimeout=15000; c.readTimeout=20000
            c.setRequestProperty("Api-Key",key); c.setRequestProperty("User-Agent","OmegaSubAndroid v0.1")
            c.setRequestProperty("Accept","application/json")
            if(body!=null){c.doOutput=true;c.setRequestProperty("Content-Type","application/json");c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }}
            val stream=if(c.responseCode in 200..299) c.inputStream else c.errorStream
            val response=stream?.bufferedReader()?.use {it.readText()} ?: ""
            if(c.responseCode !in 200..299) throw IllegalStateException("HTTP ${c.responseCode}: ${response.take(260)}")
            return response
        } finally {c.disconnect()}
    }
    private fun search() {
        if(videoName.isBlank()) {message("Selecione um vídeo.");return}
        val key=keyInput.text.toString().trim()
        if(key.isBlank()){message("Informe sua chave de API do OpenSubtitles.com.");return}
        getPreferences(MODE_PRIVATE).edit().putString("api_key",key).apply()
        chosen=null; options.clear(); list.removeAllViews(); message("Pesquisando...")
        val query=videoName.substringBeforeLast('.').replace(Regex("[._]+")," ").trim()
        executor.execute {
            try {
                val encoded=URLEncoder.encode(query,"UTF-8")
                val result=JSONObject(network("https://api.opensubtitles.com/api/v1/subtitles?languages=pt-br&query=$encoded",key=key))
                val data=result.optJSONArray("data")
                val found=mutableListOf<Subtitle>()
                if(data!=null) for(i in 0 until data.length()) {
                    val attr=data.getJSONObject(i).optJSONObject("attributes") ?: continue
                    val files=attr.optJSONArray("files") ?: continue
                    if(files.length()==0)continue
                    val id=files.getJSONObject(0).optInt("file_id",0)
                    if(id==0)continue
                    val release=attr.optString("release","").ifBlank { files.getJSONObject(0).optString("file_name","Release não informada") }
                    found.add(Subtitle(id,release,"OpenSubtitles",attr.optString("language","pt-br")))
                }
                runOnUiThread {
                    options=found.take(100).toMutableList()
                    message("${options.size} legenda(s) encontrada(s). Confira a sincronização antes de usar.")
                    options.forEachIndexed { index, sub ->
                        val item=button("${index+1}. ${sub.release.take(90)}  •  ${sub.language}") { chosen=sub; message("Selecionada: ${sub.release}") }
                        list.addView(item)
                    }
                    if(options.isEmpty()) message("Nenhuma legenda PT-BR encontrada para: $query")
                    else if(mode.selectedItemPosition==1) {chosen=options.first();download()}
                }
            } catch(e:Exception){runOnUiThread {message("Erro na busca: ${e.message}")}}
        }
    }
    private fun download() {
        val sub=chosen ?: return
        val key=keyInput.text.toString().trim()
        message("Solicitando download...")
        executor.execute {
            try {
                val body=JSONObject().put("file_id",sub.fileId).toString()
                val response=JSONObject(network("https://api.opensubtitles.com/api/v1/download", "POST",body,key))
                val link=response.getString("link")
                if(!link.startsWith("https://")) throw IllegalStateException("Link inseguro recebido.")
                val connection=URL(link).openConnection() as HttpURLConnection
                connection.connectTimeout=15000;connection.readTimeout=30000
                val bytes=try { connection.inputStream.use {it.readBytes()} } finally {connection.disconnect()}
                if(bytes.size>10_000_000) throw IllegalStateException("Legenda excede limite de 10 MB")
                val content=when {
                    bytes.size>=2 && bytes[0]==0x1f.toByte() && bytes[1]==0x8b.toByte() -> java.util.zip.GZIPInputStream(bytes.inputStream()).use {it.readBytes()}
                    else -> bytes
                }
                if(content.size>10_000_000) throw IllegalStateException("Legenda descompactada excede 10 MB")
                val name=videoName.substringBeforeLast('.')+".srt"
                runOnUiThread {
                    pendingBytes=content
                    startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE);type="application/x-subrip";putExtra(Intent.EXTRA_TITLE,name)
                    },saveSubtitle)
                    message("Escolha onde salvar $name")
                }
            } catch(e:Exception){runOnUiThread {message("Erro no download: ${e.message}")}}
        }
    }
    private var pendingBytes:ByteArray?=null
    @Deprecated("Activity result API")
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        if(resultCode!=RESULT_OK || data?.data==null)return
        val uri=data.data!!
        if(requestCode==pickVideo){
            videoUri=uri
            videoName=contentResolver.query(uri,null,null,null,null)?.use { cursor ->
                val idx=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if(idx>=0 && cursor.moveToFirst())cursor.getString(idx) else null
            } ?: uri.lastPathSegment.orEmpty()
            selected.text="Selecionado: $videoName"
        } else if(requestCode==saveSubtitle){
            try {
                val bytes=pendingBytes ?: throw IllegalStateException("Download não disponível")
                contentResolver.openOutputStream(uri)?.use {it.write(bytes)} ?: throw IllegalStateException("Não foi possível abrir o destino")
                message("Legenda salva com sucesso.")
            } catch(e:Exception){message("Falha ao salvar: ${e.message}")}
            finally {pendingBytes=null}
        }
    }
    override fun onDestroy(){executor.shutdownNow();super.onDestroy()}
}
