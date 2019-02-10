# -*- coding: utf-8 -*-


import javaobj
import numpy as np
import matplotlib.pyplot as plt

###############################################################################
# Your work directory must be MODEL directory
###############################################################################


def readSERfile(filename):
    marshaller = javaobj.JavaObjectUnmarshaller(open(filename, "rb"))
    pobj = marshaller.readObject()

    array = [np.asarray(i) for i in list(pobj)]
    
    return array

# Import data from ser files to python
# Install javaobj:

# pip install javaobj-py3 (como admin)
    

#=============================================================================
    
# Defining files

# Options: "original", "20", "15", "10", "5", "0", "_5"
SNR = "10"
# Options: "set1", "set2", "test"
setname = "set1" 
diir = "../data/data_ser/" + SNR + "/" + setname + "/"

#Ejemplos
filename = "ID_62963719_rec"
#filename = "test1"

original = np.asarray(readSERfile(diir+filename+"_original.ser"))
normalize = np.asarray(readSERfile(diir+filename+"_normalize.ser"))
denoised = np.asarray(readSERfile(diir+filename+"_denoised.ser"))
vad = np.asarray(readSERfile(diir+filename+"_vad.ser"))
final = np.asarray(readSERfile(diir+filename+"_final.ser"))

#%%

f, axarr = plt.subplots(5, 1)

axarr[0].plot(original)
axarr[0].set_title("Original")

axarr[1].plot(normalize)
axarr[1].set_title("Normalized")

axarr[2].plot(denoised)
axarr[2].set_title("Denoised")

voiced = denoised.copy()
unvoiced = denoised.copy()
voiced[vad == 0] = np.nan
unvoiced[vad == 1] = np.nan

axarr[3].plot(voiced)
axarr[3].plot(unvoiced, color='r')
axarr[3].set_title("Denoised+VAD")
plt.show()

axarr[4].plot(final)
axarr[4].set_title("Final")

# Fine-tune figure; hide x ticks for top plots and y ticks for right plots
plt.setp([a.get_xticklabels() for a in axarr[0:4]], visible=False)
plt.subplots_adjust(hspace=0.4)
f.suptitle(filename + " with SNR = " + SNR + " dB")
