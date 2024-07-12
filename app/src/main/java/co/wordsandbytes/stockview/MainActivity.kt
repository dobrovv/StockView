package co.wordsandbytes.stockview

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import co.wordsandbytes.stockview.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    //private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var btnGenerate : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        btnGenerate = findViewById(R.id.btnGenerate)
        btnGenerate.setOnClickListener() {
            compassRotate()
        }
    }

    fun compassRotate() {
        val imageView = findViewById<View>(R.id.imageView) as ImageView
        val textView = findViewById<TextView>(R.id.textView)

        val  background = BitmapFactory.decodeResource(getResources(), R.drawable.compass)
        val  image = BitmapFactory.decodeResource(getResources(), R.drawable.compass_needle)

        val rndAngle = (0..359).random()

        val spanString = SpannableString("%d °".format(rndAngle))
        spanString.setSpan(StyleSpan(Typeface.BOLD), 0, spanString.length, 0)
        textView.text = spanString

        val bmpImages = overlayBitmap(background, image, rndAngle.toFloat())
        imageView.setImageBitmap(bmpImages)
    }

    fun overlayBitmap(bitmapBackground: Bitmap, bitmapImage: Bitmap, angle: Float): Bitmap {
        val bitmap1Width = bitmapBackground.getWidth()
        val bitmap1Height = bitmapBackground.getHeight()
        val bitmap2Width = bitmapImage.getWidth()
        val bitmap2Height = bitmapImage.getHeight()
        val marginLeft = (bitmap1Width * 0.5 - bitmap2Width * 0.5).toFloat()
        val marginTop = (bitmap1Height * 0.5 - bitmap2Height * 0.5).toFloat()
        val overlayBitmap =
            Bitmap.createBitmap(bitmap1Width, bitmap1Height, bitmapBackground.getConfig())
        val canvas = Canvas(overlayBitmap)
        canvas.drawBitmap(bitmapBackground, Matrix(), null)
        canvas.translate(bitmap1Width.toFloat()/2f, bitmap1Height.toFloat()/2f)
        canvas.rotate(angle)
        canvas.drawBitmap(bitmapImage, null, RectF(-bitmap1Width.toFloat()/2,  -bitmap1Height.toFloat()/2, bitmap1Width.toFloat()/2,bitmap1Height.toFloat()/2), null)

        return overlayBitmap
    }

    public override fun onResume() {
        super.onResume()

        val myWebView = findViewById<WebView>(R.id.myWebView);
        val myWebViewWord = findViewById<WebView>(R.id.myWebViewWord);
        val webSettings = myWebView.getSettings()
        webSettings.javaScriptEnabled = true
        //webSettings.useWideViewPort = true
        //webSettings.loadWithOverviewMode = true
        webSettings.domStorageEnabled = true

        val webSettingsWord = myWebViewWord.getSettings();
        webSettingsWord.javaScriptEnabled = true;
        webSettingsWord.domStorageEnabled = true;

        myWebView.setVisibility(View.GONE)
        myWebViewWord.visibility = View.GONE;


        myWebView.loadUrl("https://www.cnn.com/markets/fear-and-greed")

        /* Js cleanup code to adapt the cnn stock data:
            const cListNews = document.querySelector(".tabcontent.active");
            document.body.innerHTML = "";
            document.body.appendChild(cListNews);

            //removes previous and 1 year ago data
            document.querySelector(".market-fng-gauge__historical").remove();
            //removes indicator descriptions
            document.querySelectorAll(".market-fng-indicator__text").forEach(node => node.remove());
            //removes FAQ
            document.querySelector(".market-faq").remove();
        */

        val jsCleanupScript =
            //"alert(\"Hi!\");" +
            "const cListNews = document.querySelector(\".tabcontent.active\");" +
                    "document.body.innerHTML = \"\";" +
                    "document.body.appendChild(cListNews);" +
                    "document.querySelector(\".market-fng-gauge__historical\").remove();" +
                    "document.querySelectorAll(\".market-fng-indicator__text\").forEach(node => node.remove());" +
                    "document.querySelector(\".market-faq\").remove();"

        // method #1 - onPageFinished
//        myWebView.setWebViewClient(object : WebViewClient() {
//            override fun onPageFinished(view: WebView, url: String) {
//                myWebView.evaluateJavascript(jsCleanupScript, null);
//            }
//
//            override fun onPageCommitVisible(view: WebView?, url: String?) {
//                myWebView.evaluateJavascript(jsCleanupScript, null);
//            }
//        })



        // method #2 - progress bar
        myWebView.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                Log.e("progress", "" + progress)

                if (progress >= 80 && myWebView.visibility != View.VISIBLE) {
                    myWebView.visibility = View.VISIBLE;
                }

                if (progress >= 90) {
                    // TODO - Add whatever code you need here based on web page load completion...
                    myWebView.evaluateJavascript(jsCleanupScript, null);
                }
            }
        })

//       // Method #3
//        val timer = object: CountDownTimer(3000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {}
//
//            override fun onFinish() {
//                myWebView.evaluateJavascript(jsCleanupScript, null);
//            }
//        }
//        timer.start()


        val jsCleanupScriptWord =
            "const cListNews = document.querySelector(\"article\");" +
                    "document.body.innerHTML = \"\";" +
                    "document.body.appendChild(cListNews);";

        myWebViewWord.loadUrl("https://www.nytimes.com/column/learning-word-of-the-day");
        myWebViewWord.setWebViewClient(object : WebViewClient() {

//            override fun onPageFinished(view: WebView, url: String) {
//                myWebViewWord.visibility = View.VISIBLE;
//                myWebViewWord.evaluateJavascript(jsCleanupScriptWord, null);
//            }

            // Open url links in external browser
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, request.url)
                view.context.startActivity(intent)
                return true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                myWebViewWord.visibility = View.VISIBLE;
                myWebViewWord.evaluateJavascript(jsCleanupScriptWord, null);
            }
        })


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}