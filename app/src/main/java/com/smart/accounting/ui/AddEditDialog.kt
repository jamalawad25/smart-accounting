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
import com.smart.accounting.data.model.PersonEntity
import com.smart.accounting.data.model.TransactionEntity
import com.smart.accounting.databinding.DialogAddEditBinding
import com.smart.accounting.util.DateUtils
import com.smart.accounting.util.ImageStorage
import java.io.File
import java.util.Calendar

class AddEditDialog(
    private val section: String,
    private val existing: TransactionEntity? = null,
    private val personsList: List<PersonEntity> = emptyList(),
    private val onSave: (TransactionEntity) -> Unit
) : DialogFragment() {

    private lateinit var b: DialogAddEditBinding
    private var selectedCurrency = "YER"
    private var selectedType = "IN"
    private var selectedPayment = "CASH"
    private var selectedCategory: String? = null
    private var imagePath: String? = null
    private val cal: Calendar = Calendar.getInstance()

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            val saved = ImageStorage.copyFromUri(requireContext(), uri)
            if (saved != null) {
                imagePath = saved
                b.imgPreview.setImageURI(Uri.fromFile(File(saved)))
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        b = DialogAddEditBinding.inflate(LayoutInflater.from(requireContext()))
        setupTypeChips()
        setupCategoryChips()
        setupChipsBehavior()
        prefill()
        setupDate()
        b.btnAttach.setOnClickListener { pickImage.launch("image/*") }

        return AlertDialog.Builder(requireContext())
            .setTitle(
                when (section) {
                    "CASH" -> "معاملة يومية"
                    "DEBT" -> "دين / التزام"
                    else -> "مصروف"
                }
            )
            .setView(b.root)
            .setPositiveButton("حفظ", null)
            .setNegativeButton("إلغاء", null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        validateAndSave(this)
                    }
                }
            }
    }

    private fun setupTypeChips() {
        val types = when (section) {
            "CASH" -> listOf("IN" to "وارد (+)", "OUT" to "صادر (-)")
            "DEBT" -> listOf("RECEIVABLE" to "مدين لنا", "PAYABLE" to "دائن علينا")
            else -> listOf("OUT" to "مصروف")
        }
        types.forEachIndexed { i, (key, label) ->
            val c = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = i == 0
                tag = key
                setOnClickListener { selectedType = key }
            }
            b.chipsType.addView(c)
        }
        selectedType = types.first().first

        if (section == "DEBT") {
            b.layoutPerson.visibility = View.VISIBLE
            b.layoutPersonLabel.visibility = View.VISIBLE
        }
        if (section == "EXPENSE") {
            b.tvCategoryLabel.visibility = View.VISIBLE
            b.chipsCategory.visibility = View.VISIBLE
        }
    }

    private fun setupCategoryChips() {
        listOf("إيجار", "كهرباء/ماء", "مشتريات وقطع", "رواتب", "نثريات").forEach { cat ->
            val c = Chip(requireContext()).apply {
                text = cat
                isCheckable = true
                tag = cat
                setOnClickListener { selectedCategory = cat }
            }
            b.chipsCategory.addView(c)
        }
    }

    private fun setupChipsBehavior() {
        b.chipsCurrency.setOnCheckedChangeListener { _, checkedId ->
            selectedCurrency = when (checkedId) {
                R.id.chipSar -> "SAR"
                R.id.chipUsd -> "USD"
                else -> "YER"
            }
        }
        b.chipsPayment.setOnCheckedChangeListener { _, checkedId ->
            selectedPayment = when (checkedId) {
                R.id.chipCredit -> "CREDIT"
                R.id.chipWallet -> "WALLET"
                else -> "CASH"
            }
            b.layoutWallet.visibility =
                if (selectedPayment == "WALLET") View.VISIBLE else View.GONE
        }
    }

    private fun prefill() {
        val t = existing ?: return
        b.etTitle.setText(t.title)
        b.etAmount.setText(t.amount.toString())
        b.etNotes.setText(t.notes ?: "")
        b.etWallet.setText(t.walletName ?: "")
        b.etPerson.setText(t.personName ?: "")
        selectedCurrency = t.currency
        selectedType = t.type
        selectedPayment = t.paymentMethod
        selectedCategory = t.category
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
        if (title.isEmpty()) {
            b.etTitle.error = "مطلوب"
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            b.etAmount.error = "قيمة غير صحيحة"
            return
        }
        if (selectedPayment == "WALLET" && b.etWallet.text.isNullOrBlank()) {
            b.etWallet.error = "اسم المحفظة مطلوب"
            return
        }

        val personName = if (section == "DEBT") b.etPerson.text?.toString()?.trim() else null
        val personId = if (section == "DEBT") {
            personsList.firstOrNull { it.name == personName }?.id ?: existing?.personId
        } else null

        val entity = TransactionEntity(
            id = existing?.id ?: 0,
            section = section,
            title = title,
            amount = amount,
            currency = selectedCurrency,
            type = selectedType,
            paymentMethod = selectedPayment,
            walletName = b.etWallet.text?.toString()?.takeIf { selectedPayment == "WALLET" },
            category = if (section == "EXPENSE") selectedCategory else null,
            personId = personId,
            personName = personName,
            dueDate = if (section == "DEBT") DateUtils.formatArabic(cal) else null,
            dateText = DateUtils.formatArabic(cal),
            timestamp = cal.timeInMillis,
            notes = b.etNotes.text?.toString()?.takeIf { it.isNotBlank() },
            imagePath = imagePath
        )
        onSave(entity)
        dialog.dismiss()
    }
}