package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentCanastaBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnCanastaListener
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.adapters.CanastaAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CanastaFragment : Fragment(R.layout.fragment_canasta), OnCanastaListener {

    private val viewModel: CanastaCuentaViewModel by viewModels()
    private lateinit var adapter: CanastaAdapter

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCanastaBinding.bind(view)
        val adapterCanasta = CanastaAdapter(this)

        binding.apply {
            recCanasta.apply {
                adapter = adapterCanasta
                layoutManager = GridLayoutManager(requireContext(), 2)
                setHasFixedSize(true)
            }
            fabCuenta.setOnClickListener {
                val action = CanastaFragmentDirections.actionCanastaFragmentToCuentaFragment()
                findNavController().navigate(action)
            }
            fabInicio.setOnClickListener {
                findNavController().navigate(R.id.main_nav_graph)
            }
            fabInfo.setOnClickListener {
                val infoView = layoutInflater.inflate(R.layout.alert_info, null)
                val txtMsg = infoView.findViewById<TextView>(R.id.txtInfoMsg)
                txtMsg.text = getString(R.string.message_canasta_info)
                AlertDialog.Builder(requireContext())
                    .setView(infoView)
                    .create()
                    .show()
                viewModel.collapseCanastaFabs()
            }
            fabMenu.setOnClickListener {
                val action = CanastaFragmentDirections.actionCanastaFragmentToCategoriesFragment()
                findNavController().navigate(action)
            }
            fabUpload.setOnClickListener {
                if (isOnline(requireContext())) {

                    if (viewModel.uiState.value.items.value!!.isNotEmpty()) {

                        if (viewModel.uiState.value.canSendPedidos) {
                            viewModel.sendPedido()
                            Toast.makeText(
                                requireContext(),
                                R.string.canasta_on_sending_pedido,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                R.string.canasta_no_sending,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), R.string.vacia_canasta, Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    it.items.observe(viewLifecycleOwner) { items ->
                        adapterCanasta.submitList(items)
                    }

                    if (!it.isPresent) {
                        // with a popup
                        findNavController().navigate(R.id.main_nav_graph)
                    }

                    if (it.areCanastaFabsExpanded) {
                        binding.apply {
                            fabParent.apply {
                                setImageResource(R.drawable.ic_collapse)
                                setOnClickListener {
                                    viewModel.collapseCanastaFabs()
                                }
                            }
                            fabMenu.show()
                            fabInfo.show()
                            fabInicio.show()
                            fabCuenta.show()
                            fabUpload.show()
                            txtBackToCarta.visibility = VISIBLE
                            txtCuenta.visibility = VISIBLE
                            txtInfo.visibility = VISIBLE
                            txtPedido.visibility = VISIBLE
                            txtInicio.visibility = VISIBLE
                        }
                    } else {
                        binding.apply {
                            fabParent.apply {
                                setImageResource(R.drawable.ic_expand)
                                setOnClickListener {
                                    viewModel.expandCanastaFabs()
                                }
                            }
                            fabMenu.hide()
                            fabInicio.hide()
                            fabInfo.hide()
                            fabCuenta.hide()
                            fabUpload.hide()
                            txtBackToCarta.visibility = GONE
                            txtCuenta.visibility = GONE
                            txtInfo.visibility = GONE
                            txtPedido.visibility = GONE
                            txtInicio.visibility = GONE
                        }
                    }
                }
            }
        }
    }

    override fun onCanastaClick(item: ItemCanasta) {
        viewModel.addItemToCanasta(item)
    }

    override fun onCanastaLongClick(item: ItemCanasta) {
        viewModel.removeItemFromCanasta(item)
    }

    override fun onStop() {
        super.onStop()
        viewModel.collapseCanastaFabs()
    }

}