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
import jsastrawi.morphology.DefaultLemmatizer
import jsastrawi.morphology.Lemmatizer
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import kotlin.math.sqrt

class ChatbotActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatbotViewModel
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var vocabulary: List<String>
    private lateinit var intents: List<Intent>
    private lateinit var lemmatizer: Lemmatizer

    private lateinit var binding: ActivityChatbotBinding

    private var detectedTag: String? = null
    private var currentPatterns: List<String>? = null
    private var isManualInput: Boolean = false

    // Stopwords bahasa Indonesia
    private val ignoreWords = setOf(
        "", "!", "\"", "'", "(", ")", ",", "-", ".", ":", ";", "[", "]", "_", "?",
        "adalah", "akan", "aku", "anda", "atau", "dalam", "dan", "dari", "dengan",
        "di", "harus", "ini", "itu", "jika", "kami", "kamu", "ke", "kita", "mereka",
        "oleh", "pada", "saya", "sebuah", "sedang", "sementara", "tanpa", "tapi",
        "telah", "untuk", "yang", "{", "}", "merasa"
    )

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
            val response = classifyWithCosineSimilarity(inputText)
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
        loadIntentsAndVocabulary()
        loadLemmatizer()
    }

    private fun loadIntentsAndVocabulary() {
        val dataset = "nlp_dataset_faq.json"
        val reader = InputStreamReader(assets.open(dataset))
        val gson = Gson()

        val jsonArray = gson.fromJson(reader, Array<JsonObject>::class.java)

        // Load intents
        intents = jsonArray.map { jsonObject ->
            val tag = jsonObject.get("tag").asString
            val patterns = jsonObject.getAsJsonArray("patterns").map { it.asString }
            val responses = jsonObject.getAsJsonArray("responses").map { it.asString }
            Intent(tag, patterns, responses)
        }

        // Build vocabulary from all patterns
        val allWords = mutableSetOf<String>()
        intents.forEach { intent ->
            intent.patterns.forEach { pattern ->
                val words = tokenize(pattern)
                allWords.addAll(words)
            }
        }
        vocabulary = allWords.sorted()
    }

    private fun loadLemmatizer() {
        val dictionary = HashSet(vocabulary)
        lemmatizer = DefaultLemmatizer(dictionary)
    }

    // Simple Indonesian stemming
    private fun stemWord(word: String): String {
        var stemmed = word.lowercase()

        // Remove prefixes
        val prefixes = listOf("me", "di", "ke", "pe", "ter", "ber", "se")
        for (prefix in prefixes) {
            if (stemmed.startsWith(prefix) && stemmed.length > prefix.length + 2) {
                stemmed = stemmed.substring(prefix.length)
                break
            }
        }

        // Remove suffixes
        val suffixes = listOf("kan", "an", "i", "nya")
        for (suffix in suffixes) {
            if (stemmed.endsWith(suffix) && stemmed.length > suffix.length + 2) {
                stemmed = stemmed.substring(0, stemmed.length - suffix.length)
                break
            }
        }

        return stemmed
    }

    private fun tokenize(sentence: String): List<String> {
        // Remove punctuation and convert to lowercase
        val cleaned = sentence.replace(Regex("[^\\w\\s]"), " ")

        // Split and filter
        return cleaned.split(Regex("\\s+"))
            .map { it.lowercase() }
            .filter { it.isNotEmpty() && !ignoreWords.contains(it) }
            .map { stemWord(it) }
    }

    private fun createBagOfWords(sentence: String): FloatArray {
        val sentenceWords = tokenize(sentence)
        return FloatArray(vocabulary.size) { index ->
            if (sentenceWords.contains(vocabulary[index])) 1f else 0f
        }
    }

    private fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dotProduct = 0f
        var mag1 = 0f
        var mag2 = 0f

        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            mag1 += vec1[i] * vec1[i]
            mag2 += vec2[i] * vec2[i]
        }

        mag1 = sqrt(mag1)
        mag2 = sqrt(mag2)

        return if (mag1 == 0f || mag2 == 0f) 0f else dotProduct / (mag1 * mag2)
    }

    private fun classifyWithCosineSimilarity(sentence: String): String {
        val inputBag = createBagOfWords(sentence)
        val scores = mutableListOf<Pair<String, Float>>()

        intents.forEach { intent ->
            var maxScore = 0f

            intent.patterns.forEach { pattern ->
                val patternBag = createBagOfWords(pattern)
                val similarity = cosineSimilarity(inputBag, patternBag)
                if (similarity > maxScore) {
                    maxScore = similarity
                }
            }

            if (maxScore > 0.3f) { // Threshold
                scores.add(Pair(intent.tag, maxScore))
            }
        }

        scores.sortByDescending { it.second }

        return if (scores.isNotEmpty()) {
            detectedTag = scores[0].first
            detectedTag ?: "unknown"
        } else {
            "unknown"
        }
    }

    private fun setupDropdown(tag: String) {
        val match = intents.find { it.tag == tag }

        if (match != null) {
            currentPatterns = match.patterns

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
        val match = intents.find { it.tag == detectedTag }

        return if (match != null) {
            match.responses.random()
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
            e.printStackTrace()
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
            // File doesn't exist yet, that's okay
        } catch (e: Exception) {
            e.printStackTrace()
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
            e.printStackTrace()
        }
    }

    // Data class untuk Intent
    data class Intent(
        val tag: String,
        val patterns: List<String>,
        val responses: List<String>
    )
}