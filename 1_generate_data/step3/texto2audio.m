clc
clear all
close all

fs = 16000;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%           REVISAR TODOS LOS DIRECTORIOS PORQUE NO ESTAN BIEN            %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% Directorio de audios

dir_texts = 'textos/';
textos = dir([dir_texts, '/*.txt']);
ind_texto = ; 
text_file = textos(ind_texto);

% Directorio de referencia de los bloques
dir_DPARK = 'recordings/DPARK/';
files = dir(dir_DPARK);
dirFlags = [files.isdir] & ~strcmp({files.name},'.') & ~strcmp({files.name},'..');
dir_bloques = files(dirFlags);

% Directorio donde almacenar los nuevos audios
dir_audios_nuevos = 'recordings/DPARK/';
audio_name = strcat('corto_nuevo_largo_14.wav');


id = fopen(strcat(dir_texts,text_file.name));
N = 21*960000;

text = textscan(id, '%s', N);
text = str2doubleq(text{1,1});

numbers = real(text);
max_value = max(numbers)
min_value = min(numbers)

audio = text/2^14;

max_value = max(audio)
min_value = min(audio)

name = strcat(dir_audios_nuevos,audio_name);
audiowrite(name, audio, fs);


