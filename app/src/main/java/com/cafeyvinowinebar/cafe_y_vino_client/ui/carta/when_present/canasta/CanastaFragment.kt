package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.canasta

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
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
import com.cafeyvinowinebar.cafe_y_vino_client.MainNavGraphDirections
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.canasta.ItemCanasta
import com.cafeyvinowinebar.cafe_y_vino_client.data.sources.FirebaseStorageSource
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentCanastaBinding
import com.cafeyvinowinebar.cafe_y_vino_client.interfaces.OnCanastaListener
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CanastaFragment : Fragment(), OnCanastaListener {

    @Inject
    lateinit var fStorage: FirebaseStorageSource
    private val viewModel: CanastaViewModel by viewModels()

    private var _binding: FragmentCanastaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCanastaBinding.inflate(inflater, container, false)
        viewModel.observeCanSendPedidos()
        return binding.root
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterCanasta = CanastaAdapter(this, fStorage)

        binding.apply {

            recCanasta.apply {
                adapter = adapterCanasta
                layoutManager = GridLayoutManager(requireContext(), 2)
                setHasFixedSize(false)
            }
            fabCuenta.setOnClickListener {
                val action = CanastaFragmentDirections.actionCanastaFragmentToCuentaFragment()
                findNavController().navigate(action)
            }
            fabInicio.setOnClickListener {

                // without popping up the stack
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

                    // check if the Canasta Room table is empty
                    if (viewModel.uiState.value.items.value!!.isNotEmpty()) {

                        // check if the user can send pedidos
                        // it will be false between requesting the bill and canceling it
                        if (viewModel.uiState.value.canSendPedidos) {

                            // all he checks are passed, we can send the order
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

                viewModel.collapseCanastaFabs()
            }
        }

        viewModel.uiState.value.items.observe(viewLifecycleOwner) { items ->
            adapterCanasta.submitList(items)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {

                    if (!it.isPresent) {

                        /**
                         * if the user is in the Canasta fragment at the moment of the bill cancellation, the presence status will change,
                         * they should be navigated to the main screen automatically
                         * the user cannot be on the canasta screen while not present
                         */
                        val action = MainNavGraphDirections.actionGlobalHomeFragment()
                        findNavController().navigate(action)
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
        viewModel.addItemToCanasta(ItemCanasta(
            name = item.name,
            category = item.category,
            price = item.price,
            icon = item.icon
        ))
    }

    override fun onCanastaLongClick(item: ItemCanasta) {
        viewModel.removeItemFromCanasta(item)
    }

    override fun onStop() {
        super.onStop()
        viewModel.collapseCanastaFabs()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}