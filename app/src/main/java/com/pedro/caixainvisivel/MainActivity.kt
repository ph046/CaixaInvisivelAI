package com.pedro.caixainvisivel

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class MainActivity : Activity() {

    private val verde = Color.parseColor("#00C853")
    private val verdeEscuro = Color.parseColor("#009B3A")
    private val preto = Color.parseColor("#1F1F1F")
    private val cinza = Color.parseColor("#606060")
    private val fundo = Color.parseColor("#F7F7F7")
    private val vermelho = Color.parseColor("#C62828")

    private lateinit var root: LinearLayout
    private lateinit var resumoText: TextView
    private lateinit var listaText: TextView

    private val prefs by lazy {
        getSharedPreferences("caixa_invisivel_data", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(fundo)
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(24))
        }

        scroll.addView(root)

        montarTela()
        atualizarResumo()
        setContentView(scroll)
    }

    private fun montarTela() {
        root.removeAllViews()

        root.addView(criarTopo())
        root.addView(espaco(12))
        root.addView(criarCardResumo())
        root.addView(espaco(12))
        root.addView(criarAcoesRapidas())
        root.addView(espaco(12))
        root.addView(criarCardLista())
        root.addView(espaco(12))
        root.addView(criarCardComoUsar())
    }

    private fun criarTopo(): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(verde, verdeEscuro)
            ).apply {
                cornerRadius = dp(26).toFloat()
            }
        }

        val badge = TextView(this).apply {
            text = "APP PARA PEQUENOS VENDEDORES"
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setTextColor(preto)
            setPadding(dp(12), dp(7), dp(12), dp(7))
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat()
                setColor(Color.WHITE)
            }
        }

        val title = TextView(this).apply {
            text = "Caixa Invisível AI"
            textSize = 27f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, dp(14), 0, dp(4))
        }

        val desc = TextView(this).apply {
            text = "Controle vendas, gastos, lucro e clientes devendo sem planilha."
            textSize = 14f
            setTextColor(Color.WHITE)
            setLineSpacing(dp(2).toFloat(), 1.0f)
        }

        val frase = TextView(this).apply {
            text = "Você sabe quanto lucrou de verdade hoje?"
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            setPadding(0, dp(14), 0, 0)
        }

        card.addView(badge)
        card.addView(title)
        card.addView(desc)
        card.addView(frase)

        return card
    }

    private fun criarCardResumo(): View {
        val card = criarCardBase()

        val titulo = criarTitulo("Resumo de hoje")

        resumoText = TextView(this).apply {
            textSize = 15f
            setTextColor(preto)
            setPadding(0, dp(10), 0, 0)
            setLineSpacing(dp(4).toFloat(), 1.0f)
        }

        card.addView(titulo)
        card.addView(resumoText)

        return card
    }

    private fun criarAcoesRapidas(): View {
        val card = criarCardBase()

        val titulo = criarTitulo("Registrar rápido")

        val desc = TextView(this).apply {
            text = "Anote suas vendas, gastos e fiados em poucos segundos."
            textSize = 13f
            setTextColor(cinza)
            setPadding(0, dp(4), 0, dp(10))
        }

        val btnVenda = criarBotaoPrincipal("Registrar venda")
        btnVenda.setOnClickListener { dialogTransacao("sale") }

        val btnGasto = criarBotaoSecundario("Registrar gasto")
        btnGasto.setOnClickListener { dialogTransacao("expense") }

        val btnFiado = criarBotaoSecundario("Cliente me deve")
        btnFiado.setOnClickListener { dialogFiado() }

        val btnFrase = criarBotaoSecundario("Anotar por frase")
        btnFrase.setOnClickListener { dialogFrase() }

        val linha1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val linha2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }

        linha1.addView(btnVenda, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = dp(6)
        })
        linha1.addView(btnGasto, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        linha2.addView(btnFiado, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginEnd = dp(6)
        })
        linha2.addView(btnFrase, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        card.addView(titulo)
        card.addView(desc)
        card.addView(linha1)
        card.addView(linha2)

        return card
    }

    private fun criarCardLista(): View {
        val card = criarCardBase()

        val topo = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titulo = criarTitulo("Últimos registros")

        val limpar = TextView(this).apply {
            text = "Limpar"
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(vermelho)
            gravity = Gravity.END
            setOnClickListener { confirmarLimpar() }
        }

        topo.addView(titulo, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        topo.addView(limpar)

        listaText = TextView(this).apply {
            textSize = 13f
            setTextColor(preto)
            setPadding(0, dp(10), 0, 0)
            setLineSpacing(dp(4).toFloat(), 1.0f)
        }

        card.addView(topo)
        card.addView(listaText)

        return card
    }

    private fun criarCardComoUsar(): View {
        val card = criarCardBase()

        val titulo = criarTitulo("Como vender esse app")

        val texto = TextView(this).apply {
            text = """
            Promessa:
            “Controle suas vendas sem planilha.”

            Público inicial:
            confeiteiras, marmiteiras, manicures, barbeiros, sacoleiras e vendedores pequenos.

            Demonstração:
            1. Registre uma venda.
            2. Registre um gasto.
            3. Cadastre um cliente devendo.
            4. Mostre o lucro do dia.
            5. Gere a cobrança pronta para WhatsApp.
            """.trimIndent()
            textSize = 13f
            setTextColor(cinza)
            setLineSpacing(dp(3).toFloat(), 1.0f)
            setPadding(0, dp(8), 0, 0)
        }

        card.addView(titulo)
        card.addView(texto)

        return card
    }

    private fun dialogTransacao(type: String) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), 0)
        }

        val descricao = criarInput("Descrição. Ex: bolo de pote, ingrediente, entrega")
        val valor = criarInput("Valor total. Ex: 35,50")
        valor.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        box.addView(descricao)
        box.addView(espaco(8))
        box.addView(valor)

        val titulo = if (type == "sale") "Registrar venda" else "Registrar gasto"

        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setView(box)
            .setPositiveButton("Salvar") { _, _ ->
                val desc = descricao.text.toString().trim()
                val amount = parseMoney(valor.text.toString())

                if (desc.isBlank() || amount <= 0.0) {
                    Toast.makeText(this, "Preencha descrição e valor.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                salvarTransacao(type, desc, amount)
                atualizarResumo()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun dialogFiado() {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(8), dp(18), 0)
        }

        val cliente = criarInput("Nome do cliente")
        val descricao = criarInput("Motivo. Ex: 2 marmitas")
        val valor = criarInput("Valor devido. Ex: 25")
        valor.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        box.addView(cliente)
        box.addView(espaco(8))
        box.addView(descricao)
        box.addView(espaco(8))
        box.addView(valor)

        AlertDialog.Builder(this)
            .setTitle("Cliente me deve")
            .setView(box)
            .setPositiveButton("Salvar") { _, _ ->
                val nome = cliente.text.toString().trim()
                val desc = descricao.text.toString().trim()
                val amount = parseMoney(valor.text.toString())

                if (nome.isBlank() || amount <= 0.0) {
                    Toast.makeText(this, "Preencha nome e valor.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                salvarFiado(nome, desc, amount)
                atualizarResumo()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun dialogFrase() {
        val input = criarInput("Ex: vendi 3 bolos por 12 reais cada")
        input.setSingleLine(false)
        input.minLines = 3

        AlertDialog.Builder(this)
            .setTitle("Anotar por frase")
            .setMessage("Versão inicial: o app tenta identificar venda ou gasto pelo texto.")
            .setView(input)
            .setPositiveButton("Salvar") { _, _ ->
                val frase = input.text.toString().trim()
                if (frase.isBlank()) return@setPositiveButton

                interpretarFraseSimples(frase)
                atualizarResumo()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun interpretarFraseSimples(frase: String) {
        val texto = frase.lowercase(Locale.ROOT)

        val numeros = Regex("""\d+[,.]?\d*""")
            .findAll(texto)
            .map { parseMoney(it.value) }
            .filter { it > 0.0 }
            .toList()

        val valor = when {
            numeros.size >= 2 && (texto.contains("cada") || texto.contains("unidade")) -> numeros[0] * numeros[1]
            numeros.isNotEmpty() -> numeros.last()
            else -> 0.0
        }

        if (valor <= 0.0) {
            Toast.makeText(this, "Não consegui identificar o valor. Registre manualmente.", Toast.LENGTH_LONG).show()
            return
        }

        val tipo = if (
            texto.contains("gastei") ||
            texto.contains("comprei") ||
            texto.contains("paguei") ||
            texto.contains("custo")
        ) {
            "expense"
        } else {
            "sale"
        }

        salvarTransacao(tipo, frase, valor)
    }

    private fun salvarTransacao(type: String, descricao: String, amount: Double) {
        val arr = getTransactions()

        val obj = JSONObject().apply {
            put("type", type)
            put("description", descricao)
            put("amount", amount)
            put("created_at", System.currentTimeMillis())
        }

        arr.put(obj)
        prefs.edit().putString("transactions", arr.toString()).apply()

        Toast.makeText(this, "Registro salvo.", Toast.LENGTH_SHORT).show()
    }

    private fun salvarFiado(cliente: String, descricao: String, amount: Double) {
        val arr = getDebts()

        val obj = JSONObject().apply {
            put("customer", cliente)
            put("description", descricao)
            put("amount", amount)
            put("paid", false)
            put("created_at", System.currentTimeMillis())
        }

        arr.put(obj)
        prefs.edit().putString("debts", arr.toString()).apply()

        val mensagem = "Oi $cliente, tudo bem? Passando só para lembrar do valor de ${money(amount)} referente a ${if (descricao.isBlank()) "sua compra" else descricao}. Pode me mandar no Pix quando conseguir?"

        AlertDialog.Builder(this)
            .setTitle("Fiado salvo")
            .setMessage("Quer copiar/enviar a cobrança agora?")
            .setPositiveButton("Enviar cobrança") { _, _ ->
                compartilharTexto(mensagem)
            }
            .setNegativeButton("Agora não", null)
            .show()
    }

    private fun atualizarResumo() {
        val hoje = hojeString()

        var vendasHoje = 0.0
        var gastosHoje = 0.0

        val trans = getTransactions()
        for (i in 0 until trans.length()) {
            val obj = trans.optJSONObject(i) ?: continue
            val data = dataString(obj.optLong("created_at", 0L))
            if (data == hoje) {
                if (obj.optString("type") == "sale") {
                    vendasHoje += obj.optDouble("amount", 0.0)
                } else {
                    gastosHoje += obj.optDouble("amount", 0.0)
                }
            }
        }

        var fiadoAberto = 0.0
        val debts = getDebts()
        for (i in 0 until debts.length()) {
            val obj = debts.optJSONObject(i) ?: continue
            if (!obj.optBoolean("paid", false)) {
                fiadoAberto += obj.optDouble("amount", 0.0)
            }
        }

        val lucro = vendasHoje - gastosHoje

        resumoText.text = buildString {
            append("Vendeu hoje: ${money(vendasHoje)}\n")
            append("Gastou hoje: ${money(gastosHoje)}\n")
            append("Lucro estimado: ${money(lucro)}\n")
            append("Clientes devendo: ${money(fiadoAberto)}")
        }

        montarLista()
    }

    private fun montarLista() {
        val linhas = mutableListOf<String>()

        val trans = getTransactions()
        for (i in max(0, trans.length() - 6) until trans.length()) {
            val obj = trans.optJSONObject(i) ?: continue
            val tipo = if (obj.optString("type") == "sale") "Venda" else "Gasto"
            linhas.add("• $tipo: ${obj.optString("description")} — ${money(obj.optDouble("amount", 0.0))}")
        }

        val debts = getDebts()
        for (i in max(0, debts.length() - 4) until debts.length()) {
            val obj = debts.optJSONObject(i) ?: continue
            if (!obj.optBoolean("paid", false)) {
                linhas.add("• Fiado: ${obj.optString("customer")} deve ${money(obj.optDouble("amount", 0.0))}")
            }
        }

        listaText.text = if (linhas.isEmpty()) {
            "Nenhum registro ainda. Registre sua primeira venda."
        } else {
            linhas.reversed().joinToString("\n")
        }
    }

    private fun confirmarLimpar() {
        AlertDialog.Builder(this)
            .setTitle("Limpar dados")
            .setMessage("Isso vai apagar vendas, gastos e fiados salvos neste celular.")
            .setPositiveButton("Apagar") { _, _ ->
                prefs.edit().clear().apply()
                atualizarResumo()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun compartilharTexto(texto: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, texto)
        }

        startActivity(Intent.createChooser(intent, "Enviar cobrança"))
    }

    private fun getTransactions(): JSONArray {
        return try {
            JSONArray(prefs.getString("transactions", "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun getDebts(): JSONArray {
        return try {
            JSONArray(prefs.getString("debts", "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun criarInput(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 14f
            setTextColor(preto)
            setHintTextColor(Color.parseColor("#999999"))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(14).toFloat()
                setColor(Color.parseColor("#F7F7F7"))
                setStroke(dp(1), Color.parseColor("#DDDDDD"))
            }
        }
    }

    private fun criarCardBase(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = GradientDrawable().apply {
                cornerRadius = dp(20).toFloat()
                setColor(Color.WHITE)
                setStroke(dp(1), Color.parseColor("#E7E7E7"))
            }
        }
    }

    private fun criarTitulo(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(preto)
        }
    }

    private fun criarBotaoPrincipal(texto: String): Button {
        return Button(this).apply {
            text = texto
            textSize = 14f
            isAllCaps = false
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(verde, verdeEscuro)
            ).apply {
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(10), dp(11), dp(10), dp(11))
        }
    }

    private fun criarBotaoSecundario(texto: String): Button {
        return Button(this).apply {
            text = texto
            textSize = 14f
            isAllCaps = false
            setTypeface(null, Typeface.BOLD)
            setTextColor(preto)
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.parseColor("#E9F8EF"))
                setStroke(dp(1), verde)
            }
            setPadding(dp(10), dp(11), dp(10), dp(11))
        }
    }

    private fun parseMoney(text: String): Double {
        return text
            .replace("R$", "", ignoreCase = true)
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
            .toDoubleOrNull() ?: 0.0
    }

    private fun money(value: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
    }

    private fun hojeString(): String {
        return dataString(System.currentTimeMillis())
    }

    private fun dataString(time: Long): String {
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(time))
        } catch (_: Exception) {
            ""
        }
    }

    private fun espaco(valorDp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(valorDp)
            )
        }
    }

    private fun dp(valor: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            valor.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
