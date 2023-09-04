package com.techno.tron.print

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.techno.tron.bluetoothprinter.R
import com.techno.tron.bluetoothprinter.databinding.ActivityMainBinding
import com.techno.tron.print.adapters.ProductQuantityAdapter
import com.techno.tron.print.utills.Util
import java.util.Date

class MainActivity : AppCompatActivity() {
    private var bitmap: Bitmap? =null
    private val courierCompanies = listOf("Blue Dart","FedEx","Delhivery","DTDC","Gati","Xpress Bee","Ecom","Shadow fax","Ecart","Indian Post","ETC")
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter:ProductQuantityAdapter
    private var textRecognizer: TextRecognizer? = null
    private var courier =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = ProductQuantityAdapter()
        binding.recyclerView.adapter = adapter
        binding.spinner.adapter = ArrayAdapter(this,R.layout.spinner_item,courierCompanies)
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        viewSetup()
    }

    private fun viewSetup(){
        with(binding) {
            tvDateAndTime.text = getString(R.string.date_and_time,Util.dateFormatter(Date()))
            buttonAddProduct.setOnClickListener { adapter.add() }

            imageView.setOnClickListener {
                cropActivityResultLauncher.launch(
                    CropImageContractOptions(
                        uri = null,
                        CropImageOptions(
                            imageSourceIncludeGallery = false,
                            imageSourceIncludeCamera = true
                        )
                    )
                )
            }

            btnReview.setOnClickListener {
                if(!binding.editTextInvoice.text.isNullOrBlank()) {
                    val print = getPrintableText()
                    val intent = Intent(this@MainActivity, PrintActivity::class.java).also {
                        it.type = "text/plain"
                        it.putExtra(Intent.EXTRA_TEXT, print)
                    }
                    startActivity(intent)
                }else{
                    binding.editTextInvoice.requestFocus()
                    Toast.makeText(this@MainActivity, "Please input invoice no...", Toast.LENGTH_SHORT).show()
                }
            }

            spinner.onItemSelectedListener =  object : OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    courier = courierCompanies[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    parent?.setSelection(0)
                }

            }
        }

    }


    private val cropActivityResultLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent
            binding.imageView.visibility = View.VISIBLE
            binding.imageView.setImageURI(uriContent)
            convertToText(uriContent)
        } else {
            // An error occurred.
            result.error?.message?.let { Log.e("CropImageActivity", it) }
        }
    }

    private fun convertToText(uri:Uri?){
        uri?.let {
            this.bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, it))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(this.contentResolver, it)
            }
        }
        if(bitmap != null) {
            textRecognizer?.process(InputImage.fromBitmap(bitmap!!, 0))
                ?.addOnSuccessListener {

                    binding.imageView.visibility = View.INVISIBLE
                    binding.tvForm.setText(toFormatted(it.text))
                    binding.tvForm.visibility = View.VISIBLE
                }?.addOnFailureListener {
                    Log.e("TAG", "err : $it")
                    Toast.makeText(applicationContext,"err: $it", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun toFormatted(str:String):String{
        val strBuilder = StringBuilder()
        val line = StringBuilder()
        str.split(" ").forEach{
            if(line.length + it.length > 31 ){
                strBuilder.append(line).append("\n")
                line.clear()
                line.append(it).append(" ")
            }else{
                line.append(it).append(" ")
            }
        }
        strBuilder.append(line)
        return strBuilder.trim().toString()
    }

    private fun getPrintableText():String{
        val strBuilder = StringBuilder()
        strBuilder.append(Util.charRepeater(" ",12)).append("INVOICE\n")
        strBuilder.append(Util.charRepeater("-")).append("\n")
        strBuilder.append(Util.charRepeater(" ",6)).append("DIGIFAST COURIER\n")
        strBuilder.append(Util.charRepeater("-")).append("\n")
        strBuilder.append(toFormatted("Bus stand market Shaktinagar Sonbhadra (U.P) 231202 Mob:7272919154")).append("\n")
        strBuilder.append(Util.charRepeater("-")).append("\n")
        strBuilder.append("invoice No : ${binding.editTextInvoice.text}\n")
        strBuilder.append("date : ${Util.dateFormatter(Date())}\n")
        strBuilder.append(Util.charRepeater("-")).append("\n")
        strBuilder.append("TO,\n")
        strBuilder.append(binding.tvForm.text).append("\n")
        strBuilder.append(Util.charRepeater("-")).append("\n")
        strBuilder.append("courier : $courier \n")
        strBuilder.append(adapter.toString())
        strBuilder.append(Util.charRepeater("-")).append("\n")
        strBuilder.append("total : ${adapter.getSubTotal()}\n")
        strBuilder.append(Util.charRepeater("-")).append("\n")
        return strBuilder.toString()
    }
}