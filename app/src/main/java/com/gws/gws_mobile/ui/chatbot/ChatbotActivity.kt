package com.gws.gws_mobile.ui.chatbot

import android.R
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.gws.gws_mobile.databinding.ActivityChatbotBinding
import com.gws.gws_mobile.ml.Model
import jsastrawi.morphology.DefaultLemmatizer
import jsastrawi.morphology.Lemmatizer
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader

class ChatbotActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatbotViewModel
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var model: Model
    private lateinit var words: List<String>
    private lateinit var classes: List<String>
    private lateinit var trainingData: TrainingData
    private lateinit var lemmatizer: Lemmatizer

    private lateinit var binding: ActivityChatbotBinding

    private var detectedTag: String? = null
    private var currentPatterns: List<String>? = null
    private var isManualInput: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this).get(ChatbotViewModel::class.java)

        setupRecyclerView()
        observeViewModel()

        loadResources()
        loadChatHistory()

        binding.sendIcon.setOnClickListener { handleSendClick() }
        binding.removeChat.setOnClickListener { clearChatHistory() }
        binding.imageArrowLeft.setOnClickListener { exitChat() }

        binding.dropdownSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                handleDropdownSelection(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action needed
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveChatHistory()
        model.close()
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(mutableListOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = messageAdapter
    }

    private fun observeViewModel() {
        viewModel.messageList.observe(this) { updatedMessageList ->
            messageAdapter.updateMessages(updatedMessageList)
            binding.recyclerView.scrollToPosition(updatedMessageList.size - 1)
        }
    }

    private fun handleSendClick() {
        val inputText = binding.messageInput.text.toString()
        if (inputText.isNotEmpty()) {
            isManualInput = true
            addMessageToViewModel(inputText, Message.SENDER_USER)
            val response = classify(inputText)
            setupDropdown(response)
            binding.messageInput.text.clear()
        }
    }

    private fun handleDropdownSelection(position: Int) {
        currentPatterns?.let {
            val selectedPattern = it[position]
            if (!isManualInput) addMessageToViewModel(selectedPattern, Message.SENDER_USER)
            val botResponse = getResponseForPattern()
            addMessageToViewModel(botResponse, Message.SENDER_BOT)
            isManualInput = false
        }
    }

    private fun exitChat() {
        finish()
        saveChatHistory()
    }

    private fun addMessageToViewModel(text: String, sender: Int) {
        val message = Message(text, sender)
        viewModel.addMessage(message)
    }

    private fun loadResources() {
        loadModel()
        loadTrainingData()
        loadLemmatizer()
    }

    private fun loadModel() {
        model = Model.newInstance(this)
    }

    private fun loadTrainingData() {
        val trainingDataPath = "training_data.json"
        val reader = InputStreamReader(assets.open(trainingDataPath))
        val gson = Gson()
        trainingData = gson.fromJson(reader, TrainingData::class.java)
        words = trainingData.words
        classes = trainingData.classes
    }

    private fun loadLemmatizer() {
        val dictionary = HashSet(words)
        lemmatizer = DefaultLemmatizer(dictionary)
    }

    private fun cleanUpSentence(sentence: String): List<String> {
        // Remove all non-alphabetic characters using regex
        val cleanedSentence = sentence.replace(Regex("[^A-Za-z\\s]"), "")
        val sentenceWords = cleanedSentence.split(" ").map { it.lowercase() }
        return sentenceWords.map { lemmatizer.lemmatize(it) }
    }


    private fun bow(sentence: String): FloatArray {
        val sentenceWords = cleanUpSentence(sentence)
        return FloatArray(words.size) { if (sentenceWords.contains(words[it])) 1f else 0f }
    }

    private fun classify(sentence: String): String {
        val inputData = bow(sentence)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, words.size), DataType.FLOAT32)
        inputFeature0.loadArray(inputData)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val results = outputFeature0.floatArray.mapIndexed { index, confidence ->
            index to confidence
        }.filter { it.second >= 0.40f }
            .sortedByDescending { it.second }

        return if (results.isNotEmpty()) {
            detectedTag = classes[results[0].first]
            detectedTag ?: "unknown"
        } else {
            "unknown"
        }
    }

    private fun setupDropdown(tag: String) {
        val dataset = "nlp_dataset_faq.json"
        val reader = InputStreamReader(assets.open(dataset))
        val gson = Gson()

        val jsonArray = gson.fromJson(reader, Array<JsonObject>::class.java)
        val match = jsonArray.find { it.get("tag").asString == tag }

        if (match != null) {
            val patterns = match.getAsJsonArray("patterns")
            currentPatterns = patterns.map { it.asString }

            val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, currentPatterns!!)
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            binding.dropdownSpinner.adapter = adapter
            binding.floatingDropdownCard.visibility = View.VISIBLE
        } else {
            binding.floatingDropdownCard.visibility = View.GONE
            addMessageToViewModel("Maaf aku belum mengerti, bisa bicarakan hal lain?", Message.SENDER_BOT)
        }
    }

    private fun getResponseForPattern(): String {
        val dataset = "nlp_dataset_faq.json"
        val reader = InputStreamReader(assets.open(dataset))
        val gson = Gson()

        val jsonArray = gson.fromJson(reader, Array<JsonObject>::class.java)
        val match = jsonArray.find { it.get("tag").asString == detectedTag }

        return if (match != null) {
            val responses = match.getAsJsonArray("responses")
            responses.map { it.asString }.random()
        } else {
            "Sorry, I don't have an answer for that."
        }
    }

    private fun saveChatHistory() {
        try {
            val history = viewModel.messageList.value ?: mutableListOf()
            val gson = Gson()
            val json = gson.toJson(history)
            val fileOutputStream = openFileOutput("history_chat.json", Context.MODE_PRIVATE)
            fileOutputStream.write(json.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
        }
    }

    private fun loadChatHistory() {
        try {
            val fileInputStream: FileInputStream = openFileInput("history_chat.json")
            val json = fileInputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            val history = gson.fromJson(json, Array<Message>::class.java).toMutableList()
            viewModel.restoreMessages(history)
        } catch (e: FileNotFoundException) {
        } catch (e: Exception) {
        }
    }

    private fun clearChatHistory() {
        viewModel.clearMessages()
        deleteChatHistory()
        binding.floatingDropdownCard.visibility = View.GONE
    }

    private fun deleteChatHistory() {
        try {
            deleteFile("history_chat.json")
        } catch (e: Exception) {
        }
    }

    data class TrainingData(
        val words: List<String>,
        val classes: List<String>,
        val train_x: List<List<Float>>,
        val train_y: List<List<Float>>
    )
}
