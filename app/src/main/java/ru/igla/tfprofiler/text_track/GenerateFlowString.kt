package ru.igla.tfprofiler.text_track

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.igla.tfprofiler.utils.forEachNoIterator

class GenerateFlowString {
    fun getFlow(): Flow<String> {
        return flow {
            val txt = "The first Artificial Neural Network\n" +
                    "\n" +
                    "The first artificial neural network was proposed back in 1943 by Warren McCulloch, a Neurophysiologist, and Walter Pitts, a Mathematician, as a result of their research into applying Mathematics, in the form of Boolean logic, to model how neurons within the brain work.  By deploying logic gates, they were able to demonstrate how outputs could be activated when specified inputs are active, to model how neurons work in the brain.  As a model of the brain, the functionality provided was considerably simpler compared to the billions of neurons in the human brain. In addition, their model was only able to process binary inputs and outputs, providing a very simplified model of a neuron.\n" +
                    "The Perceptron\n" +
                    "\n" +
                    "Frank Rosenblatt, a Psychologist, developed a more advanced neural network called the Perceptron in 1958 which was able to process numbers as inputs, in addition to associating weights with inputs.   By using weights, the Perceptron modelled behaviour that had been observed in the brain, that a neuron is more likely to activate a second neuron that it close to when they are both triggered multiple times which is described by Hebb’s Rule that Siegrid Löwel paraphrased as “neurons that fire together, wire together.”\n" +
                    "\n" +
                    "The Perceptron was able to categorise basic images without being given a pre-programmed sequence of steps to execute.  This caused understandable excitement; for the first time, a machine was able to function without being given detailed instructions to follow.\n" +
                    "\n" +
                    "Unfortunately, the breakthrough with the Perceptron also led to a problem that has been a problem throughout the history of Artificial Intelligence: unrealistic claims and hype, including a claim by The New York Times in 1958 that the Perceptron would “be able to walk, talk, see, write, reproduce itself and be conscious of its existence.” \n" +
                    "The first AI winter\n" +
                    "\n" +
                    "In reality, it was becoming apparent that there were limitations to what could be achieved with Neural Networks that could only feed-forward and in 1969, Marvin Minksy and Seymour Papert demonstrated limitations with Perceptrons including their inability to solve problems such as the XOR classification problem, which led to funding drying up.\n" +
                    "\n" +
                    "This period of inactivity, referred to as the first AI winter, continued until the 1980s.  \n" +
                    "Back Propagation\n" +
                    "\n" +
                    "The breakthrough that finally ended the AI winter in 1986 was a back-propagation algorithm that could be used to train multi-layer networks, overcoming the limitations of the single-layer network Perceptron.  This led to a renewed interest in the potential application of neural networks.\n" +
                    "The Second Winter\n" +
                    "\n" +
                    "However, with the relatively-slow processing power where training a model could take weeks and the lack of availability of large datasets in the 1990s, the AI community moved away from neural networks to other methods such as Support Vector Mechanisms (a technique to categorise data by determining which side of a hyperplane item falls on) as these regularly produced better results.\n" +
                    "\n" +
                    "Although neural nets were out of favour in the 1990s, there was still active research which led to key breakthroughs: in 1997, Hochreiter and Schmidhuber developed the long-term short memory (LTSM) to enable values to be persisted across networks with many layers and in 1998 the Convolutional neural network, a multi-layer network based on the visual cortex that is often applied for visual imagery, was presented by LeCun, Bottou, Bengio, and Haffner."
            val splits = txt.chunked(3)
            splits.forEachNoIterator {
                emit(it)
            }
        }
    }
}