package com.example.livestate.ui.currency

import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import com.example.livestate.R
import com.example.livestate.base.BaseActivity
import com.example.livestate.databinding.ActivityCurrencyBinding
import com.example.livestate.entity.RetrofitInstance
import com.example.livestate.ui.currency.OnCurrencyClickListener
import com.example.livestate.widget.gone
import com.example.livestate.widget.tap
import com.example.livestate.widget.visible
import kotlinx.coroutines.CoroutineScope
import com.example.livestate.model.Currency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class CurrencyActivity : BaseActivity<ActivityCurrencyBinding>() {

    private var switchSoundCheck = ""
    private var switchVibrationCheck = ""
    private var precision = 0
    private var decimalSeparator = 0
    private var thousandsSeparator = 0
    private lateinit var currencyAdapter: CurrencyAdapter
    private var checkCurrency: String? = ""
    private var rateFrom: Double? = 0.0
    private var rateTo: Double? = 0.0
    private var dataTime: String? = ""

    private val sharedPreferences by lazy {
        getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    override fun setViewBinding() = ActivityCurrencyBinding.inflate(layoutInflater)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        binding.edtAmount.showSoftInputOnFocus = true
        binding.edtAmount.requestFocus()

        sharedPreferences.edit().putString("fromCurrency", "AUD").apply()
        sharedPreferences.edit().putString("toCurrency", "USD").apply()
        sharedPreferences.edit().putInt("fromCurrencyId", 1).apply()
        sharedPreferences.edit().putInt("toCurrencyId", 0).apply()

        binding.tvTopCurrency.text = "AUD"
        binding.tvCurrencyTop.text = "AUD"
        binding.tvCenterCurrency.text = "USD"
        binding.tvCurrencyCenter.text = "USD"

        setNumberButtonClickListeners()
        initCurrencyView()
    }

    private fun initCurrencyView() {
        val initialList = getInitialCurrencyList()
        fetchCurrencyData(initialList)
        binding.edtAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                equal() // Tự động tính lại khi thay đổi số
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        currencyAdapter = CurrencyAdapter(initialList, object : OnCurrencyClickListener {
            override fun onItemClick(currency: Currency) {
                binding.editTextSearch.clearFocus()
                binding.editTextSearch.text = null
                if (checkCurrency == "fromCurrency") {
                    sharedPreferences.edit().putString("fromCurrency", currency.contentDisplay).apply()
                    sharedPreferences.edit().putInt("fromCurrencyId", currency.id).apply()
                    rateFrom = currency.rate
                    binding.tvTopCurrency.text = currency.contentDisplay
                    binding.tvCurrencyTop.text = currency.contentDisplay
                } else {
                    sharedPreferences.edit().putString("toCurrency", currency.contentDisplay).apply()
                    sharedPreferences.edit().putInt("toCurrencyId", currency.id).apply()
                    rateTo = currency.rate
                    binding.tvCenterCurrency.text = currency.contentDisplay
                    binding.tvCurrencyCenter.text = currency.contentDisplay
                }
            }
        })

        binding.rvOptionCurrency.adapter = currencyAdapter



        binding.ivCloseBottomSheet.tap {
            binding.clBottomSheet.gone()
        }

        binding.clTopCurrency.tap {
            checkCurrency = "fromCurrency"
            val fromId = sharedPreferences.getInt("fromCurrencyId", 1)
            currencyAdapter.updateSelection(fromId)
            binding.clBottomSheet.visible()
        }

        binding.clCenterCurrency.tap {
            checkCurrency = "toCurrency"
            val toId = sharedPreferences.getInt("toCurrencyId", 1)
            currencyAdapter.updateSelection(toId)
            binding.clBottomSheet.visible()
        }

        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currencyAdapter.filterList(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.editTextSearch.setOnFocusChangeListener { _, hasFocus ->
            binding.editTextSearch.setHint(if (hasFocus) "" else getString(R.string.search))
        }

        binding.ivBack.tap { finish() }
    }

    private fun getInitialCurrencyList(): MutableList<Currency> {
        return mutableListOf(
            Currency(0, "USD", "United States Dollar USD", 1.0, false),
            Currency(1, "AUD", "Australian Dollar AUD", 1.0, false),
            Currency(2, "BGN", "Bulgarian Lev BGN", 1.0, false),
            Currency(3, "BRL", "Brazilian Real BRL", 1.0, false),
            Currency(4, "CAD", "Canadian Dollar CAD", 1.0, false),
            Currency(5, "CHF", "Swiss Franc CHF", 1.0, false),
            Currency(6, "CNY", "Chinese Yuan CNY", 1.0, false),
            Currency(7, "CZK", "Czech Koruna CZK", 1.0, false),
            Currency(8, "DKK", "Danish Krone DKK", 1.0, false),
            Currency(9, "EUR", "Euro EUR", 1.0, false),
            Currency(10, "GBP", "British Pound GBP", 1.0, false),
            Currency(11, "HKD", "Hong Kong Dollar HKD", 1.0, false),
            Currency(12, "HUF", "Hungarian Forint HUF", 1.0, false),
            Currency(13, "IDR", "Indonesian Rupiah IDR", 1.0, false),
            Currency(14, "ILS", "Israeli New Shekel ILS", 1.0, false),
            Currency(15, "INR", "Indian Rupee INR", 1.0, false),
            Currency(16, "ISK", "Icelandic Krona ISK", 1.0, false),
            Currency(17, "JPY", "Japanese Yen JPY", 1.0, false),
            Currency(18, "KRW", "South Korean Won KRW", 1.0, false),
            Currency(19, "MXN", "Mexican Peso MXN", 1.0, false),
            Currency(20, "MYR", "Malaysian Ringgit MYR", 1.0, false),
            Currency(21, "NOK", "Norwegian Krone NOK", 1.0, false),
            Currency(22, "NZD", "New Zealand Dollar NZD", 1.0, false),
            Currency(23, "PHP", "Philippine Peso PHP", 1.0, false),
            Currency(24, "PLN", "Polish Zloty PLN", 1.0, false),
            Currency(25, "RON", "Romanian Leu RON", 1.0, false),
            Currency(26, "SEK", "Swedish Krona SEK", 1.0, false),
            Currency(27, "SGD", "Singapore Dollar SGD", 1.0, false),
            Currency(28, "THB", "Thai Baht THB", 1.0, false),
            Currency(29, "TRY", "Turkish Lira TRY", 1.0, false),
            Currency(30, "ZAR", "South African Rand ZAR", 1.0, false)
        )
    }

    private fun equal() {
        val amount = binding.edtAmount.text.toString().toFloatOrNull()
        if (amount == null) {
            Toast.makeText(this, getString(R.string.please_enter_a_valid_number), Toast.LENGTH_SHORT).show()
        } else {
            val result = (amount / rateFrom!! * rateTo!!)
            binding.tvResult.text = getFormatNumber(result, precision, decimalSeparator, thousandsSeparator)
        }
    }

    private fun fetchCurrencyData(initialList: MutableList<Currency>) {
        CoroutineScope(Dispatchers.Main).launch {
            val response = RetrofitInstance.api.getCurrencyRates()
            withContext(Dispatchers.Main) {
                response?.let {
                    val updatedList = initialList.map { currency ->
                        val rate = response.rates[currency.contentDisplay] ?: 1.0
                        currency.copy(rate = rate)
                    }
                    val timestamp = response.timestamp
                    val date = Date(timestamp * 1000L)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dataTime = getString(R.string.update) + " ${dateFormat.format(date)}"
                    currencyAdapter.updateList(updatedList.toMutableList())
                    binding.tvDate.text = dataTime
                    rateTo = 1.0
                    rateFrom = response.rates["AUD"] ?: 1.0
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun click() {
        if (switchVibrationCheck == "Selected") vibratePhone(this)
        if (switchSoundCheck == "Selected") SoundClick(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNumberButtonClickListeners() {


    }

    override fun onStart() {
        super.onStart()
        precision = sharedPreferences.getInt("clLimitPrecision", 1)
        decimalSeparator = sharedPreferences.getInt("clDecimalSeparator", 0)
        thousandsSeparator = sharedPreferences.getInt("clThousandsSeparator", 0)
        switchSoundCheck = sharedPreferences.getString("switchSound", "") ?: "noSelected"
        switchVibrationCheck = sharedPreferences.getString("switchVibration", "") ?: "noSelected"
    }

    override fun viewListener() {}
    override fun dataObservable() {}
}
