package com.smart.accounting.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import com.smart.accounting.R
import com.smart.accounting.data.model.AccountEntity
import com.smart.accounting.data.model.TransactionEntity
import com.smart.accounting.databinding.DialogAddTransactionBinding
import com.smart.accounting.util.DateUtils
import com.smart.accounting.util.ImageStorage
import java.io.File
import java.util.Calendar

class AddEditTransactionDialog(
    private val account: AccountEntity? = null,
    private val existing: TransactionEntity? = null,
    private val onSave: (TransactionEntity) -> Unit
) : DialogFragment() {

    private lateinit var b: DialogAddTransactionBinding
    private var selectedType = "CREDIT"
    private var imagePath: String? = null
    private val cal: Calendar = Calendar.getInstance()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        val saved = ImageStorage.copyFromUri(requireContext(), uri)
        if (saved != null) { imagePath = saved; b.imgPreview.setImageURI(Uri.fromFile(File(saved))) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = DialogAddTransactionBinding.inflate(LayoutInflater.from(requireContext()))
        setupTypeChips()
        prefill()
        setupDate()
        b.btnAttach.setOnClickListener { pickImage.launch("image/*") }

        return AlertDialog.Builder(requireContext())
            .setTitle(if (account != null) "عملية على: ${account.name}" else "عملية جديدة")
            .setView(b.root)
            .setPositiveButton("حفظ", null)
            .setNegativeButton("إلغاء", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { validateAndSave(this) }
                }
            }
    }

    private fun setupTypeChips() {
        listOf("CREDIT" to "دائن (له)", "DEBIT" to "مدين (عليه)").forEachIndexed { i, (key, label) ->
            val c = Chip(requireContext()).apply {
                text = label; isCheckable = true; isChecked = i == 0; tag = key
                setOnClickListener { selectedType = key }
            }
            b.chipsType.addView(c)
        }
        selectedType = "CREDIT"
    }

    private fun prefill() {
        val t = existing ?: return
        b.etTitle.setText(t.title)
        b.etAmount.setText(t.amount.toString())
        b.etNotes.setText(t.notes ?: "")
        selectedType = t.type
        imagePath = t.imagePath
        t.imagePath?.let { b.imgPreview.setImageURI(Uri.fromFile(File(it))) }
        cal.timeInMillis = t.timestamp
    }

    private fun setupDate() {
        b.tvDateText.text = DateUtils.formatArabic(cal)
        b.btnPickDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                cal.set(y, m, d)
                b.tvDateText.text = DateUtils.formatArabic(cal)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun validateAndSave(dialog: AlertDialog) {
        val title = b.etTitle.text?.toString()?.trim().orEmpty()
        val amountStr = b.etAmount.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) { b.etTitle.error = "مطلوب"; return }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) { b.etAmount.error = "قيمة غير صحيحة"; return }

        val entity = TransactionEntity(
            id = existing?.id ?: 0,
            accountId = account?.id ?: existing?.accountId ?: 0,
            type = selectedType,
            amount = amount,
            currency = "YER",
            title = title,
            dateText = DateUtils.formatArabic(cal),
            timestamp = cal.timeInMillis,
            notes = b.etNotes.text?.toString()?.takeIf { it.isNotBlank() },
            imagePath = imagePath,
            section = "ACCOUNT"
        )
        onSave(entity)
        dialog.dismiss()
    }
}