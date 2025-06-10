package com.ecozym.wastemanagement.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecozym.wastemanagement.databinding.ItemPartnerPendingBinding
import com.ecozym.wastemanagement.models.User

class PartnerPendingAdapter(
    private val onViewDetails: (User) -> Unit,
    private val onApprove: (User) -> Unit,
    private val onReject: (User) -> Unit
) : ListAdapter<User, PartnerPendingAdapter.PendingViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val binding = ItemPartnerPendingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PendingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PendingViewHolder(
        private val binding: ItemPartnerPendingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                tvCompanyName.text = user.companyName ?: "N/A"
                tvEmail.text = user.email
                tvIndustry.text = user.industryType ?: "N/A"
                tvAddress.text = user.address ?: "N/A"
                tvDocument.text = user.documentUrl?.substringAfterLast("/") ?: "No document"

                btnViewDetails.setOnClickListener { onViewDetails(user) }
                btnApprove.setOnClickListener { onApprove(user) }
                btnReject.setOnClickListener { onReject(user) }
            }
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
