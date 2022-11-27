package com.cafeyvinowinebar.cafe_y_vino_client.ui.reservas

import android.app.DatePickerDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentReservasFechaBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * First screen in the reservas graph
 * User is expected to choose the date and the part of day (day/night) for his reservation
 */
@AndroidEntryPoint
class FechaFragment : Fragment(R.layout.fragment_reservas_fecha) {

    private val viewModel: ReservasViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var horas: List<String>

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

                    horas = it.horas

                    // when user chooses a date or a part, the UI state values stop being null
                    // and we can display them on the screen
                    if (it.fecha != null) {
                        binding.txtDateDisplay.apply {
                            text = it.fecha
                            visibility = View.VISIBLE
                        }
                    }
                    if (it.part != null) {
                        binding.imgPartOfDayDisplay.apply {
                            visibility = View.VISIBLE
                            if (it.part == "dia") {
                                setImageResource(R.drawable.ic_day)
                            } else {
                                setImageResource(R.drawable.ic_night)
                            }

                        }
                    }
                }
            }
        }


        binding.apply {

            imgInfo.setOnClickListener {
                showAlertDialog()
            }

            imgCalendar.setOnClickListener {
                val calendar = Calendar.getInstance()

                // define what happens when the date is set
                val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->

                    // first we get a string for the day of week chosen to make sure it's not a weekend day
                    val sdf = SimpleDateFormat("EEEE", Locale.ENGLISH)
                    val date = Date(year, monthOfYear, dayOfMonth - 1)
                    val dayOfWeek = sdf.format(date)
                    if (dayOfWeek.equals("Friday") || dayOfWeek.equals("Saturday") || dayOfWeek.equals("Sunday")) {
                        showAlertDialog()
                    } else {

                        // the day is a workweek day - we can set the date officially
                        calendar.set(year, monthOfYear, dayOfMonth)
                        viewModel.setDate(calendar)
                    }
                }

                // instantiate a date picker
                val datePicker = DatePickerDialog(requireContext())
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
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

    /**
     * Because in theory the hours for parts of day can be changed, we must construct a popup menu programmatically
     */
    private fun constructPopupMenu(
        wrapper: ContextThemeWrapper,
        icons: List<Drawable?>,
        horas: List<String>,
        view: View
    ): PopupMenu {
        val popupMenu = PopupMenu(wrapper, view)
        popupMenu.menu.add(1, R.id.day, 0, horas[0]).icon.apply { icons[0] }
        popupMenu.menu.add(1, R.id.night, 1, horas[1]).icon.apply { icons[1] }

        // we also define an on menu item click listener
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

    /**
     * Displays an alert dialog with info about the functionality of the screen
     * Can be triggered by pressing the info img, or if the user chooses unavailable date from the calendar
     */
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