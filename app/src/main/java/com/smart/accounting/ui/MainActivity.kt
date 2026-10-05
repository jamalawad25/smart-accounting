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
import com.smart.accounting.util.BackupManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val app by lazy { application as App }
    private val vm: MainViewModel by viewModels {
        VMFactory(app.accountRepo, app.transactionRepo)
    }
    private lateinit var accountAdapter: AccountAdapter
    private lateinit var txAdapter: TransactionAdapter

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
                if (ok) { recreate(); toast("تمت الاستعادة بنجاح") } else toast("فشل الاستعادة")
            }
            .setNegativeButton("إلغاء", null).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupRecycler()
        setupTabs()
        setupFabs()
        observe()
    }

    private fun setupTabs() {
        b.tabs.addTab(b.tabs.newTab().setText("الحسابات").setTag("ACCOUNTS"))
        b.tabs.addTab(b.tabs.newTab().setText("المعاملات اليومية").setTag("CASH"))
        b.tabs.addTab(b.tabs.newTab().setText("المصروف").setTag("EXPENSE"))
        b.tabs.addTab(b.tabs.newTab().setText("الديون").setTag("DEBT"))
        b.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                vm.setSection(tab.tag as String)
                updateUiForSection(tab.tag as String)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        updateUiForSection("ACCOUNTS")
    }

    private fun updateUiForSection(section: String) {
        if (section == "ACCOUNTS") {
            b.recycler.adapter = accountAdapter
            b.fabAdd.text = "إضافة حساب"
        } else {
            b.recycler.adapter = txAdapter
            b.fabAdd.text = "إضافة عملية"
        }
    }

    private fun setupRecycler() {
        accountAdapter = AccountAdapter(
            repo = app.transactionRepo,
            onDelete = { acc ->
                AlertDialog.Builder(this).setTitle("حذف حساب")
                    .setMessage("حذف \"${acc.name}\" وكل عملياته؟")
                    .setPositiveButton("حذف") { _, _ -> vm.deleteAccount(acc) }
                    .setNegativeButton("إلغاء", null).show()
            },
            onOpen = { AccountStatementDialog(it).show(supportFragmentManager, "statement") }
        )
        txAdapter = TransactionAdapter(
            onImage = { ImagePreviewDialog(it).show(supportFragmentManager, "img") },
            onDelete = { item ->
                AlertDialog.Builder(this).setTitle("حذف")
                    .setMessage("حذف \"${item.title}\"؟")
                    .setPositiveButton("حذف") { _, _ -> vm.deleteTransaction(item.id) }
                    .setNegativeButton("إلغاء", null).show()
            },
            onEdit = { item ->
                AddEditTransactionDialog(null, item) { vm.saveTransaction(it) }
                    .show(supportFragmentManager, "edit")
            }
        )
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = accountAdapter
    }

    private fun setupFabs() {
        b.fabAdd.setOnClickListener {
            if (vm.section.value == "ACCOUNTS") {
                AddEditAccountDialog { vm.addAccount(it) }.show(supportFragmentManager, "addAccount")
            } else {
                AddEditTransactionDialog(null, null) { vm.saveTransaction(it) }
                    .show(supportFragmentManager, "addTx")
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
                        startActivity(Intent.createChooser(BackupManager.shareIntent(this, f), "مشاركة النسخة"))
                    }
                }
            }.show()
    }

    private fun observe() {
        lifecycleScope.launch {
            vm.accounts.collectLatest { list ->
                accountAdapter.submitList(list)
                if (vm.section.value == "ACCOUNTS")
                    b.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        lifecycleScope.launch {
            vm.transactions.collectLatest { list ->
                txAdapter.submitList(list)
                if (vm.section.value != "ACCOUNTS")
                    b.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_LONG).show()
}