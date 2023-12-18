package com.mintokoneko.notes.ui.notes

import CustomLayoutManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.mintokoneko.notes.R
import com.mintokoneko.notes.databinding.FragmentNotesBinding
import com.mintokoneko.notes.data.Note
import com.mintokoneko.notes.ui.notes.noteitem.NoteItemFragment
import com.mintokoneko.notes.utils.dpToPx
import com.mintokoneko.notes.utils.getScreenWidthDp
import java.io.File
import kotlin.math.log

class NotesFragment : Fragment() {
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val fragmentActivity = requireActivity()
        initRecyclers(context, fragmentActivity)
       // initTempUri(context)
        addNotes()
    }

    private fun addNotes() {
        noteAdapter.submitList(
            listOf(
                Note("1", "Hello", null, 0),
                Note("2", "Hello", null, 1),
                Note("3", "Hello", null, 2),
                Note("4", "Hello", null, 3),
                Note("5", "Hello", null, 4),
                Note("6", "Hello", null, 5),
                Note("7", "Hello", null, 6),
                Note("8", "Hello", null, 7),
                Note("9", "Hello", null, 8),
                Note("10", "Hello", null, 9),
                Note("11", "Hello", null, 10),
                Note("12", "Hello", null, 11),
                Note("13", "Hello", null, 12),
                Note("14", "Hello", null, 13),
                Note("15", "Hello", null, 14),
                Note("16", "Hello", null, 15),
                Note("17", "Hello", null, 16),
                Note("18", "Hello", null, 17),
                Note("19", "Hello", null, 18),
                Note("20", "Hello", null, 19),
            )
        )
    }

    private fun initTempUri(context: Context): Uri {
        val tempImagesDir = File(
            context.filesDir,
            getString(R.string.temp_images_dir)
        )
        tempImagesDir.mkdir()

        val tempImage = File(
            tempImagesDir,
            getString(R.string.temp_image)
        )

        return FileProvider.getUriForFile(
            context,
            getString(R.string.authorities),
            tempImage
        )
    }

    private fun initRecyclers(context: Context, fragmentActivity: FragmentActivity) {
        initNotesRecycler(context, fragmentActivity)
    }

    private fun initNotesRecycler(context: Context, fragmentActivity: FragmentActivity) {
        val screenWidthDp = getScreenWidthDp()
        val columnsNum = getColumnsNum(screenWidthDp)
        val childSizeDp = getChildSizeDp(screenWidthDp, columnsNum)


        noteAdapter = NoteAdapter { note ->
            beginTransaction(fragmentActivity, note)
        }

        val notesRecycler = binding.notesRecycler
        notesRecycler.apply {
            adapter = noteAdapter
            layoutManager = CustomLayoutManager(dpToPx(childSizeDp), columnsNum, CustomLayoutManager.Gravity.LEFT)
        }
    }

    private fun getColumnsNum(screenWidthDp: Float): Int {
        return if (screenWidthDp < 600) {
            2
        } else {
            3
        }
    }

    private fun getChildSizeDp(screenWidthDp: Float, columnsNum: Int): Float {
        val childSizeDp = (screenWidthDp - 16F * (columnsNum + 1)) / columnsNum
        return childSizeDp
    }

    private fun beginTransaction(fragmentActivity: FragmentActivity, note: Note) {
        val mainContainerId = R.id.main_container
        val fragmentManager = fragmentActivity.supportFragmentManager
        fragmentManager
            .beginTransaction()
            .replace(mainContainerId, NoteItemFragment.newInstance(note))
            .addToBackStack(NoteItemFragment.NOTE_ITEM_FRAGMENT_TAG)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        const val NOTES_FRAGMENT_TAG = "notes"
    }
}