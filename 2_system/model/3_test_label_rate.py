# -*- coding: utf-8 -*-
"""
Created on Mon Jan 14 17:39:10 2019

@author: Alba
"""

import numpy as np
import pandas as pd

def get_both_labels(SNR, ss=True):
    
    list_numTest = np.arange(1,11).astype(str)
    labels_python = []
    labels_java = []
    
    for n in list_numTest:
            
        test_num = n
        
        if(ss):
            file_py = "../data/data_test/" + SNR + "/labels_python/StressLabels_test" + test_num + '.csv'
            file_java = "../data/data_test/" + SNR + "/labels_java/StressLabels_test" + test_num + '.csv'
        else:
            file_py = "../data/data_test_sin_ss/" + SNR + "/labels_python/StressLabels_test" + test_num + '.csv'
            file_java = "../data/data_test_sin_ss/" + SNR + "/labels_java/StressLabels_test" + test_num + '.csv'
       

               
        if len(labels_python) == 0:
            try:
                labels_python = pd.read_csv(file_py, header=None).transpose().values
                labels_java = pd.read_csv(file_java, header=None).values
            except:
                continue
            
        else:
            
            try:
                aux_py = pd.read_csv(file_py, header=None).transpose().values
                labels_python = np.hstack((labels_python, aux_py))
                
                aux_java = pd.read_csv(file_java, header=None).values
                labels_java = np.hstack((labels_java, aux_java))
                
            except:
                continue
            
    
    return [labels_python, labels_java]
 

#=============================================================================
# MAIN
#=============================================================================
    
list_SNR = ["original", "20", "15", "10", "5", "0", "_5"]
ss = True
list_rate = []

for SNR in list_SNR:
      
    [labels_python, labels_java] = get_both_labels(SNR, ss=ss)
    list_rate.append(np.sum(labels_java!=labels_python)*100/labels_java.shape[1])
    
mean_rate = np.mean(list_rate)

print("\nMean error rate between Java and Python labels {0:3.2f} %".format(mean_rate))
        
        
    