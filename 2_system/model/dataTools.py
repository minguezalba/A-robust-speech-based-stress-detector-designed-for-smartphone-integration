# -*- coding: utf-8 -*-

import glob
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

def loadData(setname, snr, extractIndexes=True, ss=True):
    if(ss==True):
        diir = "../data/data_model/" + snr + "/"
    else:
        diir = "../data/data_model_sin_ss/" + snr + "/"
        
    listFeatures = glob.glob(diir + 'features/' + setname + '/*.csv')
    listLabels = glob.glob(diir + 'labels/' + setname + '/stress/*.csv')
    listIndexes = glob.glob(diir + 'indexes/' + setname + '/*.csv')
    
    mfccMatrix = pd.DataFrame() # matriz de nFrames x nMFCC
    aux = pd.DataFrame()
    
    for i in listFeatures:
        
        if mfccMatrix.size == 0:
            mfccMatrix = pd.read_csv(i, header=None).transpose()
        else:
            aux = pd.read_csv(i, header=None).transpose()
            mfccMatrix = pd.concat((mfccMatrix, aux))
           
           
    labels = np.array([]) 
    
    for i in listLabels:
        
        if labels.size == 0:
            labels = (pd.read_csv(i, header=None).values).T
        else:
            labels = np.vstack((labels, (pd.read_csv(i, header=None).values).T))
       
    
    indexes = np.array([])
    
    if extractIndexes:
        for i in listIndexes:
            
            if indexes.size == 0:
                indexes = (pd.read_csv(i, header=None).values).T
            else:
                indexes = np.vstack((indexes, (pd.read_csv(i, header=None).values).T))
        
    return mfccMatrix, labels, indexes
   

def plot_confusion_matrix(cm, classes,
                          normalize=False,
                          title='Confusion matrix',
                          cmap=plt.cm.Blues):
    """
    This function prints and plots the confusion matrix.
    Normalization can be applied by setting `normalize=True`.
    """
    if normalize:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
        print("Normalized confusion matrix")
    else:
        print('Confusion matrix, without normalization')

    print(cm)

#    plt.imshow(cm, interpolation='nearest', cmap=cmap)
#    plt.title(title)
#    plt.colorbar()
#    tick_marks = np.arange(len(classes))
#    plt.xticks(tick_marks, classes, rotation=45)
#    plt.yticks(tick_marks, classes)
#
#    fmt = '.2f' if normalize else 'd'
#    thresh = cm.max() / 2.
#    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
#        plt.text(j, i, format(cm[i, j], fmt),
#                 horizontalalignment="center",
#                 color="white" if cm[i, j] > thresh else "black")
#
#    plt.ylabel('True label')
#    plt.xlabel('Predicted label')
#    plt.tight_layout()
