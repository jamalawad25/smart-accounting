package com.smart.accounting.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import coil.load
import com.smart.accounting.databinding.DialogImagePreviewBinding
import java.io.File

class ImagePreviewDialog(private val path: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val b = DialogImagePreviewBinding.inflate(LayoutInflater.from(requireContext()))
        b.imgFull.load(File(path))
        return AlertDialog.Builder(requireContext()).setView(b.root).create()
    }
}