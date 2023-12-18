package com.mintokoneko.notes.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mintokoneko.notes.databinding.ItemNoteCompactBinding
import com.mintokoneko.notes.data.Note
import com.mintokoneko.notes.utils.dpToPx
import com.mintokoneko.notes.utils.getScreenWidthDp

class NoteAdapter(
    private val callback: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DIFF_CALLBACK) {
    inner class NoteViewHolder(private val binding: ItemNoteCompactBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                binding.root.setOnClickListener {
                    callback.invoke(getItem(adapterPosition))
                }
            }
        }

        fun bind(currentNote: Note) {
            val title = currentNote.title
            val content = currentNote.content

            binding.apply {
                itemNoteCompactTitle.text = title
                content?.let { itemNoteCompactContent.text = it }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteCompactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = getItem(position)
        holder.bind(currentNote)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Note, newItem: Note) =
                oldItem == newItem
        }
    }
}