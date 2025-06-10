package com.ecozym.wastemanagement.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecozym.wastemanagement.databinding.ItemTrackingOngoingBinding
import com.ecozym.wastemanagement.databinding.ItemTrackingHistoryBinding
import com.ecozym.wastemanagement.models.WasteTracking
import com.ecozym.wastemanagement.utils.DateUtils

class WasteTrackingAdapter(
    private val onItemClick: (WasteTracking) -> Unit
) : ListAdapter<WasteTracking, RecyclerView.ViewHolder>(WasteTrackingDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_ONGOING = 0
        private const val VIEW_TYPE_HISTORY = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isCompleted) VIEW_TYPE_HISTORY else VIEW_TYPE_ONGOING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ONGOING -> {
                val binding = ItemTrackingOngoingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                OngoingViewHolder(binding)
            }
            VIEW_TYPE_HISTORY -> {
                val binding = ItemTrackingHistoryBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HistoryViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is OngoingViewHolder -> holder.bind(item)
            is HistoryViewHolder -> holder.bind(item)
        }
    }

    inner class OngoingViewHolder(
        private val binding: ItemTrackingOngoingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tracking: WasteTracking) {
            binding.apply {
                tvWasteInfo.text = "${tracking.wasteType} - ${tracking.quantity}kg"
                tvCompanyName.text = tracking.companyName
                tvAddress.text = tracking.pickupAddress
                tvStatus.text = tracking.status

                // Fix: Handle nullable Timestamp
                tvEstimatedArrival.text = "Estimated Arrival: ${
                    tracking.estimatedArrival?.let {
                        DateUtils.formatDate(it)
                    } ?: "TBD"
                }"

                tvVehicleType.text = tracking.vehicleType
                tvLicensePlate.text = tracking.licensePlate
                tvDriverName.text = tracking.driverName

                btnCall.setOnClickListener {
                    // Handle call driver
                    onItemClick(tracking)
                }

                btnMessage.setOnClickListener {
                    // Handle message driver
                    onItemClick(tracking)
                }

                root.setOnClickListener { onItemClick(tracking) }
            }
        }
    }

    inner class HistoryViewHolder(
        private val binding: ItemTrackingHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(tracking: WasteTracking) {
            binding.apply {
                tvWasteInfo.text = "${tracking.wasteType} - ${tracking.quantity}kg"
                tvStatus.text = tracking.status

                // Fix: Handle nullable Timestamp
                tvDate.text = tracking.completedAt?.let {
                    DateUtils.formatDate(it)
                } ?: "Date unavailable"

                tvBatchId.text = "Batch ${tracking.batchId}"

                root.setOnClickListener { onItemClick(tracking) }
            }
        }
    }

    private class WasteTrackingDiffCallback : DiffUtil.ItemCallback<WasteTracking>() {
        override fun areItemsTheSame(oldItem: WasteTracking, newItem: WasteTracking): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WasteTracking, newItem: WasteTracking): Boolean {
            return oldItem == newItem
        }
    }
}