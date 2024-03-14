package com.ljmarinscull.baubuddy.ui.home

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ljmarinscull.baubuddy.R
import com.ljmarinscull.baubuddy.databinding.RemoteResourceLayoutBinding
import com.ljmarinscull.baubuddy.domain.models.Resource
import com.ljmarinscull.baubuddy.util.WHITE_COLOR_HEX

class RemoteResourceAdapter: RecyclerView.Adapter<RemoteResourceAdapter.ViewHolder>() {//Filterable

    private lateinit var _context: Context
    var _itemsAll: List<Resource> = listOf()

    private val _diffCallback = object : DiffUtil.ItemCallback<Resource>() {

        override fun areItemsTheSame(oldItem: Resource, newItem: Resource): Boolean {
            return oldItem.task == newItem.task
        }

        override fun areContentsTheSame(oldItem: Resource, newItem: Resource): Boolean {
            return oldItem == newItem
        }
    }
    private val _differ = AsyncListDiffer(this, _diffCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.remote_resource_layout, parent, false)

        _context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resource = _differ.currentList[position]

        holder.bind(resource)
    }

    override fun getItemCount() = _differ.currentList.size

    fun addItems(items: List<Resource>) {
        _itemsAll = items
        _differ.submitList(items)
    }
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = RemoteResourceLayoutBinding.bind(view)

        fun bind(resource: Resource) = with(binding) {
            task.text = resource.task
            title.text = resource.title
            description.text = resource.description
            colorCode.text = binding.root.context.getString(R.string.color_code_label, resource.colorCode)
            binding.root.setBackgroundColor(Color.parseColor(resource.colorCode.ifEmpty { WHITE_COLOR_HEX }))
        }
    }
}