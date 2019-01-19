

clc
clear all
close all

fs = 16000;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Rellenar:
texto = '../fase2/textos/set1a_-5_2018_11_9_13_28_15.txt';
dir_audios = '../recordings/bloques/set1a/';
audio_name = 'set1a_-5.wav';
minutos = 18;
N = minutos*960000;
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

id = fopen(texto);

text = textscan(id, '%s', N);
text = str2doubleq(text{1,1});

numbers = real(text);
max_value = max(numbers)
min_value = min(numbers)

audio = text/2^15;

max_value = max(audio)
min_value = min(audio)

plot(audio)

name = strcat(dir_audios,audio_name);

audiowrite(name, audio, fs);


