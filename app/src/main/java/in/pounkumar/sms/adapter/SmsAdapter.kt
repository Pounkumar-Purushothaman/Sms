package `in`.pounkumar.sms.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import `in`.pounkumar.sms.databinding.ItemSmsReceivedBinding
import `in`.pounkumar.sms.databinding.ItemSmsSentBinding
import `in`.pounkumar.sms.view.HomeActivity.Companion.decrypt
import `in`.pounkumar.sms.view.HomeActivity.SMSDetails
import java.text.SimpleDateFormat
import java.util.Date

class SmsAdapter : ListAdapter<SMSDetails, ViewHolder>(SmsDiffUtil()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == SENT) {
            SentViewHolder(ItemSmsSentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            ReceivedViewHolder(ItemSmsReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is SentViewHolder -> holder.bind(getItem(position))
            is ReceivedViewHolder -> holder.bind(getItem(position))
        }
    }

    class SentViewHolder(val binding: ItemSmsSentBinding) : ViewHolder(binding.root) {
        fun bind(item: SMSDetails) {
            binding.tvSmsContent.text = item.message.trim().decrypt()
            binding.tvTiming.text = item.time.convertToTime()
        }
    }

    class ReceivedViewHolder(val binding: ItemSmsReceivedBinding) : ViewHolder(binding.root) {
        fun bind(item: SMSDetails) {
            binding.tvSmsContent.text = item.message.trim().decrypt()
            binding.tvTiming.text = item.time.convertToTime()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isSent) {
            SENT
        } else {
            RECEIVED
        }
    }

    class SmsDiffUtil : ItemCallback<SMSDetails>() {
        override fun areItemsTheSame(oldItem: SMSDetails, newItem: SMSDetails): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SMSDetails, newItem: SMSDetails): Boolean {
            return oldItem.id == newItem.id
        }
    }

    companion object {
        private const val SENT = 1
        private const val RECEIVED = 2
    }
}

@SuppressLint("SimpleDateFormat")
private fun String.convertToTime(): String {
    return SimpleDateFormat("dd/MM/yyyy hh:mm a").format(Date(this.toLong()))
}