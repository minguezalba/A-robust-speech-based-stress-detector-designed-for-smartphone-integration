function grabar_audio()
% Generación de audio grande (silencios, audios BD, claqueta..) para reproducir -> generar_audio.m
% generar_audio;

% Configurar JLink -> Interactuar_RTT.m
process = System.Diagnostics.Process;
process.StartInfo.FileName = 'C:\Program Files\SEGGER\JLink_V512f\JLinkRTTLogger.exe';
process.EnableRaisingEvents = true;
process.StartInfo.CreateNoWindow = false;
 
process.StartInfo.UseShellExecute = false ;  
process.StartInfo.RedirectStandardOutput = true;
  
lh = process.addlistener('OutputDataReceived',@processOutputHandler);  
process.StartInfo.RedirectStandardInput =true; 
process.Start(); 
ProcessStreamWriter = process.StandardInput;
process.BeginOutputReadLine();

  % Obtener fecha -> obtener_fecha.m
      format shortg;

      c = clock;

      fecha = fix(c);

      for i=1:length(fecha)

          if i==1
              aa = fecha(1,i);
              AA = num2str(aa);
          end

          if i==2
              mm = fecha(1,i);
              MM = num2str(mm);

          end

          if i==3
              dd = fecha(1,i);
              DD = num2str(dd);
          end

          if i==4
              hh = fecha(1,i);
              HH = num2str(hh);
          end

          if i==5
              min = fecha(1,i);
              MIN = num2str(min);
          end

          if i==6
              ss = fecha(1,i);
              SS = num2str(ss);
          end
      end

      s = strcat(AA,'_',MM,'_',DD,'_',HH,'_',MIN,'_',SS);

ProcessStreamWriter.WriteLine('NRF52832_XXAA');
ProcessStreamWriter.WriteLine('1');
ProcessStreamWriter.WriteLine('4000');
ProcessStreamWriter.WriteLine(' ');
ProcessStreamWriter.WriteLine('0');
ProcessStreamWriter.WriteLine(strcat('textos\audio_',s,'.txt'));

numInputLines = 0;

ProcessStreamWriter.Close();

% Configurar Termite -> Interactuar_Termite.m
% clc
% clear all
% close all
disp('                          Termite');
disp(' ');

tep = serial('COM6'); 
set(tep, 'BaudRate', 115200); 
set(tep, 'Parity', 'none'); 
set(tep, 'DataBits', 8); 
set(tep, 'StopBits', 1); 

disp(get(tep,{'Type','Name','Port','BaudRate','Parity','DataBits','StopBits'}));
fopen(tep); 

% Start grabación
%data = input('Enter command to start recording:', 's'); 
data = 'start';
fprintf(tep,data); 
disp('Command sent to Serial Port is:');
disp(data);


% Reproducir audio -> reproducir_audio.m
dir_name = 'audios_grandes/20/';
cont = 9;

%dir_name = 'audios/'; % dir de la claqueta para prueba inicial
%cont = 1; %cont de la claqueta para prueba inicial

dir_rec = dir([dir_name, '/*.wav']);

[filepath,name,ext] = fileparts(dir_rec(cont).name)
wav_to_play = [name,ext];
[y,Fs] = audioread(strcat(dir_name,dir_rec(cont).name));
playObj = audioplayer(y,Fs);
playblocking(playObj);

% Stop grabación
%data = input('Enter command to finish recording:', 's');
data2 = 'stop';
fprintf(tep,data2); 
disp('Command sent to Serial Port is:');
disp(data2);

process.WaitForExit();
process.Close();
fclose(tep); 

% system('taskkill /IM JLinkRTTLogger.exe')

% Extraer audios a partir del fichero texto que genera el micro ->
% dividir_audio.m

end 

function processOutputHandler(obj,event)
 if(~isempty(event.Data)) 
     disp(event.Data);
 end
end




