clc
clear all

% =============================================================
% Options: "original", "20", "15", "10", "5", "0", "_5"
SNR = "_5";
% =============================================================

Nseg = 20;
cd '../../TrainStress/data_recordings/audios/'

path_set1 = char(strcat(SNR, '/set1/'));
path_set2 = char(strcat(SNR, '/set2/'));

list_set1 = dir([path_set1, '/*.wav']);
list_set2 = dir([path_set2, '/*.wav']);
list_total = [list_set1; list_set2];


cd '../../../TestStress/data_test/'
Nfiles = 10; % 10 test files per SNR
r = randi([1 length(list_total)],1,Nfiles);

for i=1:Nfiles
   
    [y_orig,fs_orig]=audioread(strcat(list_total(i).folder, '/', list_total(i).name));
    
    a = 1;
    b = length(y_orig)-(Nseg*fs_orig)-1;
    inicio = int32((b-a).*rand(1,1) + a);
    fin = int32(inicio + (Nseg*fs_orig)-1);

    y_new = y_orig(inicio:fin,1);
    
    name = strcat('test', int2str(i), '.wav');
    
    audiowrite(strcat(SNR, '/audios/', name), y_new, fs_orig);

    
end