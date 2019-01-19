
% Seleccionamos directorio de audios 
dir_name = 'audios/';
dir_rec = dir([dir_name, '/*.wav']);
cont = 2;

% Reproducir audio grande
[filepath,name,ext] = fileparts(dir_rec(cont).name)
wav_to_play = [name,ext];
[y,Fs] = audioread(strcat(dir_name,dir_rec(cont).name));
playObj = audioplayer(y,Fs);
playblocking(playObj);





