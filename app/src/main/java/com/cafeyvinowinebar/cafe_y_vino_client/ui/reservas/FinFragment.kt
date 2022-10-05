package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentReservasFinBinding
import com.cafeyvinowinebar.cafe_y_vino_client.isDarkThemeOn
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import kotlinx.coroutines.launch

class FinFragment : Fragment() {

    private val viewModel: ReservasViewModel by viewModels()
    private val passAllowed = arguments?.getBoolean("passAllowed") ?: false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return if (!passAllowed) {
            val view = inflater.inflate(R.layout.fragment_mesa_denied, container)
            view.findViewById<TextView>(R.id.txtReservaDenied).text =
                getString(R.string.reserva_error_mesa)
            view
        } else {
            inflater.inflate(R.layout.fragment_reservas_fin, container)
        }
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentReservasFinBinding.bind(view)
        val wrapper = ContextThemeWrapper(requireContext(), R.style.popupMenuStyle)

        var clockPopupMenu: PopupMenu? = PopupMenu(wrapper, binding.imgClock)
        viewModel.populateClockPopup(clockPopupMenu)
        val onClockMenuItemClickListener = PopupMenu.OnMenuItemClickListener {
            viewModel.setHora(it.title.toString())
            true
        }
        clockPopupMenu?.setOnMenuItemClickListener(onClockMenuItemClickListener)

        val paxPopupMenu = PopupMenu(wrapper, binding.imgPax)
        val onPaxMenuItemClickListener = PopupMenu.OnMenuItemClickListener {
            viewModel.setPax(it.title.toString())
            true
        }
        paxPopupMenu.setOnMenuItemClickListener(onPaxMenuItemClickListener)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->

                    clockPopupMenu = uiState.clockPopup

                    if (uiState.hora != null) {
                        binding.txtClockDisplay.apply {
                            text = uiState.hora
                            visibility = View.VISIBLE
                        }
                    }

                    if (uiState.mesa == "05" || uiState.mesa == "11") {
                        paxPopupMenu.inflate(R.menu.popup_pax_large)
                    } else {
                        paxPopupMenu.inflate(R.menu.popup_pax_small)
                    }

                    binding.fabUpload.setOnClickListener {

                        if (isOnline(requireContext())) {
                            if (uiState.hora == null) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.reserva_error_hora,
                                    Toast.LENGTH_LONG
                                ).show()
                                return@setOnClickListener
                            }
                            if (uiState.pax == null) {
                                Toast.makeText(
                                    requireContext(),
                                    R.string.reserva_error_pax,
                                    Toast.LENGTH_LONG
                                ).show()
                                return@setOnClickListener
                            }

                            val comentario = binding.edtComment.text.toString().trim()
                            viewModel.setComentario(comentario)
                            val confirmationView = layoutInflater.inflate(R.layout.reserva_confirmation, null)
                            val dialogBuilder = AlertDialog.Builder(requireContext())
                            confirmationView.apply {
                                findViewById<TextView>(R.id.txtReservaFecha).text = uiState.fecha
                                findViewById<TextView>(R.id.txtReservaMesa).text = uiState.mesa
                                findViewById<TextView>(R.id.txtReservaHora).text = uiState.hora
                                findViewById<TextView>(R.id.txtReservaPax).text = uiState.pax
                                if (comentario.isEmpty()) {
                                    findViewById<TextView>(R.id.txtReservaComentario).text = getString(R.string.no_comment)
                                } else {
                                    findViewById<TextView>(R.id.txtReservaComentario).text = uiState.comentario
                                }

                            }
                            with(dialogBuilder) {
                                setView(confirmationView)
                                setMessage(R.string.reserva_tiempo_espera)
                                setPositiveButton(R.string.confirmar) { _, _ ->
                                    viewModel.makeReservation(comentario)
                                }
                            }

                            val confirmationDialog = dialogBuilder.create()
                            confirmationDialog.show()

                            if (requireContext().isDarkThemeOn()) {
                                val buttonPositive = confirmationDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                                buttonPositive.setTextColor(ContextCompat.getColor(requireContext(), R.color.soft_light_teal))
                                val buttonNegative = confirmationDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                                buttonNegative.setTextColor(ContextCompat.getColor(requireContext(), R.color.soft_light_teal))

                            }

                        } else {
                            Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT).show()
                        }
                    }

                    if (uiState.isReservaSent) {
                        findNavController().navigate(R.id.main_nav_graph)
                    }

                }
            }
        }

        binding.apply {
            imgClock.setOnClickListener {
                clockPopupMenu?.show()
            }
            imgPax.setOnClickListener {
                paxPopupMenu.show()
            }
        }
    }
}