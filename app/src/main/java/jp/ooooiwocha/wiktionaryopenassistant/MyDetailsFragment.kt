package jp.ooooiwocha.wiktionaryopenassistant

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyDetailsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_settings, menu)
    }
    var recyclerView: RecyclerView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_layout, container, false)

        recyclerView = view.findViewById(R.id.myRecyclerView)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
        }
        val langCodeList: ArrayList<String> = ArrayList<String>()

        resources.openRawResource(R.raw.lang_pref).bufferedReader().forEachLine {
            langCodeList.add(it)
        }


        val adapter = MyAdapter(langCodeList)
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                // Adapter内でアイテムの移動を処理
                adapter.moveItem(fromPos, toPos)
                return true
            }


            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // スワイプでの削除は使用しない場合、このメソッドは空
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        adapter.setOnItemClickListener(
            object: MyAdapter.OnItemClickListener {
                override fun onItemClickListener(view: View, position: Int, clickedText: String) {
                    //Toast.makeText(requireContext(), "${clickedText}がタップされました", Toast.LENGTH_SHORT).show()

                }
            })


        recyclerView?.adapter = adapter

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_apply -> {
                // アダプターからデータリストを取得
                val currentDataList = (recyclerView?.adapter as MyAdapter).getDataList()
                // データリストの内容を順に処理
                val sharedPreferences = requireContext().getSharedPreferences("lang_pref", Context.MODE_PRIVATE)
                val edit = sharedPreferences.edit()
                edit.clear()
                edit.apply()
                currentDataList.forEach { item ->
                    // ここで各項目に対する処理を実行
                    // 例: Log出力、Toast表示など
                    edit.putString(sharedPreferences.getAll().size.toString(), item)
                    edit.apply()
                }
                Toast.makeText(requireContext(), "Setting Updated.", Toast.LENGTH_SHORT).show()
                activity?.finish()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }

    }
}
