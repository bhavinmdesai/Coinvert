package dev.bhavindesai.coinvert.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import dev.bhavindesai.coinvert.databinding.FragmentChooseSourceCurrencyBinding
import dev.bhavindesai.coinvert.databinding.ListItemCurrencyBinding
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.viewmodel.ChooseSourceCurrencyViewModel

@AndroidEntryPoint
class ChooseSourceCurrencyFragment : Fragment(), OnItemClickListener {

    private lateinit var binding: FragmentChooseSourceCurrencyBinding
    private val viewModel: ChooseSourceCurrencyViewModel by viewModels()
    private var adapter = CurrencyAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchCurrencies()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChooseSourceCurrencyBinding.inflate(
            inflater,
            container,
            false
        )
        binding.rvCurrencyList.adapter = adapter
        binding.rvCurrencyList.addItemDecoration(
            DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        )
        binding.rvCurrencyList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        setListeners()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.filteredCurrencies.observe(viewLifecycleOwner) { currencies ->
            currencies?.let {
                adapter.currencies = it
            }
        }
    }

    private class CurrencyAdapter(
        private val itemClickListener: OnItemClickListener? = null
    ): RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

        var currencies: List<Currency> = emptyList()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        class CurrencyViewHolder(
            private val binding: ListItemCurrencyBinding,
            private val itemClickListener: OnItemClickListener?
        ): RecyclerView.ViewHolder(binding.root) {
            fun bind(currency: Currency) {
                binding.root.setOnClickListener {
                    itemClickListener?.onItemClick(currency)
                }
                binding.currency = currency
                binding.executePendingBindings()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder =
            CurrencyViewHolder(
                ListItemCurrencyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                itemClickListener
            )

        override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
            holder.bind(currencies[position])
        }

        override fun getItemCount() = currencies.size
    }

    override fun onItemClick(currency: Currency) {
        parentFragmentManager.setFragmentResult(
            "RESULT",
            bundleOf("CURRENCY" to currency)
        )

        findNavController().popBackStack()
    }
}

interface OnItemClickListener {
    fun onItemClick(currency: Currency)
}