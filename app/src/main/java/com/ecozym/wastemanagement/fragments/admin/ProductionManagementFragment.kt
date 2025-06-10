package com.example.wastewiseapp.ui.production

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ecozym.wastemanagement.R

class ProductionManagementFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_production_management, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterChips(view)
        setupUpdateButtons(view)
    }

    private fun setupFilterChips(view: View) {
        val chipAllStatus = view.findViewById<TextView>(R.id.chipAllStatus)
        val chipMaterialCollection = view.findViewById<TextView>(R.id.chipMaterialCollection)
        val chipFermentation = view.findViewById<TextView>(R.id.chipFermentation)
        val chipFiltration = view.findViewById<TextView>(R.id.chipFiltration)
        val chipPurification = view.findViewById<TextView>(R.id.chipPurification)
        val chipReadyForReturn = view.findViewById<TextView>(R.id.chipReadyForReturn)

        // Set initial selection for "All Status"
        selectChip(chipAllStatus, listOf(chipAllStatus, chipMaterialCollection, chipFermentation, chipFiltration, chipPurification, chipReadyForReturn))

        chipAllStatus.setOnClickListener {
            selectChip(chipAllStatus, listOf(chipAllStatus, chipMaterialCollection, chipFermentation, chipFiltration, chipPurification, chipReadyForReturn))
            showAllCards(view)
        }

        chipMaterialCollection.setOnClickListener {
            selectChip(chipMaterialCollection, listOf(chipAllStatus, chipMaterialCollection, chipFermentation, chipFiltration, chipPurification, chipReadyForReturn))
            filterByStatus(view, "Material Collection")
        }

        chipFermentation.setOnClickListener {
            selectChip(chipFermentation, listOf(chipAllStatus, chipMaterialCollection, chipFermentation, chipFiltration, chipPurification, chipReadyForReturn))
            filterByStatus(view, "Fermentation")
        }

        chipFiltration.setOnClickListener {
            selectChip(chipFiltration, listOf(chipAllStatus, chipMaterialCollection, chipFermentation, chipFiltration, chipPurification, chipReadyForReturn))
            filterByStatus(view, "Filtration")
        }

        chipPurification.setOnClickListener {
            selectChip(chipPurification, listOf(chipAllStatus, chipMaterialCollection, chipFermentation, chipFiltration, chipPurification, chipReadyForReturn))
            filterByStatus(view, "Purification")
        }

        chipReadyForReturn.setOnClickListener {
            selectChip(chipReadyForReturn, listOf(chipAllStatus, chipMaterialCollection, chipFermentation, chipFiltration, chipPurification, chipReadyForReturn))
            filterByStatus(view, "Ready for Return")
        }
    }

    private fun selectChip(selectedChip: TextView, allChips: List<TextView>) {
        allChips.forEach { chip ->
            if (chip == selectedChip) {
                try {
                    chip.setBackgroundResource(R.drawable.bg_chip_selected)
                    chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                } catch (e: Exception) {
                    // Fallback if drawable doesn't exist
                    chip.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                    chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
            } else {
                try {
                    chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                    chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                } catch (e: Exception) {
                    // Fallback if drawable doesn't exist
                    chip.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.background_light))
                    chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                }
            }
        }
    }

    private fun showAllCards(view: View) {
        val cardProduction1 = view.findViewById<CardView>(R.id.cardProduction1)
        val cardProduction2 = view.findViewById<CardView>(R.id.cardProduction2)
        val cardProduction3 = view.findViewById<CardView>(R.id.cardProduction3)
        val cardProduction4 = view.findViewById<CardView>(R.id.cardProduction4)
        val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)

        cardProduction1?.visibility = View.VISIBLE
        cardProduction2?.visibility = View.VISIBLE
        cardProduction3?.visibility = View.VISIBLE
        cardProduction4?.visibility = View.GONE // Keep hidden as per design
        tvEmptyState?.visibility = View.GONE
    }

    private fun filterByStatus(view: View, status: String) {
        val cardProduction1 = view.findViewById<CardView>(R.id.cardProduction1)
        val cardProduction2 = view.findViewById<CardView>(R.id.cardProduction2)
        val cardProduction3 = view.findViewById<CardView>(R.id.cardProduction3)
        val cardProduction4 = view.findViewById<CardView>(R.id.cardProduction4)
        val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)

        // Hide all cards first
        cardProduction1?.visibility = View.GONE
        cardProduction2?.visibility = View.GONE
        cardProduction3?.visibility = View.GONE
        cardProduction4?.visibility = View.GONE

        // Show cards matching the status
        when (status) {
            "Material Collection" -> {
                cardProduction1?.visibility = View.VISIBLE
            }
            "Fermentation" -> {
                cardProduction2?.visibility = View.VISIBLE
            }
            "Filtration" -> {
                cardProduction3?.visibility = View.VISIBLE
            }
            "Purification" -> {
                cardProduction4?.visibility = View.VISIBLE
            }
            "Ready for Return" -> {
                // No cards in this status currently
                tvEmptyState?.visibility = View.VISIBLE
            }
        }

        // Check if any cards are visible
        val hasVisibleCards = (cardProduction1?.visibility == View.VISIBLE) ||
            (cardProduction2?.visibility == View.VISIBLE) ||
            (cardProduction3?.visibility == View.VISIBLE) ||
            (cardProduction4?.visibility == View.VISIBLE)

        if (!hasVisibleCards && status != "Ready for Return") {
            tvEmptyState?.visibility = View.VISIBLE
        } else if (hasVisibleCards) {
            tvEmptyState?.visibility = View.GONE
        }
    }

    private fun setupUpdateButtons(view: View) {
        val btnUpdateStatus1 = view.findViewById<Button>(R.id.btnUpdateStatus1)
        val btnUpdateStatus2 = view.findViewById<Button>(R.id.btnUpdateStatus2)
        val btnUpdateStatus3 = view.findViewById<Button>(R.id.btnUpdateStatus3)
        val btnUpdateStatus4 = view.findViewById<Button>(R.id.btnUpdateStatus4)

        btnUpdateStatus1?.setOnClickListener {
            updateStatus1(view)
        }

        btnUpdateStatus2?.setOnClickListener {
            updateStatus2(view)
        }

        btnUpdateStatus3?.setOnClickListener {
            updateStatus3(view)
        }

        btnUpdateStatus4?.setOnClickListener {
            updateStatus4(view)
        }
    }

    private fun updateStatus1(view: View) {
        val tvStatus1 = view.findViewById<TextView>(R.id.tvStatus1)
        val btnUpdateStatus1 = view.findViewById<Button>(R.id.btnUpdateStatus1)

        // Update from Material Collection to Fermentation
        tvStatus1?.text = "Fermentation"
        try {
            tvStatus1?.setBackgroundResource(R.drawable.bg_status_yellow)
        } catch (e: Exception) {
            tvStatus1?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark))
        }
        btnUpdateStatus1?.text = "Update to Filtration →"

        showToast("ECO-2025-075 updated to Fermentation")
    }

    private fun updateStatus2(view: View) {
        val tvStatus2 = view.findViewById<TextView>(R.id.tvStatus2)
        val btnUpdateStatus2 = view.findViewById<Button>(R.id.btnUpdateStatus2)

        // Update from Fermentation to Filtration
        tvStatus2?.text = "Filtration"
        try {
            tvStatus2?.setBackgroundResource(R.drawable.bg_blue)
        } catch (e: Exception) {
            tvStatus2?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
        }
        btnUpdateStatus2?.text = "Update to Purification →"

        showToast("ECO-2025-074 updated to Filtration")
    }

    private fun updateStatus3(view: View) {
        val tvStatus3 = view.findViewById<TextView>(R.id.tvStatus3)
        val btnUpdateStatus3 = view.findViewById<Button>(R.id.btnUpdateStatus3)

        // Update from Filtration to Purification
        tvStatus3?.text = "Purification"
        try {
            tvStatus3?.setBackgroundResource(R.drawable.bg_purple)
        } catch (e: Exception) {
            tvStatus3?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_purple))
        }
        btnUpdateStatus3?.text = "Mark Ready for Return →"

        showToast("ECO-2025-073 updated to Purification")
    }

    private fun updateStatus4(view: View) {
        val tvStatus4 = view.findViewById<TextView>(R.id.tvStatus4)
        val btnUpdateStatus4 = view.findViewById<Button>(R.id.btnUpdateStatus4)

        // Update from Purification to Ready for Return
        tvStatus4?.text = "Ready for Return"
        try {
            tvStatus4?.setBackgroundResource(R.drawable.bg_status_green)
        } catch (e: Exception) {
            tvStatus4?.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        }
        btnUpdateStatus4?.text = "Process Complete ✓"

        showToast("ECO-2025-072 ready for return")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}