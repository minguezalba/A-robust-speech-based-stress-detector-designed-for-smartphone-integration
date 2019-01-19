function y_vad = vad(y,fs)

% 1. Detectamos las zonas de silencio

    threshold = 0.99;

%   Definimos los valores para la función de la Voicebox. Dejamos los
%   valores por defecto excepto para el umbral de decisión

    qq.of=2;        % overlap factor = (fft length)/(frame increment)
    qq.pr=threshold;% Speech probability threshold
    qq.ts=0.1;      % mean talkspurt length (100 ms)
    qq.tn=0.05;     % mean silence length (50 ms)
    qq.ti=10e-3;    % desired output frame increment (10 ms)
    qq.tj=10e-3;    % internal frame increment (10 ms)
    qq.ri=0;        % round ni to the nearest power of 2
    qq.ta=0.396;    % Time const for smoothing SNR estimate = -tinc/log(0.98) from [2]
    qq.gx=1000;     % maximum posterior SNR = 30dB
    qq.gz=1e-4;     % minimum posterior SNR = -40dB
    qq.xn=0;        % minimum prior SNR = -Inf dB
    qq.ne=0;        % noise estimation: 0=min statistics, 1=MMSE [0]

%   Obtenemos vector de detección donde 1=sonido y 0=silencio

    [vad_prob, ~] = vadsohn(y,fs, 'a', qq); 
    
    % vad_prob16000 tiene menor dimensión que signal_16000 porque el algoritmo
    % descarta la última trama si no tiene las muestras justas.
    
    % Rellenamos las muestras que quedan con la ultima muestra de vad_prob
    dif = length(y) - length(vad_prob);
    final = vad_prob(end)*ones(dif,1);
    vad_prob_new = [vad_prob; final];
    
    % Recortamos los silencios de la señal
    y_vad = y(vad_prob_new==1);
               
    % Comprobamos que se han eliminado los silencios
%     figure
%     subplot(2,1,1)
%     plot(y)
%     title('Original signal - Before VAD');
%     
%     subplot(2,1,2)
%     x = 1:1:length(y);
%     y_vad_plot1 = y;
%     y_vad_plot2 = y;
%     y_vad_plot1(vad_prob_new==1) = NaN;
%     y_vad_plot2(vad_prob_new==0) = NaN;
%     plot(x,y_vad_plot1,'r',x,y_vad_plot2);
%     title('After VAD');
    

end

