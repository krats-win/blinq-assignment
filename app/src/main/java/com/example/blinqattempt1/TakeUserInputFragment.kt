package com.example.blinqattempt1

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns.EMAIL_ADDRESS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.example.blinqattempt1.databinding.FragmentTakeUserInputBinding
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class TakeUserInputFragment : DialogFragment() {
    private lateinit var binding: FragmentTakeUserInputBinding
    private var viewContainer: ViewGroup? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewContainer = container
        binding = FragmentTakeUserInputBinding.inflate(inflater, container, false)

        return binding.root
    }

    private val client = OkHttpClient()
    private val url = "https://us-central1-blinkapp-684c1.cloudfunctions.net/fakeAuth"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submitForm.setOnClickListener {

            var valid = true
            if (binding.etName.length() < 3) {
                binding.etName.error = "Please enter at least 3 characters!"
                valid = false
            }

            if (!isValidEmail(binding.etEmail.text.toString())) {
                binding.etEmail.error = "Please enter a valid email address!"
                valid = false
            }

            if (binding.etEmail.text.toString() != binding.etEmailConfirm.text.toString()) {
                binding.etEmailConfirm.error = "Emails do not match!"
                valid = false
            }

            if (valid) {
                val request =
                    makeApiRequest(binding.etName.text.toString(), binding.etEmail.text.toString())

                if (request != null) {
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.i("DIALOG", "onFailure")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            Log.i("DIALOG", response.body().toString())
                            Log.i("DIALOG", "HTTP STATUS CODE: ${response.code()}")

                            val sharedPref =
                                view.context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
                                    ?: return

                            if (response.code() == 200) {
                                Log.i("DIALOG", "onSuccess")

                                activity?.runOnUiThread {
                                    CommonConfetti.rainingConfetti(
                                        viewContainer, intArrayOf(
                                            Color.GREEN, Color.RED, Color.MAGENTA
                                        )
                                    ).oneShot()

                                    MaterialAlertDialogBuilder(view.context)
                                        .setTitle("Congratulations")
                                        .setMessage("You request has been received successfully.")
                                        .setPositiveButton("Okay") { _, _ -> }
                                        .show()
                                }

                                with(sharedPref.edit()) {
                                    putBoolean(INVITED_SHARED_PREF, true)
                                    apply()
                                }
                                setFragmentResult("requestKey", bundleOf())
                                fragmentManager?.popBackStack()

                            } else if (response.code() == 400) {
                                Log.i("DIALOG", "onFailure")

                                activity!!.runOnUiThread {
                                    MaterialAlertDialogBuilder(view.context)
                                        .setTitle("Request failed")
                                        .setMessage("Error. Please try again later.")
                                        .setPositiveButton("Okay") { _, _ -> }
                                        .show()
                                }

                            }
                        }
                    })
                }
            }
        }
    }

    private fun makeApiRequest(name: String, email: String): Request? {
        val mediaType = MediaType.parse("application/json; charset=utf-8")
        val json = JSONObject()
        json.put("name", name)
        json.put("email", email)
//        json.put("name", "name")
//        json.put("email", "email@gmail.com")
//        json.put("email", "usedemail@blinq.app")

        val postBody = RequestBody.create(mediaType, json.toString())
        return Request.Builder().url(url).post(postBody).build()
    }

    private fun isValidEmail(email: String): Boolean {
        return if (TextUtils.isEmpty(email)) {
            false
        } else {
            EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            val inflater = requireActivity().layoutInflater

            builder.setView(inflater.inflate(R.layout.fragment_take_user_input, null))

            val dialog = builder.create()
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            return dialog

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}