package `in`.pounkumar.sms.view

import android.content.ContentValues
import android.os.Bundle
import android.provider.Telephony
import android.provider.Telephony.Sms.Inbox
import android.util.Base64
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import `in`.pounkumar.sms.adapter.SmsAdapter
import `in`.pounkumar.sms.databinding.ActivityHomeBinding
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class HomeActivity : AppCompatActivity() {
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!

    val smsAdapter by lazy { SmsAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.rvSent.apply {
            adapter = smsAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity).apply { reverseLayout = true }
        }
        getAllSmsFromProvider()
        binding.btnSend.setOnClickListener {
            sendSms()
        }
        binding.etSmsContent.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendSms()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun sendSms() {
        binding.etSmsContent.apply {
            if (text.toString().isNotEmpty()) {
                insert(text.toString(), true)
                insert(text.toString(), false)
                this.setText("")
                getAllSmsFromProvider()
            }
        }
    }

    private fun insert(smsContent: String, isSentType: Boolean) {
        ContentValues().apply {
            put("address", if (isSentType) SENT_TYPE else RECEIVED_TYPE)
            put("body", smsContent.trim().encrypt())
            put("date", System.currentTimeMillis().toString())
            put("read", "1")
            put("type", if (isSentType) "0" else "1")
            contentResolver.insert(Telephony.Sms.CONTENT_URI, this).toString()
        }
    }

    private fun getAllSmsFromProvider(): MutableList<SMSDetails> {
        val smsList: MutableList<SMSDetails> = ArrayList()
        val cursor = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, Inbox.DEFAULT_SORT_ORDER)
        if (cursor?.moveToFirst() == true) {
            try {
                do {
                    SMSDetails().apply {
                        id = cursor.getString(cursor.getColumnIndexOrThrow("_id"))
                        address = cursor.getString(cursor.getColumnIndexOrThrow("address"))
                        message = cursor.getString(cursor.getColumnIndexOrThrow("body"))
                        readState = cursor.getString(cursor.getColumnIndexOrThrow("read"))
                        time = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                        isSent = cursor.getString(cursor.getColumnIndexOrThrow("type")).contains("0")
                        smsList.add(this)
                    }
                } while (cursor.moveToNext())
            } catch (e: Exception) {

            }
        } else {
            Log.e("Sms", "You have no SMS in Inbox")
        }
        cursor?.close()
        smsAdapter.submitList(smsList.filter { it.address == SENT_TYPE || it.address == RECEIVED_TYPE })
        return smsList
    }


    data class SMSDetails(
        var id: String = "",
        var address: String = "",
        var message: String = "",
        var readState: String = "",
        var time: String = "",
        var isSent: Boolean = false
    )

    companion object {
        private const val SENT_TYPE = "98765432101"
        private const val RECEIVED_TYPE = "90439741341"
        private const val algorithm = "AES/CBC/PKCS5Padding"
        private val key = SecretKeySpec("1234567890123456".toByteArray(), "AES")
        private val iv = IvParameterSpec(ByteArray(16))

        fun String.encrypt(): String {
            val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val cipherText = cipher.doFinal(this.toByteArray(charset("UTF-8")))
            return Base64.encodeToString(cipherText, Base64.NO_WRAP)
        }

        fun String.decrypt(): String {
            val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decode: ByteArray = Base64.decode(this, Base64.NO_WRAP)
            return String(cipher.doFinal(decode), charset("UTF-8"))
        }
    }
}