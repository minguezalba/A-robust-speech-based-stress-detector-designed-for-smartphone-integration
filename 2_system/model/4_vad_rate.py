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
rate = []  # Rate removed frames
ss = False

for snr in list_SNR:
    _, _, indexes1 = loadData('set1', snr, ss=ss)
    _, _, indexes2 = loadData('set2', snr, ss=ss)
    indexes = np.vstack((indexes1, indexes2))
    
    rate.append(np.sum(indexes==0)*100/indexes.shape[0])
    

print("\nMean rate VAD: {0:.2f}%".format(np.mean(rate)))
