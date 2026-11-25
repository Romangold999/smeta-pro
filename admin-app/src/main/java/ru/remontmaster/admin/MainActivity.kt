package ru.remontmaster.admin

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ADMIN_URL = "https://romangold999.github.io/smeta-pro/remont-master/admin/"
private const val KEY_ALIAS = "remontmaster_admin_token_v1"

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var root: LinearLayout
    private var fileCallback: android.webkit.ValueCallback<Array<Uri>>? = null
    private val pickerCode = 7214

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(25, 22, 17)
        window.navigationBarColor = Color.rgb(16, 15, 13)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(16, 15, 13))
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), 0, dp(12), 0)
            setBackgroundColor(Color.rgb(25, 22, 17))
        }

        val appTitle = TextView(this).apply {
            text = "RemontMaster"
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
        toolbar.addView(
            appTitle,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
        )

        val refreshButton = TextView(this).apply {
            text = "↻  Обновить страницу"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(15), dp(9), dp(15), dp(9))
            background = GradientDrawable().apply {
                setColor(Color.rgb(48, 74, 86))
                cornerRadius = dp(10).toFloat()
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                web.reload()
            }
        }
        toolbar.addView(
            refreshButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(40)
            )
        )
        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )

        web = WebView(this)
        root.addView(
            web,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        setContentView(root)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = false
        web.settings.allowContentAccess = true
        web.settings.javaScriptCanOpenWindowsAutomatically = false
        web.settings.setSupportMultipleWindows(false)
        web.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
        web.addJavascriptInterface(TokenBridge(), "RemontMasterNative")

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url ?: return false
                if (!request.isForMainFrame) return false
                if (uri.scheme == "https" && uri.host == "romangold999.github.io") return false
                runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url?.startsWith(ADMIN_URL) == true) {
                    installTokenObserver()
                    restoreLoginIfAvailable()
                }
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                callback: android.webkit.ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = callback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params?.mode == FileChooserParams.MODE_OPEN_MULTIPLE)
                }
                return try {
                    startActivityForResult(Intent.createChooser(intent, "Выбрать файлы"), pickerCode)
                    true
                } catch (_: Exception) {
                    fileCallback?.onReceiveValue(null)
                    fileCallback = null
                    false
                }
            }
        }

        web.loadUrl(ADMIN_URL)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun installTokenObserver() {
        val script = """
            (function(){
              if(window.__rmTokenObserverInstalled)return;
              const input=document.getElementById('rmAdminToken');
              const button=document.getElementById('rmAdminConnect');
              const gate=document.getElementById('rmAdminGate');
              if(!input||!button||!gate)return;
              window.__rmTokenObserverInstalled=true;
              let candidate='';
              button.addEventListener('click',function(){candidate=input.value.trim();},true);
              const check=function(){
                if(!candidate)return;
                const style=window.getComputedStyle(gate);
                if(gate.hidden||style.display==='none'||style.visibility==='hidden'){
                  try{RemontMasterNative.rememberAfterLogin(candidate);candidate='';}catch(e){}
                }
              };
              new MutationObserver(check).observe(gate,{attributes:true,attributeFilter:['style','hidden','class']});
              window.setInterval(check,400);
            })();
        """.trimIndent()
        web.evaluateJavascript(script, null)
    }

    private fun restoreLoginIfAvailable() {
        val token = readToken() ?: return
        val encoded = JSONObject.quote(token)
        val script = """
            (function(){
              const input=document.getElementById('rmAdminToken');
              const button=document.getElementById('rmAdminConnect');
              if(!input||!button||window.__rmNativeAutoLogin)return;
              window.__rmNativeAutoLogin=true;
              input.value=$encoded;
              button.click();
            })();
        """.trimIndent()
        web.evaluateJavascript(script, null)
    }

    private fun saveToken(value: String) {
        val token = value.trim()
        if (!token.startsWith("github_pat_") || token.length < 30) return
        try {
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
            getSharedPreferences("secure_admin", MODE_PRIVATE).edit()
                .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .putString("data", Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .apply()
            runOnUiThread {
                Toast.makeText(this, "Токен безопасно сохранён на устройстве", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Не удалось сохранить токен: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun readToken(): String? {
        return try {
            val prefs = getSharedPreferences("secure_admin", MODE_PRIVATE)
            val ivText = prefs.getString("iv", null) ?: return null
            val dataText = prefs.getString("data", null) ?: return null
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(128, Base64.decode(ivText, Base64.NO_WRAP))
            )
            String(cipher.doFinal(Base64.decode(dataText, Base64.NO_WRAP)), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    inner class TokenBridge {
        @JavascriptInterface
        fun rememberAfterLogin(value: String) {
            saveToken(value)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickerCode) {
            val result = if (resultCode == RESULT_OK) {
                val clip: ClipData? = data?.clipData
                when {
                    clip != null -> Array(clip.itemCount) { i -> clip.getItemAt(i).uri }
                    data?.data != null -> arrayOf(data.data!!)
                    else -> null
                }
            } else null
            fileCallback?.onReceiveValue(result)
            fileCallback = null
        }
    }

    @Deprecated("Deprecated in Android API")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::web.isInitialized) {
            web.removeJavascriptInterface("RemontMasterNative")
            web.destroy()
        }
        super.onDestroy()
    }
}
