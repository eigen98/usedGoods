package com.example.usedgoods.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usedgoods.R
import com.example.usedgoods.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home){

    private var binding : FragmentHomeBinding? = null
    private lateinit var  articleAdapter : ArticleAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)    //여기서만큼은 널이 아님
        binding = fragmentHomeBinding

        articleAdapter = ArticleAdapter()

        //리스트어답터 연결하여 리사이클러뷰 연결(프라그먼트같은 경우 컨텍스트가 아님this->context)
        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

    }
}