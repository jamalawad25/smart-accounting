package com.smart.accounting.ui

import android.app.Dialog
import android.graphics.Color
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
import com.smart.accounting.data.model.AccountEntity
import com.smart.accounting.data.model.TransactionEntity
import com.smart.accounting.databinding.DialogAccountStatementBinding
import com.smart.accounting.databinding.ItemStatementRowBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountStatementDialog(private val account: AccountEntity) : DialogFragment() {

    private lateinit var b: DialogAccountStatementBinding
    private val repo by lazy { (requireActivity().application as App).transactionRepo }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = DialogAccountStatementBinding.inflate(LayoutInflater.from(requireContext()))
        b.tvHeaderName.text = account.name
        b.tvHeaderPhone.text = account.phone ?: "—"
        b.tvHeaderInitial.text = account.name.firstOrNull()?.toString() ?: "؟"

        val adapter = StatementAdapter()
        b.recyclerPersonTx.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerPersonTx.adapter = adapter

        lifecycleScope.launch {
            repo.byAccount(account.id).collectLatest { adapter.submitList(it) }
        }
        lifecycleScope.launch {
            repo.balanceForAccount(account.id).collectLatest { bal ->
                val formatted = "%.2f".format(kotlin.math.abs(bal))
                when {
                    bal > 0 -> {
                        b.tvBalance.text = "$formatted (لنا)"
                        b.tvBalance.setTextColor(Color.parseColor("#10B981"))
                    }
                    bal < 0 -> {
                        b.tvBalance.text = "$formatted (علينا)"
                        b.tvBalance.setTextColor(Color.parseColor("#EF4444"))
                    }
                    else -> {
                        b.tvBalance.text = "0.00"
                        b.tvBalance.setTextColor(Color.parseColor("#111827"))
                    }
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("كشف حساب: ${account.name}")
            .setView(b.root)
            .setPositiveButton("إغلاق", null)
            .create()
    }

    inner class StatementAdapter :
        ListAdapter<TransactionEntity, StatementAdapter.VH>(
            object : DiffUtil.ItemCallback<TransactionEntity>() {
                override fun areItemsTheSame(a: TransactionEntity, b: TransactionEntity) = a.id == b.id
                override fun areContentsTheSame(a: TransactionEntity, b: TransactionEntity) = a == b
            }
        ) {
        inner class VH(val vb: ItemStatementRowBinding) : RecyclerView.ViewHolder(vb.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            ItemStatementRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(h: VH, position: Int) {
            val item = getItem(position)
            h.vb.tvTitle.text = item.title
            h.vb.tvDate.text = item.dateText

            if (item.type == "DEBIT") {
                h.vb.tvDebit.text = "%.2f".format(item.amount)
                h.vb.tvCredit.text = ""
            } else {
                h.vb.tvDebit.text = ""
                h.vb.tvCredit.text = "%.2f".format(item.amount)
            }

            var running = 0.0
            for (i in 0..position) {
                val tx = getItem(i)
                running += if (tx.type == "CREDIT") tx.amount else -tx.amount
            }
            h.vb.tvBalance.text = "%.2f".format(running)
            h.vb.tvBalance.setTextColor(
                if (running >= 0) Color.parseColor("#10B981") else Color.parseColor("#EF4444")
            )
        }
    }
}