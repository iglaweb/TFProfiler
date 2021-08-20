package ru.igla.tfprofiler.tflite_runners.text_classification

import android.content.Context
import org.tensorflow.lite.support.metadata.MetadataExtractor
import ru.igla.tfprofiler.core.tflite.TensorFlowUtils
import ru.igla.tfprofiler.models_list.ModelEntity
import ru.igla.tfprofiler.tflite_runners.base.ModelOptions
import ru.igla.tfprofiler.tflite_runners.base.TFLiteTextDetectAPIModelBase
import ru.igla.tfprofiler.tflite_runners.domain.Label
import ru.igla.tfprofiler.tflite_runners.domain.TextRecognition
import ru.igla.tfprofiler.utils.forEachNoIterator
import ru.igla.tfprofiler.utils.logI
import java.io.IOException
import java.io.InputStream
import java.util.*


/***
 * https://github.com/tensorflow/examples/tree/master/lite/examples/text_classification/android
 */
class TFLiteAPIModelTextClassification : TFLiteTextDetectAPIModelBase() {

    private val dic: MutableMap<String, Int> = mutableMapOf()

    private val priorityQueue by lazy {
        PriorityQueue(
            MAX_RESULTS
        ) { lhs: TextRecognition, rhs: TextRecognition ->
            rhs.confidence.compareTo(lhs.confidence)
        }
    }

    private val outputScoreArray by lazy {
        Array(modelOptions.numberOfInputImages) { FloatArray(labels.size) }
    }

    override fun init(context: Context, modelEntity: ModelEntity, modelOptions: ModelOptions) {
        super.init(context, modelEntity, modelOptions)
        val buffer = TensorFlowUtils.loadModelFileFromAssets(context, modelEntity.modelFile)

        // Use metadata extractor to extract the dictionary and label files.
        val metadataExtractor = MetadataExtractor(buffer)
        // Extract and load the dictionary file.
        val dictionaryFile: InputStream = metadataExtractor.getAssociatedFile("vocab.txt")
        loadDictionaryFile(dictionaryFile)
        logI { "Dictionary loaded." }

        // Extract and load the label file.
        val labelFile: InputStream = metadataExtractor.getAssociatedFile("labels.txt")
        labels = TensorFlowUtils.loadLabelList(labelFile)
        logI { "Labels loaded." }
    }

    /** Load labels from model file.  */
    @Throws(IOException::class)
    private fun loadDictionaryFile(ins: InputStream) {
        val labelList = TensorFlowUtils.loadLabelList(ins)
        labelList.forEachNoIterator {
            val line: List<String> = it.split(" ")
            if (line.size >= 2) {
                dic[line[0]] = line[1].toInt()
            }
        }
    }

    override fun prepareOutputs(): MutableMap<Int, Any> {
        val outputs = HashMap<Int, Any>()
        outputs[0] = outputScoreArray
        return outputs
    }

    override fun getDetections(outputMap: Map<Int, Any>): List<TextRecognition> {
        priorityQueue.clear()
        for (i in labels.indices) {
            val label = labels[i]
            priorityQueue.add(TextRecognition("" + i, Label(label, i), outputScoreArray[0][i]))
        }
        val results = ArrayList<TextRecognition>()
        while (!priorityQueue.isEmpty()) {
            results.add(priorityQueue.poll())
        }
        results.sort()
        return results
    }

    override fun preprocess(data: String): Array<Any> {
        val tokens = tokenizeInputText(data)
        return arrayOf(arrayOf(tokens))
    }

    /** Pre-prosessing: tokenize and map the input words into a float array.  */
    private fun tokenizeInputText(text: String): IntArray {
        val tmp = IntArray(SENTENCE_LEN)
        val array: List<String> = text.split(SIMPLE_SPACE_OR_PUNCTUATION)
        var index = 0
        // Prepend <START> if it is in vocabulary file.
        if (dic.containsKey(START)) {
            tmp[index++] = dic[START]!!
        }
        for (word in array) {
            if (index >= SENTENCE_LEN) {
                break
            }
            tmp[index++] =
                if (dic.containsKey(word)) dic[word]!! else dic[UNKNOWN] as Int
        }
        // Padding and wrapping.
        Arrays.fill(tmp, index, SENTENCE_LEN - 1, dic[PAD] as Int)
        return tmp
    }

    companion object {
        /** Number of results to show in the UI.  */
        private const val MAX_RESULTS = 3

        private const val SENTENCE_LEN = 256 // The maximum length of an input sentence.

        // Simple delimiter to split words.
        private const val SIMPLE_SPACE_OR_PUNCTUATION = " |\\,|\\.|\\!|\\?|\n"

        /*
       * Reserved values in ImdbDataSet dic:
       * dic["<PAD>"] = 0      used for padding
       * dic["<START>"] = 1    mark for the start of a sentence
       * dic["<UNKNOWN>"] = 2  mark for unknown words (OOV)
       */
        private const val START = "<START>"
        private const val PAD = "<PAD>"
        private const val UNKNOWN = "<UNKNOWN>"
    }
}