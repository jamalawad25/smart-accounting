package com.smart.accounting.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.smart.accounting.data.model.AccountEntity
import com.smart.accounting.databinding.DialogAddAccountBinding

class AddEditAccountDialog(
    private val onSave: (AccountEntity) -> Unit
) : DialogFragment() {

    private lateinit var b: DialogAddAccountBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = DialogAddAccountBinding.inflate(LayoutInflater.from(requireContext()))

        return AlertDialog.Builder(requireContext())
            .setTitle("إضافة حساب جديد")
            .setView(b.root)
            .setPositiveButton("حفظ", null)
            .setNegativeButton("إلغاء", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = b.etName.text?.toString()?.trim().orEmpty()
                        if (name.isEmpty()) { b.etName.error = "الاسم مطلوب"; return@setOnClickListener }
                        val acc = AccountEntity(
                            name = name,
                            phone = b.etPhone.text?.toString()?.takeIf { it.isNotBlank() },
                            email = b.etEmail.text?.toString()?.takeIf { it.isNotBlank() },
                            category = b.etCategory.text?.toString()?.trim()?.takeIf { it.isNotBlank() } ?: "عميل",
                            initialBalance = b.etInitialBalance.text?.toString()?.toDoubleOrNull() ?: 0.0,
                            notes = b.etNotes.text?.toString()?.takeIf { it.isNotBlank() }
                        )
                        onSave(acc)
                        dismiss()
                    }
                }
            }
    }
}