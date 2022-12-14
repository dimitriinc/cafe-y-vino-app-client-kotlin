package com.cafeyvinowinebar.cafe_y_vino_client.ui.carta.when_present.cuenta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cafeyvinowinebar.cafe_y_vino_client.*
import com.cafeyvinowinebar.cafe_y_vino_client.data.data_models.ItemCuenta
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentCuentaBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CuentaFragment : Fragment() {

    private val viewModel: CuentaViewModel by viewModels()
    private lateinit var adapterCuenta: CuentaAdapter

    @Inject
    lateinit var fStore: FirebaseFirestore

    private var _binding: FragmentCuentaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCuentaBinding.inflate(inflater, container, false)
        viewModel.observePresence()
        viewModel.observeCanSendPedidos()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            fabCuentaHome.setOnClickListener {
                val action = CuentaFragmentDirections.actionCuentaFragmentToHomeFragment()
                findNavController().navigate(action)
            }
            fabCuentaMenu.setOnClickListener {
                val action = CuentaFragmentDirections.actionCuentaFragmentToCategoriesFragment()
                findNavController().navigate(action)
            }

            // show different pay methods
            fabPedirCuenta.setOnClickListener {
                viewModel.expandPayModeFabs()
            }

            // four fabs that define the pay method, call the same function with the corresponding pay mode
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

                    /** when the bill is cancelled by the admin app, the presence status of the user becomes false
                     * and they're navigated to the main screen
                     * the user can't stay in the cuenta fragment while in negative presence status
                     */
                    if (!uiState.isPresent) {
                        val action = MainNavGraphDirections.actionGlobalHomeFragment()
                        findNavController().navigate(action)
                    }

                    binding.txtMontoTotal.text = uiState.totalCuentaCost.toString()

                    // configure the parent fab's behavior
                    if (uiState.isPedirCuentaFabExpanded) {
                        binding.apply {
                            fabParentCuenta.apply {
                                setImageResource(R.drawable.ic_collapse)
                                setOnClickListener {
                                    collapseAllFabs()
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
                                    // we can give the user access to send cuenta requests on when the preference is true
                                    if (uiState.canSendPedidos) {
                                        // so when it's true we expand the pedir fab
                                        viewModel.expandPedirCuentaFab()
                                    } else {
                                        // if it's false, the user can't expand fabs, and thus can't send requests
                                        Toast.makeText(
                                            requireContext(),
                                            R.string.cuenta_no_expand,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                }
                            }
                            fabPedirCuenta.hide()
                            txtPedirCuenta.visibility = GONE
                        }
                    }

                    // the expanding behavior
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
                            txtPedirCuenta.visibility = GONE
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
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val query =
            fStore.collection("cuentas/${getCurrentDate()}/cuentas corrientes/${viewModel.uiState.value.userId}/cuenta")
        val options = FirestoreRecyclerOptions.Builder<ItemCuenta>()
            .setQuery(query, ItemCuenta::class.java)
            .build()
        adapterCuenta = CuentaAdapter(options)

        binding.recCuenta.apply {
            adapter = adapterCuenta
            layoutManager = LinearLayoutManager(requireContext())
        }

        adapterCuenta.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapterCuenta.stopListening()
        collapseAllFabs()
    }

    /**
     * Check the conditions under which it's possible to send a cuenta request
     * If the conditions are satisfied, tell the view model to start the sending
     */
    private fun checkOnlineSendRequest(payMode: String) {
            if (viewModel.uiState.value.totalCuentaCost > 0) {
                if (isOnline(requireContext())) {
                    viewModel.sendCuentaRequest(payMode)
                } else {
                    Toast.makeText(requireContext(), R.string.no_connection, Toast.LENGTH_SHORT)
                        .show()
                    collapseAllFabs()
                }
            } else {
                // there is nothing in the bill
                Toast.makeText(requireContext(), R.string.cuenta_vacia, Toast.LENGTH_SHORT).show()
                collapseAllFabs()
            }
    }

    private fun collapseAllFabs() {
        viewModel.collapsePayModeFabs()
        viewModel.collapsePedirCuentaFab()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}