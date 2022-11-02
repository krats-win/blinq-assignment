package com.example.blinqattempt1

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.blinqattempt1.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

const val INVITED_SHARED_PREF = "INVITED_SHARED_PREF"
const val SHARED_PREF = "SHARED_PREF"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private fun updateUI(invited: Boolean) {
        if (invited) {
            binding.tvSampleText.text =
                getString(R.string.inviteText)
            binding.btnReq.text = getString(R.string.cancelButtonText)
        } else {
            binding.tvSampleText.text = getString(R.string.cancelText)
            binding.btnReq.text = getString(R.string.inviteButtonText)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.setFragmentResultListener("requestKey", this) { _, _ ->
            val sharedPref = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
            val invited = sharedPref.getBoolean(INVITED_SHARED_PREF, false)
            runOnUiThread {
                updateUI(invited)
            }
        }

        val sharedPref = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
        val invited = sharedPref.getBoolean(INVITED_SHARED_PREF, false)
        updateUI(invited)

        binding.btnReq.setOnClickListener {
            val newInvited = sharedPref.getBoolean(INVITED_SHARED_PREF, false)
            if (newInvited) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Cancellation confirmation")
                    .setMessage("Are you sure you want to cancel your registration?")
                    .setNegativeButton("No") { _, _ -> }
                    .setPositiveButton("Yes") { _, _ ->
                        with(sharedPref.edit()) {
                            putBoolean(INVITED_SHARED_PREF, false)
                            apply()
                        }
                        MaterialAlertDialogBuilder(this)
                            .setTitle("Successfully cancelled invite")
                            .setMessage("Your registration has been cancelled successfully.")
                            .setPositiveButton("Okay") { _, _ -> }
                            .show()
                        updateUI(false)
                    }
                    .show()
            } else {
                showDialog()
            }
        }
    }

    private fun showDialog() {
        val fragmentManager = supportFragmentManager
        val newFragment = TakeUserInputFragment()

        val transaction = fragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit()
    }
}