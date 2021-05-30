package dev.bhavindesai.coinvert.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.bhavindesai.coinvert.R
import dev.bhavindesai.coinvert.databinding.FragmentCurrencyConversionBinding
import dev.bhavindesai.coinvert.databinding.ListItemCurrencyQuotesBinding
import dev.bhavindesai.domain.local.CurrencyQuote
import dev.bhavindesai.viewmodel.CurrencyConversionViewModel
import kotlinx.coroutines.FlowPreview

@AndroidEntryPoint
class CurrencyConversionFragment : Fragment() {

    private lateinit var binding: FragmentCurrencyConversionBinding
    private val viewModel by viewModels<CurrencyConversionViewModel>()
    private var adapter = CurrencyQuotesAdapter()
    private val args: CurrencyConversionFragmentArgs by navArgs()

    @FlowPreview
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyConversionBinding.inflate(inflater, container, false)
        binding.rvCurrencyList.adapter = adapter
        binding.rvCurrencyList.addItemDecoration(
            DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        )
        binding.rvCurrencyList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        setObservers()

        setListeners()

        viewModel.convertCurrency(args.currencyAbbreviation, args.amount)

        return binding.root
    }

    private fun setListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    viewModel.setSearchText(it)
                }
                return true
            }
        })
    }

    @FlowPreview
    private fun setObservers() {
        viewModel.fetchQuotesFetchedAt().observe(viewLifecycleOwner) { timestamp ->
            binding.txtQuotesLastUpdatedAt.text = getString(R.string.quotes_updated_at, timestamp)
        }
        viewModel.filteredCurrencyQuotes.observe(viewLifecycleOwner) { currencyQuotes ->
            if (currencyQuotes != null) {
                adapter.currencyQuotes = currencyQuotes
            }
        }
    }

    private class CurrencyQuotesAdapter: RecyclerView.Adapter<CurrencyQuotesAdapter.CurrencyViewHolder>() {

        var currencyQuotes: List<CurrencyQuote> = emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        class CurrencyViewHolder(
            private val binding: ListItemCurrencyQuotesBinding
        ): RecyclerView.ViewHolder(binding.root) {
            fun bind(currencyQuote: CurrencyQuote) {
                binding.currencyQuote = currencyQuote
                binding.executePendingBindings()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder =
            CurrencyViewHolder(
                ListItemCurrencyQuotesBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
            holder.bind(currencyQuotes[position])
        }

        override fun getItemCount() = currencyQuotes.size
    }
}