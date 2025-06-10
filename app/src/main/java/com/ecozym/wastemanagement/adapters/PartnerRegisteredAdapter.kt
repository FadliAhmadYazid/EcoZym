package com.ecozym.wastemanagement.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecozym.wastemanagement.databinding.ItemPartnerRegisteredBinding
import com.ecozym.wastemanagement.models.User

class PartnerRegisteredAdapter(
    private val onEdit: (User) -> Unit,
    private val onDelete: (User) -> Unit
) : ListAdapter<User, PartnerRegisteredAdapter.RegisteredViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegisteredViewHolder {
        val binding = ItemPartnerRegisteredBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RegisteredViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RegisteredViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RegisteredViewHolder(
        private val binding: ItemPartnerRegisteredBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                tvPartnerType.text = when (user.role) {
                    "company" -> "Company"
                    "admin" -> "Admin"
                    "driver" -> "Driver"
                    else -> "Partner"
                }

                when (user.role) {
                    "company" -> {
                        tvName.text = user.companyName ?: "Unknown Company"
                        tvIndustryOrRole.text = user.industryType ?: "N/A"
                    }
                    "admin" -> {
                        tvName.text = user.name ?: "Unknown Admin"
                        tvIndustryOrRole.text = when (user.adminRole) {
                            "super_admin" -> "Super Admin"
                            "progress_admin" -> "Progress Admin"
                            "production_admin" -> "Production Admin"
                            else -> "Admin"
                        }
                    }
                    "driver" -> {
                        tvName.text = user.name ?: "Unknown Driver"
                        tvIndustryOrRole.text = "${user.vehicleType} - ${user.licensePlate}"
                    }
                }

                tvEmail.text = user.email
                tvAddress.text = user.address ?: "N/A"

                btnEdit.setOnClickListener { onEdit(user) }
                btnDelete.setOnClickListener { onDelete(user) }
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
