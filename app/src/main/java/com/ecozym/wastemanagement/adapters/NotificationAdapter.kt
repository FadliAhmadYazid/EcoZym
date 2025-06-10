package com.ecozym.wastemanagement.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecozym.wastemanagement.databinding.ItemNotificationAdminBinding
import com.ecozym.wastemanagement.models.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onNotificationClick: (Notification) -> Unit
) : ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            with(binding) {
                // Basic info
                tvTitle.text = notification.title
                tvDescription.text = notification.message

                // Format timestamp
                val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                tvTimestamp.text = dateFormat.format(notification.createdAt.toDate())

                // Set notification type icon and color
                when (notification.type) {
                    "admin" -> {
                        ivNotificationIcon.setImageResource(android.R.drawable.ic_dialog_info)
                        when (notification.data["action"]) {
                            "waste_approval" -> {
                                ivNotificationIcon.setImageResource(android.R.drawable.ic_dialog_alert)
                                binding.root.setBackgroundColor(
                                    android.graphics.Color.parseColor("#FFF3E0") // Orange tint
                                )
                            }
                        }
                    }
                    "company" -> {
                        ivNotificationIcon.setImageResource(android.R.drawable.ic_dialog_email)
                    }
                    else -> {
                        ivNotificationIcon.setImageResource(android.R.drawable.ic_dialog_info)
                    }
                }

                // Visual state for read/unread
                if (notification.isRead) {
                    tvTitle.setTypeface(null, Typeface.NORMAL)
                    tvDescription.setTypeface(null, Typeface.NORMAL)
                    viewUnreadIndicator.visibility = android.view.View.GONE
                    binding.root.alpha = 0.7f
                } else {
                    tvTitle.setTypeface(null, Typeface.BOLD)
                    tvDescription.setTypeface(null, Typeface.NORMAL)
                    viewUnreadIndicator.visibility = android.view.View.VISIBLE
                    binding.root.alpha = 1.0f
                }

                // Priority indicator for urgent notifications
                if (notification.data["action"] == "waste_approval") {
                    viewPriorityIndicator.visibility = android.view.View.VISIBLE
                    viewPriorityIndicator.setBackgroundColor(
                        android.graphics.Color.parseColor("#FF5722") // Red for urgent
                    )
                } else {
                    viewPriorityIndicator.visibility = android.view.View.GONE
                }

                // Click listener
                binding.root.setOnClickListener {
                    onNotificationClick(notification)
                }

                // Long click for additional actions
                binding.root.setOnLongClickListener {
                    // Could show context menu for mark as read, delete, etc.
                    true
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}