package com.example.usedgoods.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.usedgoods.DBKey.Companion.CHILD_CHAT
import com.example.usedgoods.DBKey.Companion.DB_ARTICLES
import com.example.usedgoods.DBKey.Companion.DB_USERS
import com.example.usedgoods.R
import com.example.usedgoods.chatlist.ChatListItem
import com.example.usedgoods.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB : DatabaseReference
    private lateinit var articleAdapter: ArticleAdapter

    private val articleList = mutableListOf<ArticleModel>()
    private val listener = object : ChildEventListener {
        //데이터 가져오기
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            val articleModel = snapshot.getValue(ArticleModel::class.java)  //클래스 자체를 받아올 수 있음
            articleModel ?: return

            articleList.add(articleModel)
            articleAdapter.submitList(articleList)

        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

        override fun onCancelled(error: DatabaseError) {}

    }

    private var binding: FragmentHomeBinding? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)    //여기서만큼은 널이 아님
        binding = fragmentHomeBinding


        articleList.clear()
        userDB= Firebase.database.reference.child(DB_USERS)
        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        articleAdapter = ArticleAdapter(onItemClicked = {articleModel ->
            if(auth.currentUser != null){
                //로그인 상태 확인 (로그인상태)
                if(auth.currentUser?.uid != articleModel.sellerId){
                    val chatRoom = ChatListItem(
                        buyerId = auth.currentUser!!.uid,
                        sellerId = articleModel?.sellerId,
                        itemTitle = articleModel?.title,
                        key = System.currentTimeMillis()
                    )
                    userDB.child(auth.currentUser!!.uid)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)
                    userDB.child(articleModel.sellerId)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)

                    Snackbar.make(view, "채팅방이 생성되었습니다. 채팅탭에서 확인해주세요.", Snackbar.LENGTH_LONG).show()
                    
                }else{
                    Snackbar.make(view, "내가 올린 아이템입니다", Snackbar.LENGTH_LONG).show()
                }
            }else{
                //로그인을 안 한 상태
                Snackbar.make(view,"로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            }


       })

        //리스트어답터 연결하여 리사이클러뷰 연결(프라그먼트같은 경우 컨텍스트가 아님this->context)
        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        fragmentHomeBinding.addFloatingButton.setOnClickListener {
            context?.let{
                //로그인 기능 구현 필요
                if(auth.currentUser !=null){
                    val intent = Intent(requireContext(),  AddArticleActivity::class.java)//프라그먼트에서 컨텍스트 사용시 requireContext사용
                    startActivity(intent)
                }else{
                    Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
                }

            }


        }

        articleDB.addChildEventListener(listener)   //프라그먼트가 열릴때마다 호출하지않도록 전역변수로 리스너 구현


    }

    override fun onResume() {
        super.onResume()
        articleAdapter.notifyDataSetChanged()//뷰를 다시 그림
    }

    override fun onDestroy() {
        super.onDestroy()

        articleDB.removeEventListener(listener)
    }
}