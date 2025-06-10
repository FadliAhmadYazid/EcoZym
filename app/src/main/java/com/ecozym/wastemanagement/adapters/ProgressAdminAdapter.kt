package com.ecozym.wastemanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecozym.wastemanagement.databinding.ItemProgressAdminBinding
import com.ecozym.wastemanagement.models.ProgressTracking
import com.ecozym.wastemanagement.utils.DateUtils

class ProgressAdminAdapter(
    private val isPreview: Boolean = false,
    private val onItemClick: (ProgressTracking) -> Unit
) : ListAdapter<ProgressTracking, ProgressAdminAdapter.ProgressViewHolder>(ProgressDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val binding = ItemProgressAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProgressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProgressViewHolder(
        private val binding: ItemProgressAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(progress: ProgressTracking) {
            binding.apply {
                tvCompanyName.text = progress.companyName
                tvWasteType.text = progress.wasteType
                tvTransportId.text = progress.transportId
                tvQuantity.text = "${progress.quantity} tons"
                tvStatus.text = progress.status

                tvDriverInitial.text = progress.driverName.firstOrNull()?.toString()?.uppercase() ?: "D"
                tvDriverName.text = progress.driverName
                tvVehicleInfo.text = "${progress.vehicleType} • ${progress.licensePlate}"

                // Handle nullable Timestamp values
                tvPickupDate.text = progress.pickupDate?.let {
                    DateUtils.formatDate(it)
                } ?: "Not scheduled"

                tvEstArrival.text = progress.estimatedArrival?.let {
                    DateUtils.formatDate(it)
                } ?: "TBD"

                tvEstReturn.text = progress.estimatedReturn?.let {
                    DateUtils.formatDate(it)
                } ?: "TBD"

                // Update progress stepper visual state
                updateProgressStepper(progress.status)

                btnCall.setOnClickListener {
                    // Handle call driver
                }

                btnMessage.setOnClickListener {
                    // Handle message driver
                }

                btnUpdateStatus.apply {
                    if (isPreview) {
                        visibility = View.GONE
                    } else {
                        text = getNextStatusButtonText(progress.status)
                        setOnClickListener { onItemClick(progress) }
                    }
                }

                root.setOnClickListener { onItemClick(progress) }
            }
        }

        private fun updateProgressStepper(currentStatus: String) {
            // Update stepper visual state based on current status
            // This would involve changing the drawable/color of each step indicator
        }

        private fun getNextStatusButtonText(currentStatus: String): String {
            return when (currentStatus) {
                "Scheduled" -> "Start Transit →"
                "In Transit" -> "Start Processing →"
                "Processing" -> "Start Return →"
                "Returning" -> "Mark Delivered →"
                else -> "Completed"
            }
        }
    }

    private class ProgressDiffCallback : DiffUtil.ItemCallback<ProgressTracking>() {
        override fun areItemsTheSame(oldItem: ProgressTracking, newItem: ProgressTracking): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProgressTracking, newItem: ProgressTracking): Boolean {
            return oldItem == newItem
        }
    }
}