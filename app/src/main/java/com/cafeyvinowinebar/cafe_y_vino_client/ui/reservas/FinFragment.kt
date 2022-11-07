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
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cafeyvinowinebar.cafe_y_vino_client.MainNavGraphDirections
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentReservasFinBinding
import com.cafeyvinowinebar.cafe_y_vino_client.isDarkThemeOn
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import kotlinx.coroutines.launch

class FinFragment : Fragment() {

    private val viewModel: ReservasViewModel by hiltNavGraphViewModels(R.id.reservas_nav_graph)
    private val passAllowed = arguments?.getBoolean("passAllowed") ?: false
    private lateinit var clockPopupMenu: PopupMenu
    private lateinit var paxPopupMenu: PopupMenu

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // for the user to proceed the mesa value of the UI state must be non-null
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

        buildClockPopup(wrapper, binding.imgClock)
        buildPaxPopup(wrapper, binding.imgPax)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->

                    if (uiState.hora != null) {
                        binding.txtClockDisplay.apply {
                            text = uiState.hora
                            visibility = View.VISIBLE
                        }
                    }
                    
                    if (uiState.isReservaSent != null) {
                        if (uiState.isReservaSent) {
                            // if the value is true, the reserva sending is done, we can navigate to the main screen
                            Toast.makeText(requireContext(), getString(R.string.solicitud_reserva, uiState.firstName), Toast.LENGTH_SHORT).show()
                            val action = MainNavGraphDirections.actionGlobalHomeFragment()
                            findNavController().navigate(action)
                            
                        } else {
                            Toast.makeText(requireContext(), R.string.error, Toast.LENGTH_SHORT).show()
                            viewModel.nullifyReservaSent()
                        }
                        
                    }

                }
            }
        }

        binding.apply {

            imgClock.setOnClickListener {
                clockPopupMenu.show()
            }
            imgPax.setOnClickListener {
                paxPopupMenu.show()
            }

            fabUpload.setOnClickListener {

                if (isOnline(requireContext())) {

                    // get a snapshot of the UI state
                    val uiState = viewModel.uiState.value

                    // check if all the values are set
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

                    // set the comment
                    val comentario = binding.edtComment.text.toString().trim()
                    viewModel.setComentario(comentario)

                    // build a confirmation dialog, where you display all the collected data
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
                            // when the user confirms, the view model is ready to start the process
                            viewModel.makeReservation()
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
        }
    }

    /**
     * Depending on the chosen table we inflate different menus, since tables 5 and 11 are big ones and more people will fit there
     */
    private fun buildPaxPopup(wrapper: ContextThemeWrapper, view: View) {
        paxPopupMenu = PopupMenu(wrapper, view)
        val onPaxMenuItemClickListener = PopupMenu.OnMenuItemClickListener {
            viewModel.setPax(it.title.toString())
            true
        }
        paxPopupMenu.setOnMenuItemClickListener(onPaxMenuItemClickListener)
        val mesa = viewModel.uiState.value.mesa
        if (mesa == "05" || mesa == "11") {
            paxPopupMenu.inflate(R.menu.popup_pax_large)
        } else {
            paxPopupMenu.inflate(R.menu.popup_pax_small)
        }
    }

    /**
     * We get list of available hours from the utils Room table
     * For each hour in the list we add one menu item
     */
    private fun buildClockPopup(wrapper: ContextThemeWrapper, view: View) {
        clockPopupMenu = PopupMenu(wrapper, view)
        val availableHours = if (viewModel.uiState.value.part == "dia") {
            viewModel.uiState.value.horasDeDia
        } else {
            viewModel.uiState.value.horasDeNoche
        }
        availableHours.forEach {
            clockPopupMenu.menu.add(it)
        }
        val onClockMenuItemClickListener = PopupMenu.OnMenuItemClickListener {
            viewModel.setHora(it.title.toString())
            true
        }
        clockPopupMenu.setOnMenuItemClickListener(onClockMenuItemClickListener)
    }
}