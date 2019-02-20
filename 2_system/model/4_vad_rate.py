# -*- coding: utf-8 -*-


#=============================================================================
# PACKAGES
#=============================================================================

from dataTools import loadData
import numpy as np
 
#=============================================================================
# MAIN
#=============================================================================

list_SNR = ["original", "20", "15", "10", "5", "0", "_5"]
rate_sin_SS = []  # Rate removed frames
labels_sin_SS = []
ss = True

for snr in list_SNR:
    _, labels1, indexes1 = loadData('set1', snr, ss=ss)
    _, labels2, indexes2 = loadData('set2', snr, ss=ss)
    indexes = np.vstack((indexes1, indexes2))
    labels = np.vstack((labels1, labels2))
    
    rate_sin_SS.append(np.sum(indexes==0)*100/indexes.shape[0])
    labels_sin_SS.append(np.sum(labels==0)*100/indexes.shape[0])
    

print("\nMean rate VAD sin SS: {0:.2f}%".format(np.mean(rate_sin_SS)))
print("\nMean class 0 sin SS: {0:.2f}%".format(np.mean(labels_sin_SS)))
