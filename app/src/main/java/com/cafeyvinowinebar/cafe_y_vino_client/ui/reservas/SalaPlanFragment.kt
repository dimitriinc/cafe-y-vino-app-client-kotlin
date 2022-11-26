package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.cafeyvinowinebar.cafe_y_vino_client.R

/**
 * Displays the table arrangement
 * No functionalities
 */
class SalaPlanFragment : DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.fragment_sala_plan, null)
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}