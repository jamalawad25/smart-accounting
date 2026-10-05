package com.smart.accounting.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smart.accounting.R
import com.smart.accounting.data.model.AccountEntity
import com.smart.accounting.data.repo.TransactionRepository
import com.smart.accounting.databinding.ItemAccountBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountAdapter(
    private val repo: TransactionRepository,
    private val onDelete: (AccountEntity) -> Unit,
    private val onOpen: (AccountEntity) -> Unit
) : ListAdapter<AccountEntity, AccountAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AccountEntity>() {
            override fun areItemsTheSame(a: AccountEntity, b: AccountEntity) = a.id == b.id
            override fun areContentsTheSame(a: AccountEntity, b: AccountEntity) = a == b
        }
    }

    inner class VH(val b: ItemAccountBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val account = getItem(position)
        val ctx = h.b.root.context

        h.b.tvAccountName.text = account.name
        h.b.tvAccountCategory.text = account.category
        h.b.tvInitial.text = account.name.firstOrNull()?.toString() ?: "؟"

        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            repo.balanceForAccount(account.id).collectLatest { balance ->
                val label = when {
                    balance > 0 -> "لنا"
                    balance < 0 -> "علينا"
                    else -> "متعادل"
                }
                h.b.tvAccountBalance.text = "%.2f $label".format(kotlin.math.abs(balance))
                h.b.tvAccountBalance.setTextColor(
                    ContextCompat.getColor(
                        ctx,
                        when {
                            balance > 0 -> R.color.emerald_500
                            balance < 0 -> R.color.crimson_500
                            else -> R.color.gray_500
                        }
                    )
                )
            }
        }

        h.b.root.setOnClickListener { onOpen(account) }
        h.b.root.setOnLongClickListener { onDelete(account); true }
    }
}