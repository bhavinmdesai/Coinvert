package dev.bhavindesai.coinvert.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.bhavindesai.coinvert.databinding.FragmentEnterAmountBinding
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.viewmodel.EnterAmountViewModel

@AndroidEntryPoint
class EnterAmountFragment: Fragment() {

    private lateinit var binding: FragmentEnterAmountBinding
    private val viewModel by viewModels<EnterAmountViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnterAmountBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        setListeners()

        setObservers()

        return binding.root
    }

    private fun setObservers() {
        viewModel.amount.observe(viewLifecycleOwner) {
            binding.txtAmount.text = it
        }

        viewModel.selectedCurrency.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.clGroupNoCurrencySelected.visibility = View.VISIBLE
                binding.btnChooseCurrency.visibility = View.GONE

                binding.txtCurrencyAbbreviation.text = it.abbreviation
                binding.txtCurrencyName.text = it.name
            } else {
                binding.clGroupNoCurrencySelected.visibility = View.GONE
                binding.btnChooseCurrency.visibility = View.VISIBLE
            }
        }

        viewModel.redirectToConvert.observe(viewLifecycleOwner) {
            findNavController().navigate(EnterAmountFragmentDirections
                    .actionEnterAmountFragmentToCurrencyConversionFragment(it.first, it.second))
        }

        viewModel.enableConvertButton.observe(viewLifecycleOwner) {
            binding.btnConvert.isEnabled = it
        }
    }

    private fun setListeners() {

        parentFragmentManager
            .setFragmentResultListener(
                "RESULT",
                viewLifecycleOwner,
                { requestKey: String, result: Bundle ->
                    if (requestKey == "RESULT") {
                        result.getParcelable<Currency>("CURRENCY")?.let {
                            viewModel.onCurrencySelection(it)
                        }
                    }
                }
            )

        binding.btnChooseCurrency.setOnClickListener { openCurrencySelectionFragment() }
        binding.clSelectedCurrency.setOnClickListener { openCurrencySelectionFragment() }
        binding.btnConvert.setOnClickListener { viewModel.onConvertClick() }
    }

    private fun openCurrencySelectionFragment() {
        findNavController().navigate(
            EnterAmountFragmentDirections
                .actionEnterAmountFragmentToChooseSourceCurrencyFragment()
        )
    }

}