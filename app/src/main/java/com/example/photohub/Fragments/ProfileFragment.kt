package com.example.photohub.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.photohub.AccountSettingsActivity
import com.example.photohub.Model.User
import com.example.photohub.R
import com.example.photohub.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "none").toString()
        }
        if (profileId == firebaseUser.uid) {
            binding.editAccountSettingsBtn.text = "Edit Profile"
        } else if (profileId == firebaseUser.uid) {
            checkFollowAndFollowingButtonStatus()
        }
        binding.editAccountSettingsBtn.setOnClickListener {
            val getButtonText = binding.editAccountSettingsBtn.text.toString()
            when{
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))
                getButtonText == "Follow" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }

                }
                getButtonText == "Following" -> {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()
                    }
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }

                }
            }
        }

        getFollowers()
        getFollowings()
        userInfo()

        return binding.root
    }

    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
        if (followingRef != null) {
            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.child(profileId).exists()) {
                        binding?.editAccountSettingsBtn?.text = "Following"
                    } else {
                        binding?.editAccountSettingsBtn?.text = "Follow"
                    }
                }

                override fun onCancelled(pO: DatabaseError) {

                }
            })
        }
    }

    private fun getFollowers() {
        val followersRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Followers")
        }
        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    binding?.totalFollowers?.text = pO.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


    }

    private fun getFollowings() {
        val followersRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }
        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(pO: DataSnapshot) {
                if (pO.exists()) {
                    binding?.totalFollowing?.text = pO.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)
        usersRef.addValueEventListener(object : ValueEventListener{
        override fun onDataChange(pO : DataSnapshot){
            if(context != null){
                return
        }
            if(pO.exists()){
                val user = pO.getValue<User>(User ::class.java)
                Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(binding?.proImageProfileFrag)
                binding?.profileFragmentUsername?.text = user!!.getUsername()
                binding?.fullNameProfileFrag?.text = user!!.getFullname()

            }
        }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
}
}