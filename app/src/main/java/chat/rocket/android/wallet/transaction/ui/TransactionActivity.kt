package chat.rocket.android.wallet.transaction.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.util.extensions.addFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject
import kotlinx.android.synthetic.main.app_bar_transaction.*

class TransactionActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        setupToolbar()
        addFragment("TransactionFragment")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setupResultAndFinish("", 0.0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onNavigateUp()
    }

    fun setupResultAndFinish(recipient: String, amount: Double) {
        if (recipient.isEmpty() || amount <= 0.0) {
            setResult(Activity.RESULT_CANCELED)
        }
        else {
            var result = Intent()
            result.putExtra("recipientId", recipient)
            result.putExtra("amount", amount)
            setResult(Activity.RESULT_OK, result)
        }
        finish()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = fragmentDispatchingAndroidInjector

    private fun addFragment(tag: String) {
        addFragment(tag, R.id.fragment_container) {
            TransactionFragment.newInstance()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
    }
}