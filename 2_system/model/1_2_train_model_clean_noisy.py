# -*- coding: utf-8 -*-

#=============================================================================
# PACKAGES
#=============================================================================

from dataTools import loadData
#import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.model_selection import GridSearchCV
from sklearn.neural_network import MLPClassifier
import time
from imblearn.over_sampling import RandomOverSampler
import json

#=============================================================================
# CONFIGURATION
#=============================================================================

SNR_train = '20'
ss = False

#=============================================================================
# LOAD SETS
#=============================================================================

features1, labels1, _ = loadData('set1', SNR_train, extractIndexes=False, ss=ss)
features2, labels2, _ = loadData('set2', SNR_train, extractIndexes=False, ss=ss)

features_raw = pd.concat([features1, features2], ignore_index=True)
labels_raw = np.vstack((labels1, labels2))

del features1, labels1, features2, labels2


#=============================================================================
# PREPARE DATA
#=============================================================================

index_nan = np.unique(np.column_stack(np.where(features_raw.isna()))[:,0])
features_train = (features_raw.drop(features_raw.index[index_nan])).astype('float64').values
labels_train = np.delete(labels_raw, index_nan)

del features_raw, labels_raw, index_nan

ros = RandomOverSampler(random_state=0)
xtrain, ytrain = ros.fit_resample(features_train, labels_train)

mean_train = np.mean(xtrain, axis=0)
std_train = np.std(xtrain, axis=0)

xtrain = xtrain - mean_train
xtrain = xtrain/std_train

#============================================================================
# SAVING DATA
#=============================================================================

#Export also mean and std from train data
if(ss==True):
    diir = "parameters_clean_noisy/"
else:
    diir = "parameters_clean_noisy_sin_ss/"
    
timestr = time.strftime("%Y%m%d_%H%M%S_")

np.savetxt(diir + timestr + "mean.csv", mean_train, fmt='%5f', delimiter=",")
np.savetxt(diir + timestr + "std.csv", std_train, fmt='%5f', delimiter=",")

np.save(diir +  timestr + "xtrain.npy", xtrain)
np.save(diir +  timestr + "ytrain.npy", ytrain)


#%%============================================================================
# GENERATE CLASSIFICATION MODEL CROSS VALIDATION
#=============================================================================
t = time.time()

p1 = ['logistic', 'relu', 'tanh']
p2 = np.logspace(-4,-1, 10)
p3 = [(100,), (150,), (200,), (250,), (300,)]

parameters = {'activation':p1, 
              'alpha':p2, 
              'hidden_layer_sizes':p3}

mlp_cv = MLPClassifier(solver='adam', max_iter=600)

model = GridSearchCV(mlp_cv, parameters, scoring='f1', cv=5, verbose=25, n_jobs=-2)
model.fit(xtrain, ytrain)

print("=============================================================")
print("Best Params : ", model.best_params_)
print("Best Score Training: ", model.best_score_)
print("=============================================================")


elapsed_time = time.time() - t
print("Elapsed time: "+ time.strftime("%H:%M:%S", time.gmtime(elapsed_time)))

best_params = {'activation' : model.best_params_['activation'],
               'alpha' : model.best_params_['alpha'],
               'hidden_layer_sizes' : model.best_params_['hidden_layer_sizes']}
 

json_file = json.dumps(best_params)
f = open(diir + timestr + "params.json","w")
f.write(json_file)
f.close()

              
              
              
              
              
              
              