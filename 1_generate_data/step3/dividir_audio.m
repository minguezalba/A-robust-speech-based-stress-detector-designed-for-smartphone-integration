
% Script para dividir los audios grabados separados por claquetas 
% a partir del fichero de texto que genera el micro

clc
clear all
close all

%%%%%%%%%%%%%%%%%%%%%%%%%%% INFO NECESARIA %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

fs = 16000;   

% Cargamos claqueta
[c_8000,Fs_c] = audioread('../recordings/claqueta_123.wav');
% Resampling clqueta de 8000 Hz a 16100 Hz
[p,q] = rat(16000/Fs_c);
c = resample(c_8000,p,q);
c = c(1:8000);
longC = length(c);
silencio_3seg = 3*fs;



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% RELLENAR %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
directorio = '../recordings/bloques/set2f/';
db = '-5_';
id = fopen('../fase2/textos/set2f_-5_2018_11_9_19_8_51.txt');
minutos = 3;
N = minutos*960000; %960000 equivale a un minuto
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

dir_rec = dir([directorio, '/*.wav']);
text = [];
fragmento = []; % fragmento del fichero que se lee

cont = 1; %indice para recorrer los nombres de los ficheros

while ~feof(id)
    
    fprintf("---------------------");
    fprintf("\nAudio %s\n", dir_rec(cont).name);
    
   % Leemos fragmento de texto
       text = textscan(id, '%s', N);
       numbers = str2doubleq(text{1,1})/2^15;
       fragmento = vertcat(fragmento, numbers);
       
          
   % Identificamos si hay claqueta o no en ese fragmento
   if cont==1
      [indices, tam_trama, claqueta] = detector_claqueta(fragmento,fs);
   else
      [indices, tam_trama, claqueta] = detector_claqueta(fragmento,fs);
   end
             
       if isempty(indices)==1
           disp("No se ha detectado claqueta en este fragmento")
           disp("Estos datos se acumulan para la siguiente iteración.");
                      
       else % Dos posibles casos
                    
           % 1. Caso primer audio (entre dos claquetas)
           if cont==1 && length(indices)>=6
                      
               % Tomar el fragmento de audio desde y hasta las claquetas
               ind_inicio = indices(1)*tam_trama + longC + silencio_3seg;
               ind_final = indices(4)*tam_trama - silencio_3seg;
               frag_audio = fragmento(ind_inicio:ind_final);
               
               % Definimos el nuevo valor de ind_inicio
               ind_inicio = indices(4)*tam_trama + longC + silencio_3seg;
               % Acumulamos el audio restante para la siguiente iteracion
               fragmento = fragmento(ind_inicio:end);
               
               % Guardamos el audio recortado
               if cont>length(dir_rec)
                   name = char(strcat(directorio, "basura.wav"));
               else
                   name = char(strcat(directorio, "nuevo_", db, dir_rec(cont).name));
               end

               
               audiowrite(name, frag_audio, fs)
               cont = cont+1;
           
           % 2. Caso resto de audios (audio + claqueta al final)
           elseif cont~=1 && length(indices)>=3
               
               % Tomamos el audio hasta la proxima claqueta
               ind_final = indices(1)*tam_trama - silencio_3seg;
               frag_audio = fragmento(1:ind_final);
               
               % Actualizamos el valor de ind_inicio para el caso 2
               ind_inicio = indices(3)*tam_trama + longC;
               % Acumulamos el audio restante para la siguiente iteracion
               fragmento = fragmento(ind_inicio:end);
                    
               % Guardamos el audio recortado
               if cont>length(dir_rec)
                   name = char(strcat(directorio, "basura.wav"));
               else
                   name = char(strcat(directorio, "nuevo_", db, dir_rec(cont).name));
               end

              
               audiowrite(name, frag_audio, fs)
               cont = cont+1;
           
           else 
               
               disp("No se han encontrado el número de tonos necesarios para dividir un nuevo fragmento.")               
               disp("Estos datos se acumulan para la siguiente iteración.");
               
           end
                         
           close all
                            
       end
     
end

% Cuando hemos terminado de leer todos los datos de fichero de texto

if feof(id)
    
     % Identificamos si hay claqueta o no en ese fragmento que queda
      [indices, tam_trama, claqueta] = detector_claqueta(fragmento,fs);
      close all
      
     % Calculamos el número de audios que quedan por dividir
     Naudios = length(claqueta)/3;
         
     for m=0:Naudios-1
         
         fprintf("\n---------------------");
         fprintf("\nAudio %i\n", cont);
         
        % Identificamos si hay claqueta o no en ese fragmento
        [indices, tam_trama, claqueta] = detector_claqueta(fragmento,fs);
              
         frag_audio = fragmento(1:indices(1)*tam_trama-silencio_3seg);
         
          % Guardamos el audio recortado
           if cont>length(dir_rec)
               name = char(strcat(directorio, "basura.wav"));
           else
               name = char(strcat(directorio,"nuevo_", db,dir_rec(cont).name));
           end
           
           
           audiowrite(name, frag_audio, fs)
           cont = cont+1;
         
           fragmento = fragmento(indices(1)*tam_trama+longC+silencio_3seg:end);
         
     end
    
end

close all

