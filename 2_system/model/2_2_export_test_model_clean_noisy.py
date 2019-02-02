# -*- coding: utf-8 -*-

#=============================================================================
# PACKAGES
#=============================================================================

from dataTools import plot_confusion_matrix, loadData
#import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.neural_network import MLPClassifier
from sklearn.metrics import f1_score, confusion_matrix
from sklearn_porter import Porter
import json


#=============================================================================
# CONFIGURATION
#=============================================================================

list_SNR = ["20", "15", "10", "5", "0", "_5"]
timestamp_con_SS = "20190202_174222_"
timestamp_sin_SS = "20190202_201442_"

ss = False

#=============================================================================
# CHARGE MODEL CLEAN CONDITIONS
#=============================================================================

if(ss):
    timestamp = timestamp_con_SS
    folder = "parameters_clean_noisy/"

else:
    timestamp = timestamp_sin_SS
    folder = "parameters_clean_noisy_sin_ss/"

diir = folder + timestamp
params = diir + 'params.json'
    
mean_train = np.loadtxt(diir + 'mean.csv' , delimiter=',')
std_train = np.loadtxt(diir + 'std.csv' , delimiter=',')

xtrain = np.load(diir + "xtrain.npy")
ytrain = np.load(diir + "ytrain.npy")

with open(params) as f:
    best_params = json.load(f)

mlp = MLPClassifier(solver = 'adam', 
                    max_iter = 600,
                    activation = best_params['activation'],
                    alpha = best_params['alpha'],
                    hidden_layer_sizes = best_params['hidden_layer_sizes'] )

mlp.fit(xtrain, ytrain)

#===========================================================================
# EXPORT CLASSIFIER TO JAVA
#=============================================================================

porter = Porter(mlp, language='java')
output = porter.export(export_data=True, export_dir=folder, export_filename=timestamp + 'model.json')


#=============================================================================
# TEST MODEL FOR DIFFERENT CONDITIONS
#=============================================================================

for i in range(0, len(list_SNR)):
    
    ind = i
    SNR = list_SNR[ind]
    
    print("===================================================")
    print("Testing model SNR:", SNR, "for ss=", ss)
    print("===================================================")

    #=============================================================================
    # LOAD DATA
    #=============================================================================
    features1, labels1, _ = loadData('set1', SNR, extractIndexes=False, ss=ss)
    features2, labels2, _ = loadData('set2', SNR, extractIndexes=False, ss=ss)
    
    features_raw = pd.concat([features1, features2], ignore_index=True)
    labels_raw = np.vstack((labels1, labels2))
    
    del features1, labels1,features2, labels2
    
    #=============================================================================
    # PREPARE DATA
    #=============================================================================
    index_nan = np.unique(np.column_stack(np.where(features_raw.isna()))[:,0])
    features = (features_raw.drop(features_raw.index[index_nan])).astype('float64').values
    ytest = np.delete(labels_raw, index_nan)
        
    del features_raw, labels_raw, index_nan
        
    #=============================================================================
    # NORMALIZE DATA
    #=============================================================================
    xtest = features - mean_train
    xtest = xtest/std_train    
    
    #=============================================================================
    # PREDICT TEST
    #=============================================================================
    ytest_hat = mlp.predict(xtest)
    print("Score Test: ", f1_score(ytest, ytest_hat))
    
    #============================================================================
    # PLOT CONFUSION MATRIX
    #=============================================================================
    
    cnf_matrix = confusion_matrix(ytest, ytest_hat)
    
    # Plot non-normalized confusion matrix
    #plt.figure()
    plot_confusion_matrix(cnf_matrix, classes=['Neutral', 'Stress'],
                          title='Confusion matrix, without normalization')
    
    # Plot normalized confusion matrix
    #plt.figure()
    plot_confusion_matrix(cnf_matrix, classes=['Neutral', 'Stress'], normalize=True,
                          title='Normalized confusion matrix')
    
    #plt.show()
    