package com.carlos.finas.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.databinding.ActivityAgendaBinding
import com.carlos.finas.databinding.ItemCompromissoBinding
import java.text.SimpleDateFormat
import java.util.*

class AgendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendaBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: CompromissoAdapter
    private var editingId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgendaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupRecyclerView()
        setupAddButton()
        binding.btnVoltar.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        val compromissos = dbHelper.getAniversarios()
        adapter = CompromissoAdapter(compromissos) { id, name, bdate ->
            editingId = id
            binding.etNome.setText(name)
            // Tentar converter data para exibição (yyyy-MM-dd ou dd/MM/yyyy para dd/MM/yyyy)
            val displayDate = parseDateForDisplay(bdate)
            binding.etData.setText(displayDate)
            binding.btnAdicionar.text = "Salvar Alteração"
        }
        binding.rvCompromissos.layoutManager = LinearLayoutManager(this)
        binding.rvCompromissos.adapter = adapter

        // Exibir mensagem se a lista estiver vazia
        if (compromissos.isEmpty()) {
            Toast.makeText(this, "Nenhum compromisso encontrado!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupAddButton() {
        binding.btnAdicionar.setOnClickListener {
            val name = binding.etNome.text.toString().trim()
            val bdate = binding.etData.text.toString().trim()
            if (name.isEmpty() || bdate.isEmpty()) {
                Toast.makeText(this, "Preencha nome e data do compromisso!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar formato da data (dd/MM/yyyy)
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            inputFormat.isLenient = false
            try {
                inputFormat.parse(bdate) ?: throw IllegalArgumentException("Data inválida")
            } catch (e: Exception) {
                Toast.makeText(this, "Formato de data inválido! Use dd/MM/yyyy", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (editingId == null) {
                // Adicionar novo compromisso
                if (dbHelper.addAniversario(name, bdate)) {
                    adapter.updateList(dbHelper.getAniversarios())
                    binding.etNome.text.clear()
                    binding.etData.text.clear()
                    Toast.makeText(this, "Compromisso adicionado!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao adicionar compromisso!", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Atualizar compromisso existente
                if (dbHelper.updateAniversario(editingId!!, name, bdate)) {
                    adapter.updateList(dbHelper.getAniversarios())
                    binding.etNome.text.clear()
                    binding.etData.text.clear()
                    binding.btnAdicionar.text = "Adicionar"
                    editingId = null
                    Toast.makeText(this, "Compromisso atualizado!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erro ao atualizar compromisso!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.etData.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.etData.setText(dateFormat.format(calendar.time))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun parseDateForDisplay(bdate: String): String {
        // Tentar parsear como yyyy-MM-dd ou dd/MM/yyyy
        val possibleFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        )
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        for (format in possibleFormats) {
            format.isLenient = false
            try {
                val date = format.parse(bdate)
                if (date != null) {
                    return outputFormat.format(date)
                }
            } catch (e: Exception) {
                // Continuar para o próximo formato
            }
        }
        return bdate // Retornar original se não puder parsear
    }
}

class CompromissoAdapter(
    private var compromissos: List<Triple<Int, String, String>>,
    private val onEditClick: (Int, String, String) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<CompromissoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCompromissoBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCompromissoBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, name, bdate) = compromissos[position]
        holder.binding.tvNome.text = name
        // Exibir apenas dia e mês (dd/MM)
        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        )
        val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        var displayDate = bdate
        for (format in inputFormats) {
            format.isLenient = false
            try {
                val date = format.parse(bdate)
                if (date != null) {
                    displayDate = outputFormat.format(date)
                    break
                }
            } catch (e: Exception) {
                // Continuar para o próximo formato
            }
        }
        holder.binding.tvData.text = displayDate

        // Botão Editar
        holder.binding.btnEditar.setOnClickListener {
            onEditClick(id, name, bdate)
        }

        // Botão Excluir
        holder.binding.btnExcluir.setOnClickListener {
            val dbHelper = DatabaseHelper(holder.binding.root.context)
            if (dbHelper.deleteAniversario(id)) {
                updateList(dbHelper.getAniversarios())
                Toast.makeText(holder.binding.root.context, "Compromisso excluído!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(holder.binding.root.context, "Erro ao excluir compromisso!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = compromissos.size

    fun updateList(newList: List<Triple<Int, String, String>>) {
        compromissos = newList
        notifyDataSetChanged()
    }
}