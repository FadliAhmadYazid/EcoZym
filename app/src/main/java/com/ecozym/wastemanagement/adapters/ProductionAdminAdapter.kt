package com.ecozym.wastemanagement.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecozym.wastemanagement.databinding.ItemProductionAdminBinding
import com.ecozym.wastemanagement.models.Production
import com.ecozym.wastemanagement.utils.DateUtils

class ProductionAdminAdapter(
    private val isPreview: Boolean = false,
    private val onItemClick: (Production) -> Unit
) : ListAdapter<Production, ProductionAdminAdapter.ProductionViewHolder>(ProductionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductionViewHolder {
        val binding = ItemProductionAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductionViewHolder(
        private val binding: ItemProductionAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(production: Production) {
            binding.apply {
                tvBatchId.text = production.batchId
                tvWasteType.text = production.wasteType
                tvStatus.text = production.status
                tvSource.text = production.sourceName

                // Use inputQuantity instead of quantity
                tvQuantity.text = "${production.inputQuantity} kg"

                // Handle nullable Timestamp
                tvEstCompletion.text = production.estimatedCompletion?.let {
                    DateUtils.formatDate(it)
                } ?: "Not set"

                // Update production stepper visual state
                updateProductionStepper(production.status)

                btnUpdateStatus.apply {
                    if (isPreview) {
                        visibility = View.GONE
                    } else {
                        text = getNextStatusButtonText(production.status)
                        setOnClickListener { onItemClick(production) }
                    }
                }

                root.setOnClickListener { onItemClick(production) }
            }
        }

        private fun updateProductionStepper(currentStatus: String) {
            // Update stepper visual state based on current status
            // This would involve changing the drawable/color of each step indicator
        }

        private fun getNextStatusButtonText(currentStatus: String): String {
            return when (currentStatus) {
                "Material Collection" -> "Start Fermentation →"
                "Fermentation" -> "Start Filtration →"
                "Filtration" -> "Start Purification →"
                "Purification" -> "Ready for Return →"
                else -> "Completed"
            }
        }
    }

    private class ProductionDiffCallback : DiffUtil.ItemCallback<Production>() {
        override fun areItemsTheSame(oldItem: Production, newItem: Production): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Production, newItem: Production): Boolean {
            return oldItem == newItem
        }
    }
}