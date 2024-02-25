package jp.ooooiwocha.wiktionaryopenassistant

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

class MyFragment : Fragment() {
    lateinit var adView: AdView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_layout, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.myRecyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            this.orientation = LinearLayoutManager.VERTICAL
        }
        val adapter = MyAdapter(resources.getStringArray(R.array.setting_items).toMutableList())

        adapter.setOnItemClickListener(
            object: MyAdapter.OnItemClickListener {
            override fun onItemClickListener(view: View, position: Int, clickedText: String) {
                Toast.makeText(requireContext(), "Set Preference by Dragging and Dropping", Toast.LENGTH_LONG).show()
                startActivity(Intent(requireContext(), DetailsActivity::class.java))
            }
        })

        recyclerView.adapter = adapter

        adView = view.findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        return view
    }

}