package jp.ooooiwocha.wiktionaryopenassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class MyAdapter(private val dataList: MutableList<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
    }

    fun getDataList(): MutableList<String> {
        return dataList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.textView.text = item

        // ViewHolderのビューにクリックリスナーを設定
        holder.itemView.setOnClickListener {
            // カスタムリスナーを呼び出し
            val adapterPos = holder.bindingAdapterPosition
            if (adapterPos != RecyclerView.NO_POSITION && ::listener.isInitialized) {
                listener.onItemClickListener(it, adapterPos, dataList[adapterPos])
            }
        }
    }

    override fun getItemCount() = dataList.size

    lateinit var listener: OnItemClickListener

    interface OnItemClickListener{
        fun onItemClickListener(view: View, position: Int, clickedText: String)
    }

    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }
    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(dataList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(dataList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }
}