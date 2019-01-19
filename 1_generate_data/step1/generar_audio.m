% Script para generar un único fichero de audio wav que contenga todos los
% audios de un directorio concreto contaminados y separados por claquetas 
% para una SNR concreta

clc
clear all
close all

addpath('voicebox');


fs = 16000; % Frecuencia de muestreo de trabajo
SNR_deseada_dB = -5;

% Cargamos ruido deseado
[n_48000,Fs_n] = audioread('../recordings/n_dpark_ch02.wav');
% Resampling ruido de 48000 Hz a fs de trabajo
[p,q] = rat(fs/Fs_n);
n_orig = resample(n_48000,p,q);

% Cargamos claqueta que separara los audios
[c_8000,Fs_c] = audioread('../recordings/claqueta_123.wav');
% Resampling claqueta de 8000 Hz a fs de trabajo
[p,q] = rat(fs/Fs_c);
c = resample(c_8000,p,q);
% Ampliamos la amplitud de la claqueta para que luego se disintga bien entre
% los audios
c = c*1.5;
% Recortamos el final que es silencio (dura medio segundo)
c = c(1:8000);


% Seleccionamos directorio de audios que queremos contaminar

dirinfo = dir('../recordings/bloques/');
dirinfo(~[dirinfo.isdir]) = [];  %remove non-directories
tf = ismember( {dirinfo.name}, {'.', '..'});
dirinfo(tf) = [];  %remove current and parent directory.


for j=1:length(dirinfo)
    
    tStart = tic;
    fprintf('Generando audio para SNR deseada de %i dB para %s\n', SNR_deseada_dB, dirinfo(j).name);
    disp('------------------------------------------------');


    dir_rec = dir([strcat('../recordings/bloques/',dirinfo(j).name), '/*.wav']);

    % Vector que contendra todos los audios contaminados y separados por
    % claqueta junto con 3 segundos de silencio al principio y final
    y_total = [];

    for i=1:length(dir_rec)

        % Fase 1: Contaminamos un audio concreto

        [~,name,ext] = fileparts(dir_rec(i).name);
        disp(name);

        % Cogemos el audio original (en nuestro caso partimos de mono, 16k)
        [s,Fs] = audioread(strcat('../recordings/bloques/',dirinfo(j).name,'/',dir_rec(i).name));

        % Eliminamos sus zonas de silencio/unvoiced
        s_vad = vad(s, Fs);

        % Calculamos la potencia de la señal sin las zonas de silencio
        power_s = sum(abs(s_vad).^2)/length(s_vad);
        power_s_dB = 10*log10(power_s);

        % Calculamos la potencia del ruido ajustado a la longitud de la señal
        % completa (con silencios)

           if(length(n_orig)>length(s))
               n = n_orig(1:length(s));
           else
               n = [repmat(n_orig, floor(length(s) / length(n_orig)),1); ...
                    n_orig(1:mod(length(s), length(n_orig)))];
           end

        power_n = sum(abs(n).^2)/length(n);
        power_n_dB = 10*log10(power_n);

        % Señal conjunta contaminada con el archivo de ruido original
        y_orig = s + n;
    %     figure()
    %     subplot(2,1,1)
    %     plot(y_orig)
    %     title('Original signal')
        %sound(y_orig, fs);

        % Calculamos la SNR original
        SNR_original_dB = power_s_dB - power_n_dB; % Resta en dB
        fprintf('SNR original: %g dB\n', SNR_original_dB);
        disp('------------------------------------------------');

        % Calculamos el factor de escalado del ruido para una SNR deseada
        power_n_deseada_dB = power_s_dB - SNR_deseada_dB;
        power_n_deseada = 10^(power_n_deseada_dB/10);

        F = sqrt(power_n_deseada/power_n);

        % Escalamos el ruido para la SNR deseada
        new_n = F * n;

        % Comprobamos que hemos conseguido la SNR que queriamos
        power_n_deseada_dB_comprobar = 10*log10(sum(abs(new_n).^2)/length(new_n));
        new_SNR = power_s_dB - power_n_deseada_dB_comprobar;

        % Señal contaminada a SNR concreta: señal original y el ruido escalado
        y_new = s + new_n;

        % Normalizo entre -1 y 1
        y_new = y_new/max(abs(y_new));
    %     subplot(2,1,2)
    %     plot(y_new)
    %     title('Signal with noise')
        %sound(y_new, fs);

        % --------------------------------------------------------------------

        % Fase 2: Concatenamos audios con silencios y claquetas

        % Si estamos en el primer archivo de audio
         if i==1

           % Añadimos 3 segundos de silencio al principio de audio
           silencio = zeros(fs*3,1);
           y_total = silencio;

           % Añadimos la claqueta del inicio y otro bloque de silencio
            y_total = vertcat(y_total, c, silencio);

         end

         % Añadimos la nueva señal
           y_total = vertcat(y_total, y_new);

           % Añadimos silencio + claqueta + silencio
           y_total = vertcat(y_total, silencio, c, silencio);

    end

    % Normalizo entre 1 y -1 para evitar error de cliping de audiowrite
    % Ahora el limite lo pondra la amplitud de la claqueta
    
     y_total = y_total/max(abs(y_total));
     
    % Lo guardo con el nombre de "_SNR deseada" en la carpeta que sea

     name_new = strcat("../fase2/audios_grandes/",dirinfo(j).name,"_", num2str(SNR_deseada_dB),"_dB.wav");

%      figure()
%      plot(y_total);

     fprintf("Nuevo fichero: %s \n", name_new);
     audiowrite(name_new, y_total, fs);

     tEnd = toc(tStart);
     fprintf('Tiempo ejecución: %d minutos and %2.f segundos\n', floor(tEnd/60), rem(tEnd,60));
 
end
 