package com.carlos.indextracker.activities

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.indextracker.R
import com.carlos.indextracker.database.IndexTrackerDbHelper
import com.carlos.indextracker.database.Indice
import com.carlos.indextracker.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: IndexTrackerDbHelper
    private var adapter: IndiceAdapter? = null
    private var scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().penaltyLog().build())
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = IndexTrackerDbHelper(this)
        adapter = IndiceAdapter(onEditClick = { indice -> showForm(indice) }, onDeleteClick = { deleteIndice(it) })
        binding.recyclerIndices.layoutManager = LinearLayoutManager(this)
        binding.recyclerIndices.adapter = adapter

        binding.btnRefresh.setOnClickListener { refreshData() }
        binding.btnAdd.setOnClickListener { showForm() }
        binding.btnArchive.setOnClickListener { archiveOld() }

        initData()
    }

    private fun initData() {
        val indices = db.listIndices()
        adapter?.submitList(indices)
        binding.tvEmpty.visibility = if (indices.isEmpty()) View.VISIBLE else View.GONE
        val hoje = LocalDate.now().toString()
        binding.tvLastUpdate.text = getString(R.string.ultima_atualizacao, hoje)
    }

    private fun showForm(indice: Indice? = null) {
        val intent = Intent(this, IndexFormActivity::class.java)
        if (indice != null) {
            intent.putExtra("indice_json", Gson().toJson(indice))
        }
        startActivityForResult(intent, 1)
    }

    private fun deleteIndice(id: Long) {
        db.deleteIndice(id)
        initData()
    }

    private fun archiveOld() {
        val removed = db.arquivarAntigos(1)
        Toast.makeText(this, "Arquivados $removed registros", Toast.LENGTH_SHORT).show()
    }

    private fun refreshData() {
        scope.launch {
            val indices = db.listIndices(onlyActive = true)
            if (indices.isEmpty()) return@launch
            val hoje = LocalDate.now().toString()
            val tx = db.writableDatabase
            try {
                tx.beginTransaction()
                indices.forEach { indice ->
                    val taxa = when (indice.fonte) {
                        "exchangerate" -> fetchExchangerate(indice.ativo)
                        "coingecko" -> fetchCoingecko()
                        "gold" -> fetchGoldApi()
                        "yahoo" -> fetchYahooFinance(indice.ativo)
                        else -> 0.0
                    }
                    db.upsertCotacao(indice.id, hoje, taxa)
                }
                tx.setTransactionSuccessful()
            } finally {
                tx.endTransaction()
            }
            initData()
            binding.tvLastUpdate.text = getString(R.string.ultima_atualizacao, hoje)
            Toast.makeText(this@MainActivity, "Atualizado", Toast.LENGTH_SHORT).show()
        }
    }

    // HTTP helper using stdlib HttpURLConnection
    private fun httpGet(urlStr: String): String? {
        return try {
            val url = URL(urlStr)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15000
                readTimeout = 15000
                setRequestProperty("User-Agent", "IndexTracker/1.0")
            }
            if (conn.responseCode != 200) return null
            conn.inputStream.bufferedReader().use { it.readText() }
        } catch (_: Exception) { null }
    }

    private fun fetchExchangerate(symbol: String): Double {
        val json = httpGet("https://api.exchangerate-api.com/v4/latest/USD") ?: return 0.0
        val rates = try { JsonParser.parseString(json).asJsonObject.getAsJsonObject("rates") } catch (_: Exception) { return 0.0 }
        return rates.get(symbol)?.asDouble ?: 0.0
    }

    private fun fetchCoingecko(): Double {
        val json = httpGet("https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=usd") ?: return 0.0
        return try {
            JsonParser.parseString(json).asJsonObject
                .getAsJsonObject("bitcoin").get("usd").asDouble
        } catch (_: Exception) { 0.0 }
    }

    private fun fetchGoldApi(): Double {
        return try {
            val json = httpGet("https://api.gold-api.com/price/XAU") ?: return 1950.0
            JsonParser.parseString(json).asJsonObject.get("price").asDouble
        } catch (_: Exception) { 1950.0 }
    }

    private fun fetchYahooFinance(symbol: String): Double {
        return try {
            val json = httpGet("https://query1.finance.yahoo.com/v7/finance/quote?symbols=$symbol") ?: return 0.0
            JsonParser.parseString(json).asJsonObject
                .getAsJsonObject("quoteResponse").asJsonArray[0]
                .asJsonObject.get("regularMarketPrice").asDouble
        } catch (_: Exception) { 0.0 }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_archive -> { archiveOld(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) initData()
    }
}