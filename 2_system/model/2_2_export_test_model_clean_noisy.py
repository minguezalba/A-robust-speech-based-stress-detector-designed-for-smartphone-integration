# -*- coding: utf-8 -*-

#=============================================================================
# PACKAGES
#=============================================================================

from dataTools import plot_confusion_matrix, loadData
#import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.neural_network import MLPClassifier
from sklearn.metrics import accuracy_score, confusion_matrix
from sklearn_porter import Porter
import json


#=============================================================================
# CONFIGURATION
#=============================================================================

list_SNR = ["20", "15", "10", "5", "0", "_5"]
timestamp_con_SS = "20190204_180607_"
timestamp_sin_SS = "20190204_192748_"

ss = True

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

accuracy = []
conf_matrix = []
Tn = []
Fp = []
Fn = []
Tp = []


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
    accuracy.append(accuracy_score(ytest, ytest_hat))
    print("Score Test: ", accuracy_score(ytest, ytest_hat))
    
    #============================================================================
    # PLOT CONFUSION MATRIX
    #=============================================================================
    
    cnf_matrix = confusion_matrix(ytest, ytest_hat)
    conf_matrix.append(cnf_matrix)
    Tn.append(cnf_matrix[0][0])
    Fp.append(cnf_matrix[0][1])
    Fn.append(cnf_matrix[1][0])
    Tp.append(cnf_matrix[1][1])
    
    
    # Plot non-normalized confusion matrix
    #plt.figure()
    plot_confusion_matrix(cnf_matrix, classes=['Neutral', 'Stress'],
                          title='Confusion matrix, without normalization')
    
    # Plot normalized confusion matrix
    #plt.figure()
    plot_confusion_matrix(cnf_matrix, classes=['Neutral', 'Stress'], normalize=True,
                          title='Normalized confusion matrix')
    
    #plt.show()
    
d = {'accuracy': accuracy,
     'confusion_matrix': conf_matrix,
     'Tn': Tn,
     'Fp': Fp,
     'Fn': Fn,
     'Tp': Tp}

df = pd.DataFrame(data=d)
df.to_excel("output.xlsx")


    