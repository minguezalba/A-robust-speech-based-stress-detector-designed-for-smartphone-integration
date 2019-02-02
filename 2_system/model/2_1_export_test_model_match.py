# -*- coding: utf-8 -*-

#=============================================================================
# PACKAGES
#=============================================================================

from dataTools import plot_confusion_matrix
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

#list_SNR = ["original", "20", "15", "10", "5", "0", "_5"]
list_SNR = ["20"]

list_timestamp_con_SS = ["20190119_201325_",
                         "20190119_212719_",
                         "20190119_215209_",
                         "20190119_221312_",
                         "20190119_224030_",
                         "20190119_231955_",
                         "20190119_235745_"]

list_timestamp_con_SS = ["20190119_212719_"]

list_timestamp_sin_SS = ["20190120_104626_",
                         "20190120_112635_",
                         "20190120_120857_",
                         "20190120_124941_",
                         "20190120_133723_",
                         "20190120_142235_",
                         "20190120_150538_"]

ss = True

#############################################

for i in range(0, len(list_SNR)):
    
    ind = i
    SNR = list_SNR[ind]
    
    print("===================================================")
    print("Exporting model SNR:", SNR, "for ss=", ss)
    
    if(ss):
        timestamp = list_timestamp_con_SS[ind]
        diir = "parameters/" + SNR + "/" + timestamp
    else:
        timestamp = list_timestamp_sin_SS[ind]
        diir = "parameters_sin_ss/" + SNR + "/" + timestamp
        
    params = diir + 'params.json'
    
    mean = np.loadtxt(diir + 'mean.csv' , delimiter=',')
    std = np.loadtxt(diir + 'std.csv' , delimiter=',')
    
    xtrain = np.load(diir + "xtrain.npy")
    ytrain = np.load(diir + "ytrain.npy")
    xtest = np.load(diir + "xtest.npy")
    ytest = np.load(diir + "ytest.npy")
    
    with open(params) as f:
        best_params = json.load(f)
    
    
    #============================================================================
    # GENERATE CLASSIFICATION MODEL BEST PARAMETERS
    #============================================================================
    
    
    mlp = MLPClassifier(solver = 'adam', 
                        max_iter = 600,
                        activation = best_params['activation'],
                        alpha = best_params['alpha'],
                        hidden_layer_sizes = best_params['hidden_layer_sizes'] )
    
    mlp.fit(xtrain, ytrain)
    ytest_hat = mlp.predict(xtest)
    print("Score Test Manual: ", f1_score(ytest, ytest_hat))
    
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
    
    
    #===========================================================================
    # EXPORT CLASSIFIER TO JAVA
    #=============================================================================
    
    porter = Porter(mlp, language='java')
    
    if(ss):
        exp_dir = "parameters/" + SNR + "/"
    else:
        exp_dir = "parameters_sin_ss/" + SNR + "/"
    
    
    output = porter.export(export_data=True, export_dir=exp_dir, export_filename=timestamp + 'model.json')
    
    
    #===========================================================================
    # CHECK CLASSIFIER RESULT FROM JAVA
    #===========================================================================
    list_numTest = np.arange(1,11).astype(str)
    print("Generating labels model SNR:", SNR, "for ss=", ss);
    for n in list_numTest:
            
        test_num = n
        if(ss):
            diir_test = "../data/data_test/" + SNR + "/features/features_test" + test_num + '.csv'
        else:
            diir_test = "../data/data_test_sin_ss/" + SNR + "/features/features_test" + test_num + '.csv'
        
        try:
            xtest_java_df = (pd.read_csv(diir_test, header=None)).astype('float64')
            xtest_java = xtest_java_df.values.T
            
            xtest_java = xtest_java - mean
            xtest_java = xtest_java/std
            
            ytest_java_hat = mlp.predict(xtest_java)
            print(ytest_java_hat)
            
            dir_save = "../data/data_test/" + SNR + "/labels_python/StressLabels_test" + test_num + '.csv'
            np.savetxt(dir_save, ytest_java_hat, fmt='%d', delimiter=",")
            
        except:
            continue
