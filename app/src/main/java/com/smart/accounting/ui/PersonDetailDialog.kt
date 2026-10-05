package com.smart.accounting.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smart.accounting.App
import com.smart.accounting.data.model.PersonEntity
import com.smart.accounting.data.model.TransactionEntity
import com.smart.accounting.databinding.DialogPersonDetailBinding
import com.smart.accounting.databinding.ItemTransactionBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PersonDetailDialog(private val person: PersonEntity) : DialogFragment() {

    private lateinit var b: DialogPersonDetailBinding
    private val repo by lazy { (requireActivity().application as App).repository }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = DialogPersonDetailBinding.inflate(LayoutInflater.from(requireContext()))
        b.tvHeaderName.text = person.name
        b.tvHeaderPhone.text = person.phone ?: "—"

        val adapter = SimpleTxAdapter()
        b.recyclerPersonTx.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerPersonTx.adapter = adapter

        lifecycleScope.launch {
            repo.byPerson(person.id).collectLatest { list ->
                adapter.submitList(list)
            }
        }
        lifecycleScope.launch {
            repo.balanceForPerson(person.id).collectLatest { bal ->
                b.tvBalance.text = "%.2f".format(bal)
            }
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("كشف حساب: ${person.name}")
            .setView(b.root)
            .setPositiveButton("إغلاق", null)
            .create()
    }

    inner class SimpleTxAdapter :
        ListAdapter<TransactionEntity, SimpleTxAdapter.VH>(
            object : DiffUtil.ItemCallback<TransactionEntity>() {
                override fun areItemsTheSame(a: TransactionEntity, b: TransactionEntity) = a.id == b.id
                override fun areContentsTheSame(a: TransactionEntity, b: TransactionEntity) = a == b
            }
        ) {
        inner class VH(val vb: ItemTransactionBinding) : RecyclerView.ViewHolder(vb.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(h: VH, position: Int) {
            val item = getItem(position)
            h.vb.tvTitle.text = item.title
            h.vb.tvDate.text = item.dateText
            h.vb.tvCurrency.text = item.currency
            val pos = item.type == "RECEIVABLE" || item.type == "IN"
            h.vb.tvAmount.text = (if (pos) "+" else "-") + "%.2f".format(item.amount)
            h.vb.tvMeta.text = item.notes ?: ""
        }
    }
}