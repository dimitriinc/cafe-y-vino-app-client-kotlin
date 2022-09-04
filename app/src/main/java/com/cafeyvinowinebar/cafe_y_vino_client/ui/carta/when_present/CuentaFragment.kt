package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present

import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.data.model_classes.ItemCuenta
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentCuentaBinding
import com.cafeyvinowinebar.cafe_y_vino_client.getCurrentDate
import com.cafeyvinowinebar.cafe_y_vino_client.isOnline
import com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.adapters.CuentaAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import javax.inject.Inject

class CuentaFragment : Fragment(R.layout.fragment_cuenta) {

    private val viewModel: CanastaCuentaViewModel by viewModels()
    private lateinit var adapterCuenta: CuentaAdapter

    @Inject
    lateinit var fStore: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCuentaBinding.bind(view)

        binding.apply {
            fabCuentaHome.setOnClickListener {
                // TODO: navigate to the main fragment
            }
            fabCuentaMenu.setOnClickListener {
                val action = CuentaFragmentDirections.actionCuentaFragmentToCategoriesFragment()
                findNavController().navigate(action)
            }
            fabPedirCuenta.setOnClickListener {
                viewModel.expandPayModeFabs()
            }
            fabCash.setOnClickListener {
                checkOnlineSendRequest("efectivo")
            }
            fabPos.setOnClickListener {
                checkOnlineSendRequest("POS")
            }
            fabYape.setOnClickListener {
                checkOnlineSendRequest("Yape")
            }
            fabCrypto.setOnClickListener {
                checkOnlineSendRequest("cripto")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    if (!uiState.isPresent) {
                        // TODO: navigate to the main fragment
                        viewModel.updateCanSendPedido(true)
                    }

                    val query =
                        fStore.collection("cuentas/${getCurrentDate()}/cuentas corrientes/${uiState.userId}/cuenta")
                    val options = FirestoreRecyclerOptions.Builder<ItemCuenta>()
                        .setQuery(query, ItemCuenta::class.java)
                        .build()
                    adapterCuenta = CuentaAdapter(options)

                    binding.apply {
                        recCuenta.apply {
                            adapter = adapterCuenta
                            layoutManager = LinearLayoutManager(requireContext())
                            setHasFixedSize(true)
                        }
                    }
                    if (uiState.isPedirCuentaFabExpanded) {
                        binding.apply {
                            fabParentCuenta.apply {
                                setImageResource(R.drawable.ic_collapse)
                                setOnClickListener {
                                    viewModel.collapsePedirCuentaFab()
                                    viewModel.collapsePayModeFabs()
                                }
                            }
                            fabPedirCuenta.show()
                            txtPedirCuenta.visibility = VISIBLE
                        }
                    } else {
                        binding.apply {
                            fabParentCuenta.apply {
                                setImageResource(R.drawable.ic_expand)
                                setOnClickListener {
                                    if (uiState.canSendPedidos) {
                                        viewModel.expandPedirCuentaFab()
                                    } else {
                                        Toast.makeText(requireContext(), R.string.cuenta_no_expand, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            }
                            fabPedirCuenta.hide()
                            txtPedirCuenta.visibility = GONE
                        }
                    }

                    if (uiState.arePayModeFabsExpanded) {
                        binding.apply {
                            fabCash.show()
                            fabCrypto.show()
                            fabPos.show()
                            fabYape.show()
                            txtCash.visibility = VISIBLE
                            txtCrypto.visibility = VISIBLE
                            txtPos.visibility = VISIBLE
                            txtYape.visibility = VISIBLE
                        }
                    } else {
                        binding.apply {
                            fabCash.hide()
                            fabCrypto.hide()
                            fabPos.hide()
                            fabYape.hide()
                            txtCash.visibility = GONE
                            txtCrypto.visibility = GONE
                            txtPos.visibility = GONE
                            txtYape.visibility = GONE
                        }
                    }

                    if (uiState.isCuentaEmpty) {
                        Toast.makeText(requireContext(), R.string.cuenta_vacia, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapterCuenta.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterCuenta.stopListening()
        viewModel.collapsePedirCuentaFab()
        viewModel.collapsePayModeFabs()
    }

    private fun checkOnlineSendRequest(payMode: String) {
        if (isOnline(requireContext())) {
            viewModel.sendCuentaRequest(payMode)
        } else {
            Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT).show()
        }
    }
}