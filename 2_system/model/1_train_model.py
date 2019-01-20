# -*- coding: utf-8 -*-

#=============================================================================
# PACKAGES
#=============================================================================

from dataTools import loadData, plot_confusion_matrix
#import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.neural_network import MLPClassifier
from sklearn.metrics import f1_score, confusion_matrix
import time
from imblearn.over_sampling import RandomOverSampler
import json
 

#=============================================================================
# CONFIGURATION
#=============================================================================

SNR = '_5'
ss = False

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
labels = np.delete(labels_raw, index_nan)


del features_raw, labels_raw, index_nan

ros = RandomOverSampler(random_state=0)
x, y = ros.fit_resample(features, labels)

#=============================================================================
# GENERATE TRAIN AND TEST SETS
#=============================================================================

xtrain, xtest, ytrain, ytest = train_test_split(x, y, test_size=0.2, random_state=42)

mean_train = np.mean(xtrain, axis=0)
std_train = np.std(xtrain, axis=0)

xtrain = xtrain - mean_train
xtrain = xtrain/std_train

xtest = xtest - mean_train
xtest = xtest/std_train

#============================================================================
# SAVING DATA
#=============================================================================

#Export also mean and std from train data
if(ss==True):
    diir = "parameters/" + SNR + "/"
else:
    diir = "parameters_sin_ss/" + SNR + "/"
    
timestr = time.strftime("%Y%m%d_%H%M%S_")

np.savetxt(diir + timestr + "mean.csv", mean_train, fmt='%5f', delimiter=",")
np.savetxt(diir + timestr + "std.csv", std_train, fmt='%5f', delimiter=",")

np.save(diir +  timestr + "xtrain.npy", xtrain)
np.save(diir +  timestr + "ytrain.npy", ytrain)
np.save(diir +  timestr + "xtest.npy", xtest)
np.save(diir +  timestr + "ytest.npy", ytest)


#%%============================================================================
# GENERATE CLASSIFICATION MODEL CROSS VALIDATION
#=============================================================================
t = time.time()

p1 = ['logistic', 'relu', 'tanh']
p2 = np.logspace(-3,-1, 10)
p3 = [(150,), (200,), (250,), (300,)]

parameters = {'activation':p1, 
              'alpha':p2, 
              'hidden_layer_sizes':p3}

mlp_cv = MLPClassifier(solver='adam', max_iter=600)

model = GridSearchCV(mlp_cv, parameters, scoring='f1', cv=3, verbose=25, n_jobs=-2)
model.fit(xtrain, ytrain)
ytest_hat_cv = model.predict(xtest)

print("=============================================================")
print("Best Params : ", model.best_params_)
print("Best Score Training: ", model.best_score_)
print("Score Test CV: ", f1_score(ytest, ytest_hat_cv))
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

#============================================================================
# PLOT CONFUSION MATRIX
#=============================================================================

cnf_matrix = confusion_matrix(ytest, ytest_hat_cv)

# Plot non-normalized confusion matrix
#plt.figure()
plot_confusion_matrix(cnf_matrix, classes=['Neutral', 'Stress'],
                      title='Confusion matrix, without normalization')

# Plot normalized confusion matrix
#plt.figure()
plot_confusion_matrix(cnf_matrix, classes=['Neutral', 'Stress'], normalize=True,
                      title='Normalized confusion matrix')

#plt.show()

              
              
              
              
              
              
              