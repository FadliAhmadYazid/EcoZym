package com.ecozym.wastemanagement.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ecozym.wastemanagement.databinding.ItemTopContributorBinding
import com.ecozym.wastemanagement.models.TopContributor

class TopContributorsAdapter(
    private val onItemClick: (TopContributor) -> Unit = {}
) : ListAdapter<TopContributor, TopContributorsAdapter.ContributorViewHolder>(ContributorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributorViewHolder {
        val binding = ItemTopContributorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContributorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContributorViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class ContributorViewHolder(
        private val binding: ItemTopContributorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contributor: TopContributor, rank: Int) {
            binding.apply {
                tvRank.text = rank.toString()
                tvCompanyName.text = contributor.companyName
                tvWasteAmount.text = "${contributor.totalWaste} kg"
                tvEnzymeProduced.text = "${contributor.enzymeProduced} L"
                tvContributionScore.text = "${contributor.contributionScore}%"

                // Set rank badge color based on position
                when (rank) {
                    1 -> {
                        tvRank.setBackgroundResource(android.R.color.holo_orange_light)
                    }
                    2 -> {
                        tvRank.setBackgroundResource(android.R.color.darker_gray)
                    }
                    3 -> {
                        tvRank.setBackgroundResource(android.R.color.holo_orange_dark)
                    }
                    else -> {
                        tvRank.setBackgroundResource(android.R.color.darker_gray)
                    }
                }

                root.setOnClickListener { onItemClick(contributor) }
            }
        }
    }

    private class ContributorDiffCallback : DiffUtil.ItemCallback<TopContributor>() {
        override fun areItemsTheSame(oldItem: TopContributor, newItem: TopContributor): Boolean {
            return oldItem.companyId == newItem.companyId
        }

        override fun areContentsTheSame(oldItem: TopContributor, newItem: TopContributor): Boolean {
            return oldItem == newItem
        }
    }
}