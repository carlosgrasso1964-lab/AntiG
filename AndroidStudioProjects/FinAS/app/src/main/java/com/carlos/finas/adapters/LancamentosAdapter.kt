package com.carlos.finas.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.models.Lancamento
import com.carlos.finas.databinding.ItemLancamentoBinding
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

class LancamentosAdapter private constructor(
    private val lancamentos: MutableList<Lancamento>,
    private val onItemClick: (Lancamento) -> Unit
) : RecyclerView.Adapter<LancamentosAdapter.ViewHolder>() {

    companion object {
        fun create(lancamentos: List<Lancamento>, onItemClick: (Lancamento) -> Unit): LancamentosAdapter {
            return LancamentosAdapter(lancamentos.toMutableList(), onItemClick)
        }
    }

    class ViewHolder(val binding: ItemLancamentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLancamentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lancamento = lancamentos[position]
        holder.binding.apply {
            txtDescricao.text = lancamento.Descr
            val decimalFormat = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
            txtValor.text = decimalFormat.format(lancamento.Valor)
            txtData.text = formatDateToDisplay(lancamento.dtVcto)
            if (lancamento.saldoAcumulado != null) {
                txtSaldo.visibility = View.VISIBLE
                txtSaldo.text = "Saldo: ${decimalFormat.format(lancamento.saldoAcumulado)}"
            } else {
                txtSaldo.visibility = View.GONE
            }
            Log.d("LancamentosAdapter", "Binding: idMov=${lancamento.idMov}, clifor=${lancamento.clifor}, recurso=${lancamento.recurso}, valor=${lancamento.Valor}, saldoAcumulado=${lancamento.saldoAcumulado}")
            root.setOnClickListener { onItemClick(lancamento) }
        }
    }

    override fun getItemCount(): Int = lancamentos.size

    fun updateData(newLancamentos: List<Lancamento>, saldoInicial: Double) {
        val oldLancamentos = lancamentos.toList()
        lancamentos.clear()
        var saldo = saldoInicial
        val updatedLancamentos = newLancamentos.map {
            saldo += it.Valor
            it.copy(saldoAcumulado = saldo)
        }
        lancamentos.addAll(updatedLancamentos)

        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldLancamentos.size
            override fun getNewListSize(): Int = updatedLancamentos.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldLancamentos[oldItemPosition].idMov == updatedLancamentos[newItemPosition].idMov
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldLancamentos[oldItemPosition] == updatedLancamentos[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        Log.d("LancamentosAdapter", "updateData: saldoInicial=$saldoInicial, lancamentos=${updatedLancamentos.size}, lastSaldo=${updatedLancamentos.lastOrNull()?.saldoAcumulado}")
    }

    fun addData(newLancamentos: List<Lancamento>, saldoInicial: Double) {
        val oldLancamentos = lancamentos.toList()
        val lastSaldo = if (lancamentos.isNotEmpty() && lancamentos.last().saldoAcumulado != null) {
            lancamentos.last().saldoAcumulado!!
        } else {
            saldoInicial
        }
        var saldo = lastSaldo
        val updatedLancamentos = newLancamentos.map {
            saldo += it.Valor
            it.copy(saldoAcumulado = saldo)
        }
        lancamentos.addAll(updatedLancamentos)

        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldLancamentos.size
            override fun getNewListSize(): Int = oldLancamentos.size + updatedLancamentos.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return if (newItemPosition < oldLancamentos.size) {
                    oldLancamentos[oldItemPosition].idMov == oldLancamentos[newItemPosition].idMov
                } else {
                    oldLancamentos[oldItemPosition].idMov == updatedLancamentos[newItemPosition - oldLancamentos.size].idMov
                }
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return if (newItemPosition < oldLancamentos.size) {
                    oldLancamentos[oldItemPosition] == oldLancamentos[newItemPosition]
                } else {
                    oldLancamentos[oldItemPosition] == updatedLancamentos[newItemPosition - oldLancamentos.size]
                }
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        Log.d("LancamentosAdapter", "addData: saldoInicial=$saldoInicial, lastSaldo=$lastSaldo, newLancamentos=${newLancamentos.size}, lastNewSaldo=${updatedLancamentos.lastOrNull()?.saldoAcumulado}")
    }

    fun getLancamentos(): List<Lancamento> = lancamentos.toList()

    private fun formatDateToDisplay(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            displayFormat.format(dbFormat.parse(dateStr)!!)
        } catch (e: Exception) {
            dateStr
        }
    }
}