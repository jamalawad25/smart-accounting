package com.smart.accounting.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.smart.accounting.R
import com.smart.accounting.data.model.PersonEntity
import com.smart.accounting.data.repo.TransactionRepository
import com.smart.accounting.databinding.ItemPersonBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PersonAdapter(
    private val repo: TransactionRepository,
    private val onDelete: (PersonEntity) -> Unit,
    private val onOpen: (PersonEntity) -> Unit
) : ListAdapter<PersonEntity, PersonAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PersonEntity>() {
            override fun areItemsTheSame(a: PersonEntity, b: PersonEntity) = a.id == b.id
            override fun areContentsTheSame(a: PersonEntity, b: PersonEntity) = a == b
        }
    }

    inner class VH(val b: ItemPersonBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val person = getItem(position)
        val ctx = h.b.root.context

        h.b.tvPersonName.text = person.name
        h.b.tvPersonPhone.text = person.phone ?: "—"

        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            repo.balanceForPerson(person.id).collectLatest { balance ->
                h.b.tvPersonBalance.text = "%.2f".format(balance)
                h.b.tvPersonBalance.setTextColor(
                    ContextCompat.getColor(
                        ctx,
                        if (balance >= 0) R.color.emerald_500 else R.color.crimson_500
                    )
                )
            }
        }

        h.b.root.setOnClickListener { onOpen(person) }
        h.b.root.setOnLongClickListener { onDelete(person); true }
    }
}