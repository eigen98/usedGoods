package com.example.usedgoods

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.usedgoods.chatlist.ChatListFragment
import com.example.usedgoods.home.HomeFragment
import com.example.usedgoods.mypage.MyPageFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val homeFragment = HomeFragment()
        val chatListFragment = ChatListFragment()
        val myPageFragment = MyPageFragment()


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        //초기에 프래그먼트 attach
        replaceFragment(homeFragment)

        bottomNavigationView.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(homeFragment)
                R.id.chatList -> replaceFragment(chatListFragment)
                R.id.myPage -> replaceFragment(myPageFragment)
            }
            return@setOnNavigationItemReselectedListener
        }
    }

    private fun replaceFragment(fragment: Fragment) {   //주의androidx에서 임포트
        //액티비티에는 SupportFragmentManager라는 것이 있다. -> Activity에 attach되어있는 프래그먼트 관리
        supportFragmentManager.beginTransaction()//트랜잭션을 연다. (작업을 시작한다고 알려주는 기능->commit까지 작업진행)
            .apply {
                replace(R.id.fragmentContainer, fragment)
                commit()
            }
    }

}