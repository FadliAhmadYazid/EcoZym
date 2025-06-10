package com.ecozym.wastemanagement.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecozym.wastemanagement.databinding.ItemWasteApprovalBinding
import com.ecozym.wastemanagement.models.WasteRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WasteApprovalAdapter(
    private val onApproveClick: (WasteRegistration) -> Unit,
    private val onRejectClick: (WasteRegistration) -> Unit
) : ListAdapter<WasteRegistration, WasteApprovalAdapter.WasteApprovalViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WasteApprovalViewHolder {
        val binding = ItemWasteApprovalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WasteApprovalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WasteApprovalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WasteApprovalViewHolder(
        private val binding: ItemWasteApprovalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(registration: WasteRegistration) {
            with(binding) {
                // Handle empty company name
                val displayCompanyName = if (registration.companyName.isBlank()) {
                    "Company ID: ${registration.companyId.take(8)}..."
                } else {
                    registration.companyName
                }

                tvCompanyName.text = displayCompanyName
                tvWasteType.text = registration.wasteType
                tvQuantity.text = "${registration.quantity.toInt()} kg"
                tvLocation.text = registration.location

                // Format total price
                tvTotalPrice.text = "Rp ${String.format("%,d", registration.totalPrice.toInt())}"

                // Format date
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = registration.createdAt.toDate()
                tvCreatedAt.text = dateFormat.format(date)

                // Status
                tvStatus.text = registration.status.replaceFirstChar { it.uppercase() }

                // Set click listeners
                btnApprove.setOnClickListener {
                    onApproveClick(registration)
                }

                btnReject.setOnClickListener {
                    onRejectClick(registration)
                }

                // Additional info if available
                registration.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        tvNotes?.text = "Notes: $notes"
                        tvNotes?.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<WasteRegistration>() {
        override fun areItemsTheSame(oldItem: WasteRegistration, newItem: WasteRegistration): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WasteRegistration, newItem: WasteRegistration): Boolean {
            return oldItem == newItem
        }
    }
}