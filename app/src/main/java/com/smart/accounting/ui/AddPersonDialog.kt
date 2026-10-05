package com.smart.accounting.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.smart.accounting.data.model.PersonEntity
import com.smart.accounting.databinding.DialogAddPersonBinding

class AddPersonDialog(
    private val onSave: (PersonEntity) -> Unit
) : DialogFragment() {

    private lateinit var b: DialogAddPersonBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = DialogAddPersonBinding.inflate(LayoutInflater.from(requireContext()))

        return AlertDialog.Builder(requireContext())
            .setTitle("إضافة حساب جديد")
            .setView(b.root)
            .setPositiveButton("حفظ", null)
            .setNegativeButton("إلغاء", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = b.etPersonName.text?.toString()?.trim().orEmpty()
                        if (name.isEmpty()) {
                            b.etPersonName.error = "الاسم مطلوب"
                            return@setOnClickListener
                        }
                        val p = PersonEntity(
                            name = name,
                            phone = b.etPersonPhone.text?.toString()?.takeIf { it.isNotBlank() },
                            notes = b.etPersonNotes.text?.toString()?.takeIf { it.isNotBlank() }
                        )
                        onSave(p)
                        dismiss()
                    }
                }
            }
    }
}