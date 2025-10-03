package com.example.test.adapter

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ListAdapter
import com.example.test.R
import com.example.test.model.recipt.OcrItemUi

class OcrItemAdapter(
    private val onSearch: (position: Int, keyword: String) -> Unit,
    private val onItemChanged: (position: Int, newValue: OcrItemUi) -> Unit
) : ListAdapter<OcrItemUi, OcrItemAdapter.VH>(diff) {

    companion object {
        val diff = object : DiffUtil.ItemCallback<OcrItemUi>() {
            override fun areItemsTheSame(o: OcrItemUi, n: OcrItemUi) = o.nameRaw == n.nameRaw
            override fun areContentsTheSame(o: OcrItemUi, n: OcrItemUi) = o == n
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_ocr_item, parent, false)
        return VH(v, onSearch, onItemChanged)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        v: View,
        private val onSearch: (Int, String) -> Unit,
        private val onItemChanged: (Int, OcrItemUi) -> Unit
    ) : RecyclerView.ViewHolder(v) {
        private val cb: CheckBox = v.findViewById(R.id.cb)
        private val etName: EditText = v.findViewById(R.id.etName)
        private val etQty: EditText = v.findViewById(R.id.etQty)
        private val tvMatch: TextView = v.findViewById(R.id.tvMatch)
        private val btnSearch: ImageButton = v.findViewById(R.id.btnSearch)

        fun bind(m: OcrItemUi) {
            cb.setOnCheckedChangeListener(null)
            etName.setText(m.nameRaw)
            etQty.setText(m.quantityStr)
            cb.isChecked = m.checked
            tvMatch.text = m.matchedName?.let { "매칭: $it" } ?: "미매칭"

            cb.setOnCheckedChangeListener { _, checked ->
                onItemChanged(bindingAdapterPosition, m.copy(checked = checked))
            }

            etName.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    onItemChanged(bindingAdapterPosition, m.copy(nameRaw = etName.text.toString()))
                }
            }
            etQty.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    onItemChanged(bindingAdapterPosition, m.copy(quantityStr = etQty.text.toString()))
                }
            }

            btnSearch.setOnClickListener {
                onSearch(bindingAdapterPosition, etName.text.toString())
            }
        }
    }
}
