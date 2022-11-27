package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentReservasMesaBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "MesaFragment"

@AndroidEntryPoint
class MesaFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private val viewModel: ReservasViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private var passAllowed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * if date or part or both weren't chosen by the user, we should display a denial screen
         * when both values are selected, we can proceed
         */
        val uiState = viewModel.uiState.value
        return if (uiState.fecha == null || uiState.part == null) {
            val view = inflater.inflate(R.layout.fragment_mesa_denied, container, false)
            view.findViewById<TextView>(R.id.txtReservaDenied).text =
                getString(R.string.reserva_error_fecha)
            view
        } else {
            passAllowed = true
            inflater.inflate(R.layout.fragment_reservas_mesa, container, false)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (!passAllowed) {
            super.onViewCreated(view, savedInstanceState)
        } else {
            super.onViewCreated(view, savedInstanceState)

            viewModel.getReservedTables()
            val binding = FragmentReservasMesaBinding.bind(view)
            // create a wrapper for a customized popup menu look
            val wrapper = ContextThemeWrapper(requireContext(), R.style.popupMenuStyle)

            val popup = PopupMenu(wrapper, binding.imgEscogerMesa)
            popup.setOnMenuItemClickListener(this)
            popup.inflate(R.menu.popup_mesa)

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.collect {

                        if (it.listOfReservedTables.isNotEmpty()) {
                            blockReservedTables(popup, it.listOfReservedTables)
                        }

                        if (it.mesa != null) {
                            binding.txtMesa.apply {
                                text = it.mesa
                                visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            binding.apply {

                imgInfoMesa.setOnClickListener {
                    val dialogView = layoutInflater.inflate(R.layout.alert_info, null)
                    val txtMsg = dialogView.findViewById<TextView>(R.id.txtInfoMsg)
                    txtMsg.text = getString(R.string.alert_reserva_mesa_message)
                    AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create()
                        .show()
                }

                imgSala.setOnClickListener {
                    val image = ImageView(requireContext())
                    image.setImageResource(R.drawable.plan)
                    AlertDialog.Builder(requireContext())
                        .setView(image)
                        .create()
                        .show()

                }
                imgEscogerMesa.setOnClickListener {
                    popup.show()
                }
            }
        }
    }

    /**
     * For each string in the list of reserved mesas stored in the UI state
     * We disable the corresponding menu item
     */
    private fun blockReservedTables(popupMenu: PopupMenu, listOfTables: List<String>) {
        val menu = popupMenu.menu
        listOfTables.forEach {
            when (it) {
                "01" -> menu.findItem(R.id.m01).isEnabled = false
                "02" -> menu.findItem(R.id.m02).isEnabled = false
                "03" -> menu.findItem(R.id.m03).isEnabled = false
                "04" -> menu.findItem(R.id.m04).isEnabled = false
                "05" -> menu.findItem(R.id.m05).isEnabled = false
                "06" -> menu.findItem(R.id.m06).isEnabled = false
                "07" -> menu.findItem(R.id.m07).isEnabled = false
                "08" -> menu.findItem(R.id.m08).isEnabled = false
                "09" -> menu.findItem(R.id.m09).isEnabled = false
                "10" -> menu.findItem(R.id.m10).isEnabled = false
                "11" -> menu.findItem(R.id.m11).isEnabled = false
                "12" -> menu.findItem(R.id.m12).isEnabled = false
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {

        val mesa = when (item?.itemId) {
            R.id.m01 -> "01"
            R.id.m02 -> "02"
            R.id.m03 -> "03"
            R.id.m04 -> "04"
            R.id.m05 -> "05"
            R.id.m06 -> "06"
            R.id.m07 -> "07"
            R.id.m08 -> "08"
            R.id.m09 -> "09"
            R.id.m10 -> "10"
            R.id.m11 -> "11"
            R.id.m12 -> "12"
            else -> "00"
        }
        viewModel.setMesa(mesa)
        return true
    }
}