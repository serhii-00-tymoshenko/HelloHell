package com.mintokoneko.notes.ui.notes.noteitem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mintokoneko.notes.databinding.FragmentNoteItemBinding
import com.mintokoneko.notes.data.Note


class NoteItemFragment : Fragment() {
    private var _binding: FragmentNoteItemBinding? = null
    private val binding get() = _binding!!

    private val welcomeNote by lazy { Note("Welcome", "It's me, mintokoneko's Notes.", null, -1) }
    private val args by lazy { arguments?.getParcelable(NOTE_ARGUMENT) ?: welcomeNote }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val NOTE_ITEM_FRAGMENT_TAG = "note_item"
        private const val NOTE_ARGUMENT = "note_argument"

        @JvmStatic
        fun newInstance(note: Note) =
            NoteItemFragment().apply {
                Bundle().apply {
                    putParcelable(NOTE_ARGUMENT, note)
                }
            }
    }
}