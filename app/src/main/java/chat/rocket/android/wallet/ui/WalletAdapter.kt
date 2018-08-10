package chat.rocket.android.wallet.ui

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.textContent
import kotlinx.android.synthetic.main.item_transaction.view.*

class WalletAdapter(private val isManaged: Boolean) : RecyclerView.Adapter<WalletAdapter.ViewHolder>() {

    // Data set of transaction hashes
    var dataSet: MutableList<TransactionViewModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_transaction))

    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    fun updateTransactions(newTxs: List<TransactionViewModel>) {
        dataSet.clear()
        dataSet.addAll(newTxs)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(tx: TransactionViewModel) = with(itemView) {
            // Set the text of the UI elements
            if (isManaged) {
                text_hash.isVisible = false
            } else {
                text_hash.textContent = tx.txHash
            }
            if (tx.timestamp == "Pending...") {
                text_timestamp.setTextColor(Color.RED)
            } else {
                text_timestamp.setTextColor(Color.DKGRAY)
            }
            text_timestamp.textContent = tx.timestamp
            if (tx.outgoingTx) {
                text_amount.textContent = "- " + tx.value
                text_amount.setTextColor(resources.getColor(R.color.colorUserStatusBusy))
            } else {
                text_amount.textContent = "+ " + tx.value
                text_amount.setTextColor(resources.getColor(R.color.colorUserStatusOnline))
            }
        }
    }

}