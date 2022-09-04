package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.app.DatePickerDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentReservasFechaBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FechaFragment : Fragment(R.layout.fragment_reservas_fecha) {

    private val viewModel: ReservasViewModel by viewModels()
    lateinit var horas: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getPartHoras()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentReservasFechaBinding.bind(view)

        // some data to construct the popup menu
        val wrapper = ContextThemeWrapper(requireContext(), R.style.popupMenuStyle)
        val icons = listOf(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_day),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_night)
        )

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

                    if (it.fecha != null) {
                        binding.txtDateDisplay.apply {
                            text = it.fecha
                            visibility = View.VISIBLE
                        }
                    }

                    if (it.part != null) {
                        binding.txtPartOfDay.apply {
                            text = it.part
                            visibility = View.VISIBLE
                        }
                    }

                    horas = it.horas
                }
            }
        }


        binding.apply {

            // this is a UI logic
            imgInfo.setOnClickListener {
                showAlertDialog()
            }

            imgCalendar.setOnClickListener {
                val calendar = Calendar.getInstance()

                val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    val sdf = SimpleDateFormat("EEEE", Locale.ENGLISH)
                    val date = Date(year, monthOfYear, dayOfMonth - 1)
                    val dayOfWeek = sdf.format(date)
                    if (dayOfWeek.equals("friday") || dayOfWeek.equals("saturday") || dayOfWeek.equals("sunday")) {
                        showAlertDialog()
                    } else {
                        calendar.set(year, monthOfYear, dayOfMonth)
                        viewModel.setDate(calendar)
                    }
                }
                val datePicker = DatePickerDialog(requireContext())
                datePicker.setOnDateSetListener(dateSetListener)
                datePicker.show()

            }

            imgDayNight.setOnClickListener {
                val popupMenu = constructPopupMenu(wrapper, icons, horas, it)
                try {
                    val popup = PopupMenu::class.java.getDeclaredField("mPopup")
                    popup.isAccessible = true
                    val menu = popup.get(popupMenu)
                    menu.javaClass
                        .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(menu, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    popupMenu.show()
                }
            }
        }

    }

    private fun constructPopupMenu(
        wrapper: ContextThemeWrapper,
        icons: List<Drawable?>,
        horas: List<String>,
        view: View
    ): PopupMenu {
        val popupMenu = PopupMenu(wrapper, view)
        popupMenu.menu.add(1, R.id.day, 0, horas[0]).icon.apply { icons[0] }
        popupMenu.menu.add(1, R.id.night, 1, horas[1]).icon.apply { icons[1] }

        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.day -> {
                    viewModel.setPartOfDay("dia")
                    true
                }
                R.id.night -> {
                    viewModel.setPartOfDay("noche")
                    true
                }
                else -> true
            }
        }

        return popupMenu
    }

    private fun showAlertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.alert_info, null)
        val txtMsg = dialogView.findViewById<TextView>(R.id.txtInfoMsg)
        txtMsg.text = getString(R.string.alert_reserva_fecha_message)
        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
            .show()
    }
}