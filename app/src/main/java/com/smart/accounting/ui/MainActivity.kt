package com.smart.accounting.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.smart.accounting.App
import com.smart.accounting.databinding.ActivityMainBinding
import com.smart.accounting.databinding.CardSummaryBinding
import com.smart.accounting.util.BackupManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val app by lazy { application as App }
    private val vm: MainViewModel by viewModels {
        VMFactory(app.repository, app.personRepository)
    }
    private lateinit var txAdapter: TransactionAdapter
    private lateinit var personAdapter: PersonAdapter

    private val exportDb = registerForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        val ok = BackupManager.writeToUri(this, uri)
        toast(if (ok) "تم التصدير بنجاح" else "فشل التصدير")
    }

    private val importDb = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        AlertDialog.Builder(this)
            .setTitle("تأكيد الاستعادة")
            .setMessage("سيتم استبدال جميع البيانات الحالية. متابعة؟")
            .setPositiveButton("استعادة") { _, _ ->
                val ok = BackupManager.restoreFromUri(this, uri)
                if (ok) {
                    recreate()
                    toast("تمت الاستعادة بنجاح")
                } else toast("فشل الاستعادة")
            }
            .setNegativeButton("إلغاء", null).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupTabs()
        setupRecycler()
        setupFabs()
        observe()
    }

    private fun setupTabs() {
        b.tabs.addTab(b.tabs.newTab().setText("المعاملات اليومية").setTag("CASH"))
        b.tabs.addTab(b.tabs.newTab().setText("المصروف").setTag("EXPENSE"))
        b.tabs.addTab(b.tabs.newTab().setText("الديون").setTag("DEBT"))
        b.tabs.addTab(b.tabs.newTab().setText("الحسابات").setTag("PERSONS"))
        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                vm.setSection(tab.tag as String)
                updateUiForSection(tab.tag as String)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        updateUiForSection("CASH")
    }

    private fun updateUiForSection(section: String) {
        if (section == "PERSONS") {
            b.recycler.adapter = personAdapter
            b.fabAdd.text = "إضافة حساب"
        } else {
            b.recycler.adapter = txAdapter
            b.fabAdd.text = "إضافة جديدة"
        }
    }

    private fun setupRecycler() {
        txAdapter = TransactionAdapter(
            onImage = { ImagePreviewDialog(it).show(supportFragmentManager, "img") },
            onDelete = { item ->
                AlertDialog.Builder(this).setTitle("حذف")
                    .setMessage("حذف \"${item.title}\"؟")
                    .setPositiveButton("حذف") { _, _ -> vm.delete(item.id) }
                    .setNegativeButton("إلغاء", null).show()
            },
            onEdit = { item ->
                AddEditDialog(item.section, item, vm.persons.value) { vm.save(it) }
                    .show(supportFragmentManager, "edit")
            }
        )
        personAdapter = PersonAdapter(
            repo = app.repository,
            onDelete = { p ->
                AlertDialog.Builder(this).setTitle("حذف حساب")
                    .setMessage("حذف \"${p.name}\"؟")
                    .setPositiveButton("حذف") { _, _ -> vm.deletePerson(p) }
                    .setNegativeButton("إلغاء", null).show()
            },
            onOpen = { PersonDetailDialog(it).show(supportFragmentManager, "detail") }
        )
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = txAdapter
    }

    private fun setupFabs() {
        b.fabAdd.setOnClickListener {
            val sec = vm.section.value
            if (sec == "PERSONS") {
                AddPersonDialog { vm.addPerson(it) }.show(supportFragmentManager, "addPerson")
            } else {
                AddEditDialog(sec, null, vm.persons.value) { vm.save(it) }
                    .show(supportFragmentManager, "add")
            }
        }
        b.fabBackup.setOnClickListener { showBackupMenu() }
    }

    private fun showBackupMenu() {
        val options = arrayOf("تصدير نسخة (.db)", "استعادة نسخة", "مشاركة عبر التطبيقات")
        AlertDialog.Builder(this).setTitle("النسخ الاحتياطي")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportDb.launch("smart_accounting_backup.db")
                    1 -> importDb.launch("*/*")
                    2 -> {
                        val f = BackupManager.stagedDbCopy(this) ?: return@setItems
                        startActivity(
                            Intent.createChooser(
                                BackupManager.shareIntent(this, f),
                                "مشاركة النسخة"
                            )
                        )
                    }
                }
            }.show()
    }

    private fun observe() {
        lifecycleScope.launch {
            vm.items.collectLatest { list ->
                txAdapter.submitList(list)
                b.emptyView.visibility =
                    if (list.isEmpty() && vm.section.value != "PERSONS")
                        View.VISIBLE else View.GONE
            }
        }
        lifecycleScope.launch {
            vm.persons.collectLatest { list ->
                personAdapter.submitList(list)
                if (vm.section.value == "PERSONS") {
                    b.emptyView.visibility =
                        if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
        lifecycleScope.launch { vm.summaryYer.collect { bindSummary(b.cardYer, it) } }
        lifecycleScope.launch { vm.summarySar.collect { bindSummary(b.cardSar, it) } }
        lifecycleScope.launch { vm.summaryUsd.collect { bindSummary(b.cardUsd, it) } }
    }

    private fun bindSummary(included: CardSummaryBinding, s: CurrencySummary) {
        included.tvCurrency.text = when (s.currency) {
            "YER" -> "ريال يمني"
            "SAR" -> "ريال سعودي"
            else -> "دولار"
        }
        included.tvBalance.text = "%.2f".format(s.balance)
        included.tvSub.text = "+${"%.2f".format(s.income)} / -${"%.2f".format(s.expense)}"
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_LONG).show()
}