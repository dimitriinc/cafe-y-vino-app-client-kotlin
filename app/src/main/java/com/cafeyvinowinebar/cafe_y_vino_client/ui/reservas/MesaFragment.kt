package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentReservasMesaBinding
import kotlinx.coroutines.launch

class MesaFragment : Fragment(), PopupMenu.OnMenuItemClickListener {

    private val viewModel: ReservasViewModel by viewModels()
    private val passAllowed = arguments?.getBoolean("passAllowed") ?: false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // if date or part or both weren't chosen by the user, we should display a denial screen
        // when both values are selected, the safe arg will be true, and we can proceed
        return if (!passAllowed) {
            val view = inflater.inflate(R.layout.fragment_mesa_denied, container)
            view.findViewById<TextView>(R.id.txtReservaDenied).text =
                getString(R.string.reserva_error_fecha)
            view
        } else {
            inflater.inflate(R.layout.fragment_reservas_mesa, container)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (!passAllowed) {
            super.onViewCreated(view, savedInstanceState)
        } else {
            super.onViewCreated(view, savedInstanceState)

            val binding = FragmentReservasMesaBinding.bind(view)
            // create a wrapper for a customized popup menu look
            val wrapper = ContextThemeWrapper(requireContext(), R.style.popupMenuStyle)

            // build a popup menu
            var popup: PopupMenu? = PopupMenu(wrapper, binding.imgEscogerMesa)
            popup?.setOnMenuItemClickListener(this)
            popup?.inflate(R.menu.popup_mesa)
            // send it to view model to block the items that correspond to existing reservations
            viewModel.blockReservedTables(popup)

            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.uiState.collect {

                        popup = it.mesasPopup

                        // if the mesa is chosen, display it
                        if(it.mesa != null) {
                            binding.txtMesa.apply {
                                text = it.mesa
                                visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }

            binding.apply {

                // display a dialog explaining the functioning of the mesa picker
                imgInfoMesa.setOnClickListener {
                    val dialogView = layoutInflater.inflate(R.layout.alert_info, null)
                    val txtMsg = dialogView.findViewById<TextView>(R.id.txtInfoMsg)
                    txtMsg.text = getString(R.string.alert_reserva_mesa_message)
                    AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create()
                        .show()
                }

                // navigate to the fragment that displays the tables arrangement
                imgSala.setOnClickListener {
                    val action = MesaFragmentDirections.actionReservasMesaDestToSalaPlanFragment()
                    findNavController().navigate(action)
                }
                imgEscogerMesa.setOnClickListener {
                    popup?.show()
                }
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