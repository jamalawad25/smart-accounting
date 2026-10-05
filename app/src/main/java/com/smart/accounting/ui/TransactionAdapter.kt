package com.smart.accounting.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.smart.accounting.R
import com.smart.accounting.data.model.TransactionEntity
import com.smart.accounting.databinding.ItemTransactionBinding

class TransactionAdapter(
    private val onImage: (String) -> Unit,
    private val onDelete: (TransactionEntity) -> Unit,
    private val onEdit: (TransactionEntity) -> Unit
) : ListAdapter<TransactionEntity, TransactionAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TransactionEntity>() {
            override fun areItemsTheSame(a: TransactionEntity, b: TransactionEntity) = a.id == b.id
            override fun areContentsTheSame(a: TransactionEntity, b: TransactionEntity) = a == b
        }
    }

    inner class VH(val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = getItem(position)
        val ctx = h.b.root.context

        h.b.tvTitle.text = item.title
        h.b.tvDate.text = item.dateText
        h.b.tvCurrency.text = when (item.currency) {
            "YER" -> "ر.ي"
            "SAR" -> "ر.س"
            else -> "$"
        }

        val isPositive = item.type == "IN" || item.type == "RECEIVABLE"
        val sign = if (isPositive) "+" else "-"
        h.b.tvAmount.text = "$sign${"%.2f".format(item.amount)}"
        h.b.tvAmount.setTextColor(
            ContextCompat.getColor(
                ctx,
                if (isPositive) R.color.emerald_500 else R.color.crimson_500
            )
        )

        val meta = buildString {
            append(when (item.section) {
                "CASH" -> "معاملة يومية"
                "DEBT" -> "دين"
                else -> "مصروف"
            })
            append(" • ")
            append(when (item.paymentMethod) {
                "CASH" -> "نقداً"
                "CREDIT" -> "آجل"
                else -> "محفظة: ${item.walletName ?: "-"}"
            })
            item.personName?.let { append(" • $it") }
            item.category?.let { append(" • $it") }
        }
        h.b.tvMeta.text = meta

        if (!item.imagePath.isNullOrBlank()) {
            h.b.imgThumb.load(item.imagePath) { crossfade(true) }
            h.b.imgThumb.setOnClickListener { onImage(item.imagePath!!) }
        } else {
            h.b.imgThumb.setImageResource(android.R.drawable.ic_menu_gallery)
            h.b.imgThumb.setOnClickListener(null)
        }

        h.b.root.setOnClickListener { onEdit(item) }
        h.b.root.setOnLongClickListener { onDelete(item); true }
    }
}