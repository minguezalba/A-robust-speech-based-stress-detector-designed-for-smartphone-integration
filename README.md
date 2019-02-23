# A robust speech-based stress detector designed for smartphone integration

The challenges of this project are designing and implementing a stress detection system from voice signals, with heart rate measurements used to label the data, suitable for integration into the BINDI system and able to properly work in real environments.

The solution to this problem will be divided into 2 independent parts:

## Part 1: Recording system for data collection

The first one will consist on implement a recording procedure which let us generate a contaminated database that includes the distortions produced by the device and environmental noise. This database will allow us to train and evaluate the robustness of the stress detection system against different noise levels as a first proof-of-concept.

### Requirements and packages dependencies:
* [MATLAB](https://es.mathworks.com) >= 2016b
* [VOICEBOX: Speech Processing Toolbox for MATLAB](http://www.ee.ic.ac.uk/hp/staff/dmb/voicebox/voicebox.html)
* BINDIs pendant drivers
* [str2doubleq for Fast String to Double Conversion](https://es.mathworks.com/matlabcentral/fileexchange/28893-fast-string-to-double-conversion)

### Directory tree:
```
1_generate_data:
  |-- originals: contains original audio files from VOCE Corpus, previously processed in a preliminary study [1].
  |-- recordings: contains clapper and noise files, and concatenated audios ready for recording phase.
  |-- step1: generates concatenated audio files contaminated at different SNR values. They are saved in recordings/ directory.
  |-- step2: contains necessary code for connecting with BINDI microphone and capture audio to text file.
  |-- step3: reading the text files, converting to wav format and splitting them based on the clapper (DTMF Decoder).
```

## Part 2: Stress detector implementation

The second will focus on the Java and Python implementation of the code of the stress detection system based on speech and measurements of heart rate values, meeting the necessary requirements to be integrated into the smartphone used in BINDI.

### Requirements and packages dependencies:
* Java 1.8
* JAR files included: commons-math3-3.7.1, TarsosDSP, gson-2.6.2, opencsv-2.2

* Python 3.7
* Packages needed: [numpy](http://www.numpy.org/) (>=1.8.2), [pandas](https://pandas.pydata.org/) (>=0.23.0), [scipy](https://www.scipy.org/) (>=0.13.3), [scikit-learn](https://scikit-learn.org/) (>=0.20), [imbalanced-learn](https://imbalanced-learn.readthedocs.io) 0.4.2

### Directory tree:
```
2_system:
  |-- TrainStress: contains Java project code to implement speech processing, feature extraction and label generation with audio files from data/data_recordings/
  |-- TestStress: contains Java project code to implement speech processing and feature extraction for test files in data/data_test/
  |-- data: contains necessary files and directories for TrainStress and TestStress projects. It contains audio files and features and labels generated from Java projects.
  |-- model: contains the necessary files for the training and test phase of the models (MLP classifiers) in Python.
  
```

## References:
[1] A. Minguez-Sanchez, “Detección de estrés en señales de voz”, 2017, Available: http://hdl.handle.net/10016/27535.

