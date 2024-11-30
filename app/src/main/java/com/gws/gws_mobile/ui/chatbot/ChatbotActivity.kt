package com.gws.gws_mobile.ui.chatbot

import android.content.Context
import android.os.Bundle
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        viewModel = ViewModelProvider(this).get(ChatbotViewModel::class.java)

        messageAdapter = MessageAdapter(mutableListOf())
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = messageAdapter

        viewModel.messageList.observe(this) { updatedMessageList ->
            messageAdapter.updateMessages(updatedMessageList)
            binding.recyclerView.scrollToPosition(updatedMessageList.size - 1)
        }

        loadModel()
        loadTrainingData()
        loadLemmatizer()
        loadChatHistory()

        binding.sendIcon.setOnClickListener {
            val inputText = binding.messageInput.text.toString()
            if (inputText.isNotEmpty()) {
                addMessageToViewModel(inputText, Message.SENDER_USER)
                val response = classify(inputText)
                addMessageToViewModel(response, Message.SENDER_BOT)
                binding.messageInput.text.clear()
            }
        }

        binding.removeChat.setOnClickListener {
            viewModel.clearMessages()
            deleteChatHistory()
        }

        binding.imageArrowLeft.setOnClickListener {
            finish()
            saveChatHistory()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveChatHistory()
        model.close()
    }

    private fun addMessageToViewModel(text: String, sender: Int) {
        val message = Message(text, sender)
        viewModel.addMessage(message)
    }

    private fun loadModel() {
        model = Model.newInstance(this)
    }

    private fun loadTrainingData() {
        val assetManager = assets
        val trainingDataPath = "training_data.json"
        val reader = InputStreamReader(assetManager.open(trainingDataPath))
        val gson = Gson()
        trainingData = gson.fromJson(reader, TrainingData::class.java)
        words = trainingData.words
        classes = trainingData.classes
    }

    private fun loadLemmatizer() {
        val dictionary = HashSet<String>()
        dictionary.addAll(words)
        lemmatizer = DefaultLemmatizer(dictionary)
    }

    private fun cleanUpSentence(sentence: String): List<String> {
        val sentenceWords = sentence.split(" ").map { it.lowercase() }
        return sentenceWords.map { lemmatizer.lemmatize(it) }
    }

    private fun bow(sentence: String): FloatArray {
        val sentenceWords = cleanUpSentence(sentence)
        val bag = FloatArray(words.size) { 0f }
        for (i in words.indices) {
            if (sentenceWords.contains(words[i])) {
                bag[i] = 1f
            }
        }
        return bag
    }

    private fun classify(sentence: String): String {
        val inputData = bow(sentence)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, words.size), DataType.FLOAT32)
        inputFeature0.loadArray(inputData)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        val results = outputFeature0.floatArray.mapIndexed { index, confidence ->
            index to confidence
        }.filter { it.second >= 0.70f }
            .sortedByDescending { it.second }

        return if (results.isNotEmpty()) {
            val predictedClass = classes[results[0].first]
            getResult(predictedClass)
        } else {
            "Sorry, I didn't understand that."
        }
    }

    private fun getResult(predictedClass: String): String {
        val assetManager = assets
        val dataset = "nlp_dataset_faq.json"
        val reader = InputStreamReader(assetManager.open(dataset))
        val gson = Gson()

        val jsonArray = gson.fromJson(reader, Array<JsonObject>::class.java)
        val match = jsonArray.find { it.get("tag").asString == predictedClass }

        return if (match != null) {
            val responses = match.getAsJsonArray("responses")

            val randomIndex = kotlin.random.Random.nextInt(responses.size())
            val randomResponse = responses[randomIndex].asString

            Log.d("RandomResponse", randomResponse)

            randomResponse
        } else {
            "Tag not found"
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
            Log.e("ChatbotActivity", "Error saving chat history", e)
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
            Log.d("ChatbotActivity", "No previous chat history found")
        } catch (e: Exception) {
            Log.e("ChatbotActivity", "Error loading chat history", e)
        }
    }

    private fun deleteChatHistory() {
        try {
            deleteFile("history_chat.json")
        } catch (e: Exception) {
            Log.e("ChatbotActivity", "Error deleting chat history", e)
        }
    }

    data class TrainingData(
        val words: List<String>,
        val classes: List<String>,
        val train_x: List<List<Float>>,
        val train_y: List<List<Float>>
    )
}
